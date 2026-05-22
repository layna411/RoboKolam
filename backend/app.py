import os
from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
from mysql.connector import Error
from werkzeug.utils import secure_filename
import uuid
import datetime
import cv2
import numpy as np


app = Flask(__name__)
CORS(app)

# Database Configuration (XAMPP default)
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'Kolam'
}

UPLOAD_FOLDER = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads')
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

from flask import send_from_directory

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

def get_db_connection():
    try:
        # First connect without DB to create it if it doesn't exist
        temp_conn = mysql.connector.connect(
            host=DB_CONFIG['host'],
            user=DB_CONFIG['user'],
            password=DB_CONFIG['password']
        )
        cursor = temp_conn.cursor()
        cursor.execute(f"CREATE DATABASE IF NOT EXISTS {DB_CONFIG['database']}")
        temp_conn.commit()
        cursor.close()
        temp_conn.close()

        # Connect with DB
        conn = mysql.connector.connect(**DB_CONFIG)
        return conn
    except Error as e:
        print(f"Error connecting to MySQL: {e}")
        return None

def init_db():
    conn = get_db_connection()
    if conn:
        cursor = conn.cursor()
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS images (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                filename VARCHAR(255) NOT NULL,
                path VARCHAR(255) NOT NULL,
                status VARCHAR(50) DEFAULT 'uploaded',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        ''')
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS completed_drawings (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                image_id INT,
                gcode LONGTEXT,
                time_taken INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (image_id) REFERENCES images(id)
            )
        ''')
        conn.commit()
        cursor.close()
        conn.close()
        print("Database initialized successfully.")

@app.route('/auth/signup', methods=['POST'])
def signup():
    data = request.json
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')

    if not username or not email or not password:
        return jsonify({"success": False, "message": "Missing fields"}), 400

    conn = get_db_connection()
    if not conn:
        return jsonify({"success": False, "message": "Database error"}), 500

    try:
        cursor = conn.cursor()
        cursor.execute("INSERT INTO users (username, email, password) VALUES (%s, %s, %s)", (username, email, password))
        conn.commit()
        user_id = cursor.lastrowid
        return jsonify({"success": True, "message": "Signup successful", "user_id": user_id})
    except Error as e:
        if e.errno == 1062:
            return jsonify({"success": False, "message": "Email already exists"}), 400
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

@app.route('/auth/login', methods=['POST'])
def login():
    data = request.json
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return jsonify({"success": False, "message": "Missing fields"}), 400

    conn = get_db_connection()
    if not conn:
        return jsonify({"success": False, "message": "Database error"}), 500

    try:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT id, username, email FROM users WHERE email=%s AND password=%s", (email, password))
        user = cursor.fetchone()
        if user:
            return jsonify({"success": True, "message": "Login successful", "user": user})
        else:
            return jsonify({"success": False, "message": "Invalid credentials"}), 401
    except Error as e:
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

@app.route('/auth/update', methods=['POST'])
def update_profile():
    data = request.json
    user_id = data.get('user_id')
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')

    if not user_id or not username or not email:
        return jsonify({"success": False, "message": "Missing required fields"}), 400

    conn = get_db_connection()
    if not conn:
        return jsonify({"success": False, "message": "Database error"}), 500

    try:
        cursor = conn.cursor(dictionary=True)
        if password and password.strip():
            cursor.execute(
                "UPDATE users SET username=%s, email=%s, password=%s WHERE id=%s",
                (username, email, password.strip(), user_id)
            )
        else:
            cursor.execute(
                "UPDATE users SET username=%s, email=%s WHERE id=%s",
                (username, email, user_id)
            )
        conn.commit()
        
        # Return updated user info
        cursor.execute("SELECT id, username, email FROM users WHERE id=%s", (user_id,))
        user = cursor.fetchone()
        
        return jsonify({"success": True, "message": "Profile updated successfully", "user": user})
    except Error as e:
        if e.errno == 1062:
            return jsonify({"success": False, "message": "Email already exists"}), 400
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

@app.route('/upload/image', methods=['POST'])
def upload_image():
    if 'image' not in request.files:
        return jsonify({"success": False, "message": "No image provided"}), 400
    
    file = request.files['image']
    user_id = request.form.get('user_id', 1) # Default to 1 if not provided

    if file.filename == '':
        return jsonify({"success": False, "message": "Empty filename"}), 400

    if file:
        filename = secure_filename(file.filename)
        unique_name = f"{uuid.uuid4().hex}_{filename}"
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], unique_name)
        file.save(filepath)

        conn = get_db_connection()
        if conn:
            try:
                cursor = conn.cursor()
                cursor.execute("INSERT INTO images (user_id, filename, path) VALUES (%s, %s, %s)", (user_id, filename, filepath))
                conn.commit()
                image_id = cursor.lastrowid
                return jsonify({"success": True, "message": "Upload successful", "image_id": image_id, "path": filepath})
            except Error as e:
                return jsonify({"success": False, "message": str(e)}), 500
            finally:
                if conn.is_connected():
                    cursor.close()
                    conn.close()
        else:
            return jsonify({"success": False, "message": "Database error, but file saved"}), 500

@app.route('/images/<int:user_id>', methods=['GET'])
def get_user_images(user_id):
    conn = get_db_connection()
    if not conn:
        return jsonify({"success": False, "message": "Database error"}), 500
        
    try:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT id, filename, path, status, created_at FROM images WHERE user_id=%s ORDER BY created_at DESC", (user_id,))
        images = cursor.fetchall()
        
        # Add the full URL for each image
        for img in images:
            unique_filename = os.path.basename(img['path'])
            img['url'] = f"/uploads/{unique_filename}"
            
        return jsonify({"success": True, "images": images})
    except Error as e:
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

def generate_gcode_for_image(image_path, sensitivity=0.8, noise_reduction=0.4, algorithm="Canny Edge"):
    img = cv2.imread(image_path)
    if img is None:
        return "; Error loading image"
    
    # Resize to map onto a standard bed size (e.g., 200x200mm)
    img = cv2.resize(img, (200, 200))
    
    # Quantize to 4 colors (1 background + 3 drawing colors)
    pixels = img.reshape((-1, 3))
    pixels = np.float32(pixels)
    criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 10, 1.0)
    K = 4
    _, labels, centers = cv2.kmeans(pixels, K, None, criteria, 10, cv2.KMEANS_RANDOM_CENTERS)
    
    centers = np.uint8(centers)
    counts = np.bincount(labels.flatten())
    background_cluster = np.argmax(counts) # Most frequent color is assumed to be background
    
    gcode = "; Smart Rangoli Generator\nG21 ; Set units to mm\nG90 ; Absolute positioning\nG28 ; Auto-Home X, Y\n"
    tools = ["Z1", "Z2", "Z3"]
    tool_idx = 0
    
    for i in range(K):
        if i == background_cluster:
            continue
        if tool_idx >= len(tools):
            break
            
        color = centers[i].tolist() # BGR
        tool = tools[tool_idx]
        tool_idx += 1
        
        gcode += f"\n; Layer {tool_idx} - RGB Color {color[::-1]}\n{tool} ; Select Color {tool_idx}\n"
        
        mask = np.zeros(labels.shape, dtype=np.uint8)
        mask[labels.flatten() == i] = 255
        mask = mask.reshape(img.shape[:2])
        
        # Apply chosen algorithm
        if algorithm == "Canny Edge":
            mask = cv2.Canny(mask, 50, 150)
        elif algorithm == "Adaptive Threshold":
            mask = cv2.adaptiveThreshold(mask, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2)
        elif algorithm == "Binary Threshold":
            _, mask = cv2.threshold(mask, 127, 255, cv2.THRESH_BINARY)
        else:
            mask = cv2.Canny(mask, 50, 150)
            
        contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        
        # Noise reduction determines minimum contour area (0.0 to 1.0 maps to 0 to 100 pixels)
        min_area = noise_reduction * 100.0
        
        # Sensitivity determines path approximation (1.0 = highly detailed, 0.0 = highly simplified)
        # Epsilon ranges from 0.001 (detailed) to 0.05 (simplified)
        epsilon_factor = 0.05 - (sensitivity * 0.049)

        for cnt in contours:
            length = cv2.arcLength(cnt, True)
            area = cv2.contourArea(cnt)
            # A contour is considered noise only if both its area AND its length are extremely small.
            # If the contour has high length but low area, it is a thin line which we want to preserve!
            # The length threshold is dynamically scaled by noise_reduction.
            if area < min_area and length < (noise_reduction * 100.0):
                continue
                
            epsilon = epsilon_factor * cv2.arcLength(cnt, True)
            approx = cv2.approxPolyDP(cnt, epsilon, True)
            
            cnt = approx.reshape(-1, 2)
            if len(cnt) > 0:
                pt = cnt[0]
                gcode += f"G0 X{pt[0]} Y{pt[1]} F1500\n"
                for pt in cnt[1:]:
                    gcode += f"G1 X{pt[0]} Y{pt[1]} F800\n"
                # close loop
                pt = cnt[0]
                gcode += f"G1 X{pt[0]} Y{pt[1]} F800\n"
                
    return gcode

@app.route('/process_image', methods=['POST'])
def process_image():
    data = request.json
    image_id = data.get('image_id')
    sensitivity = data.get('sensitivity', 0.8)
    noise_reduction = data.get('noise_reduction', 0.4)
    algorithm = data.get('algorithm', 'Canny Edge')
    
    if not image_id:
        return jsonify({"success": False, "message": "Missing image_id"}), 400
        
    conn = get_db_connection()
    if not conn:
        return jsonify({"success": False, "message": "Database error"}), 500
        
    try:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT path FROM images WHERE id=%s", (image_id,))
        img_record = cursor.fetchone()
        
        if not img_record:
            return jsonify({"success": False, "message": "Image not found"}), 404
            
        gcode = generate_gcode_for_image(img_record['path'], sensitivity=float(sensitivity), noise_reduction=float(noise_reduction), algorithm=algorithm)
        
        # update status
        cursor.execute("UPDATE images SET status='processed' WHERE id=%s", (image_id,))
        conn.commit()
        
        return jsonify({"success": True, "gcode": gcode})
        
    except Error as e:
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

@app.route('/drawing/complete', methods=['POST'])
def complete_drawing():
    data = request.json
    user_id = data.get('user_id')
    image_id = data.get('image_id')
    gcode = data.get('gcode')
    time_taken = data.get('time_taken')
    
    if not user_id or not image_id or not gcode or time_taken is None:
        return jsonify({"success": False, "message": "Missing fields"}), 400
        
    conn = get_db_connection()
    if not conn:
        return jsonify({"success": False, "message": "Database error"}), 500
        
    try:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO completed_drawings (user_id, image_id, gcode, time_taken) VALUES (%s, %s, %s, %s)",
            (user_id, image_id, gcode, time_taken)
        )
        conn.commit()
        drawing_id = cursor.lastrowid
        return jsonify({"success": True, "message": "Drawing details stored successfully", "drawing_id": drawing_id})
    except Error as e:
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

@app.route('/drawings/<int:user_id>', methods=['GET'])
def get_completed_drawings(user_id):
    conn = get_db_connection()
    if not conn:
        return jsonify({"success": False, "message": "Database error"}), 500
        
    try:
        cursor = conn.cursor(dictionary=True)
        cursor.execute(
            """SELECT cd.id, cd.image_id, cd.gcode, cd.time_taken, cd.created_at, i.filename, i.path 
               FROM completed_drawings cd 
               JOIN images i ON cd.image_id = i.id 
               WHERE cd.user_id=%s 
               ORDER BY cd.created_at DESC""",
            (user_id,)
        )
        drawings = cursor.fetchall()
        
        # Add full URL for images
        for item in drawings:
            unique_filename = os.path.basename(item['path'])
            item['image_url'] = f"/uploads/{unique_filename}"
            
        return jsonify({"success": True, "drawings": drawings})
    except Error as e:
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

if __name__ == '__main__':
    init_db()
    app.run(host='0.0.0.0', port=5000, debug=True)
