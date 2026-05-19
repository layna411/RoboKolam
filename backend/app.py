import os
from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
from mysql.connector import Error
from werkzeug.utils import secure_filename
import uuid
import datetime

app = Flask(__name__)
CORS(app)

# Database Configuration (XAMPP default)
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'Kolam'
}

UPLOAD_FOLDER = 'uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

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

if __name__ == '__main__':
    init_db()
    app.run(host='0.0.0.0', port=5000, debug=True)
