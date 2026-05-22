package com.simats.kolam.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.simats.kolam.api.RetrofitClient
import com.simats.kolam.models.AuthRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.content.Context
import java.io.FileOutputStream
import com.simats.kolam.models.User
import com.simats.kolam.models.ImageRecord
import com.google.gson.Gson

class KolamViewModel : ViewModel() {
    private val gson = Gson()
    private var sharedPrefs: android.content.SharedPreferences? = null

    fun initPrefs(context: Context) {
        sharedPrefs = context.getSharedPreferences("kolam_prefs", Context.MODE_PRIVATE)
        loadUser()
        loadSettings()
    }

    private fun saveUser(user: User) {
        sharedPrefs?.edit()?.putString("user_data", gson.toJson(user))?.apply()
    }

    private fun loadUser() {
        val userData = sharedPrefs?.getString("user_data", null)
        if (userData != null) {
            _currentUser.value = gson.fromJson(userData, User::class.java)
            fetchUserImages()
            fetchCompletedDrawings()
        }
    }

    private fun clearUser() {
        sharedPrefs?.edit()?.remove("user_data")?.apply()
    }

    private fun loadSettings() {
        sharedPrefs?.let { prefs ->
            _motorStepsX.value = prefs.getFloat("motor_steps_x", 80.0f)
            _motorStepsY.value = prefs.getFloat("motor_steps_y", 80.0f)
            _motorStepsZ.value = prefs.getFloat("motor_steps_z", 80.0f)
            _powderFlowRate.value = prefs.getFloat("powder_flow_rate", 100.0f)
            _bedWidth.value = prefs.getFloat("bed_width", 200.0f)
            _bedHeight.value = prefs.getFloat("bed_height", 200.0f)
            _machineFeedrate.value = prefs.getFloat("machine_feedrate", 1500.0f)
            
            _appThemeMode.value = prefs.getString("app_theme_mode", "System") ?: "System"
            _notificationsEnabled.value = prefs.getBoolean("notifications_enabled", true)
            _unitsPreference.value = prefs.getString("units_preference", "mm") ?: "mm"
        }
    }

    // --- Auth State ---
    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _userImages = MutableStateFlow<List<ImageRecord>>(emptyList())
    val userImages: StateFlow<List<ImageRecord>> = _userImages.asStateFlow()

    private val _completedDrawings = MutableStateFlow<List<com.simats.kolam.models.CompletedDrawingRecord>>(emptyList())
    val completedDrawings: StateFlow<List<com.simats.kolam.models.CompletedDrawingRecord>> = _completedDrawings.asStateFlow()

    private val _toolpathLines = MutableStateFlow<List<com.simats.kolam.models.ToolpathLine>>(emptyList())
    val toolpathLines: StateFlow<List<com.simats.kolam.models.ToolpathLine>> = _toolpathLines.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(-1)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()

    // --- Settings State ---
    private val _motorStepsX = MutableStateFlow(80.0f)
    val motorStepsX: StateFlow<Float> = _motorStepsX.asStateFlow()

    private val _motorStepsY = MutableStateFlow(80.0f)
    val motorStepsY: StateFlow<Float> = _motorStepsY.asStateFlow()

    private val _motorStepsZ = MutableStateFlow(80.0f)
    val motorStepsZ: StateFlow<Float> = _motorStepsZ.asStateFlow()

    private val _powderFlowRate = MutableStateFlow(100.0f)
    val powderFlowRate: StateFlow<Float> = _powderFlowRate.asStateFlow()

    private val _bedWidth = MutableStateFlow(200.0f)
    val bedWidth: StateFlow<Float> = _bedWidth.asStateFlow()

    private val _bedHeight = MutableStateFlow(200.0f)
    val bedHeight: StateFlow<Float> = _bedHeight.asStateFlow()

    private val _machineFeedrate = MutableStateFlow(1500.0f)
    val machineFeedrate: StateFlow<Float> = _machineFeedrate.asStateFlow()

    private val _appThemeMode = MutableStateFlow("System")
    val appThemeMode: StateFlow<String> = _appThemeMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _unitsPreference = MutableStateFlow("mm")
    val unitsPreference: StateFlow<String> = _unitsPreference.asStateFlow()

    fun resetAuth() {
        _authState.value = null
    }
    
    fun logout() {
        _currentUser.value = null
        _authState.value = null
        _userImages.value = emptyList()
        _completedDrawings.value = emptyList()
        _selectedImageUri.value = null
        uploadedImageId = null
        _toolpathLines.value = emptyList()
        _currentLineIndex.value = -1
        clearUser()
    }

    fun fetchUserImages() {
        viewModelScope.launch {
            val userId = _currentUser.value?.id ?: return@launch
            try {
                val response = RetrofitClient.apiService.getUserImages(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _userImages.value = response.body()?.images ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun signup(username: String, email: String, pass: String) {
        viewModelScope.launch {
            try {
                val req = AuthRequest(username = username, email = email, password = pass)
                val response = RetrofitClient.apiService.signup(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = User(id = response.body()?.user_id ?: 0, username = username, email = email)
                    _currentUser.value = user
                    saveUser(user)
                    _authState.value = "Signup Success"
                    fetchUserImages()
                    fetchCompletedDrawings()
                } else {
                    _authState.value = "Error: ${response.body()?.message}"
                }
            } catch (e: Exception) {
                _authState.value = "Exception: ${e.message}"
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            try {
                val req = AuthRequest(email = email, password = pass)
                val response = RetrofitClient.apiService.login(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    _currentUser.value = response.body()?.user
                    response.body()?.user?.let { saveUser(it) }
                    _authState.value = "Login Success"
                    fetchUserImages()
                    fetchCompletedDrawings()
                } else {
                    _authState.value = "Error: ${response.body()?.message}"
                }
            } catch (e: Exception) {
                _authState.value = "Exception: ${e.message}"
            }
        }
    }

    // --- Upload Image State ---
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()
    
    private var uploadedImageId: Int? = null

    fun setSelectedImage(uri: Uri?, context: Context? = null) {
        _selectedImageUri.value = uri
        
        // If context is provided, we can upload the image
        if (uri != null && context != null) {
            uploadImage(uri, context)
        }
    }

    fun selectRecentImage(imageRecord: ImageRecord) {
        uploadedImageId = imageRecord.id
        val fullUrl = if (imageRecord.url.startsWith("http")) {
            imageRecord.url
        } else {
            val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
            val path = if (imageRecord.url.startsWith("/")) imageRecord.url else "/${imageRecord.url}"
            baseUrl + path
        }
        _selectedImageUri.value = Uri.parse(fullUrl)
        _generatedGCode.value = ""
        _isProcessingComplete.value = false
        _processingProgress.value = 0f
    }

    private fun uploadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                
                val reqFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", tempFile.name, reqFile)
                val userIdBody = (_currentUser.value?.id ?: 1).toString().toRequestBody("text/plain".toMediaTypeOrNull())
                
                val response = RetrofitClient.apiService.uploadImage(body, userIdBody)
                if (response.isSuccessful && response.body()?.success == true) {
                    uploadedImageId = response.body()?.image_id
                    println("Upload successful: ${response.body()?.path}")
                    fetchUserImages()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Processing State ---
    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress: StateFlow<Float> = _processingProgress.asStateFlow()

    private val _isProcessingComplete = MutableStateFlow(false)
    val isProcessingComplete: StateFlow<Boolean> = _isProcessingComplete.asStateFlow()
    
    private val _sensitivity = MutableStateFlow(0.8f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private val _noiseReduction = MutableStateFlow(0.4f)
    val noiseReduction: StateFlow<Float> = _noiseReduction.asStateFlow()

    private val _algorithm = MutableStateFlow("Canny Edge")
    val algorithm: StateFlow<String> = _algorithm.asStateFlow()

    fun updateSettings(sens: Float, noise: Float, algo: String = _algorithm.value) {
        _sensitivity.value = sens
        _noiseReduction.value = noise
        _algorithm.value = algo
    }

    fun startProcessing() {
        viewModelScope.launch {
            _processingProgress.value = 0f
            _isProcessingComplete.value = false
            
            if (uploadedImageId == null) {
                _generatedGCode.value = "; No image uploaded"
                _processingProgress.value = 1f
                _isProcessingComplete.value = true
                return@launch
            }
            
            try {
                _processingProgress.value = 0.5f // Halfway there...
                
                val req = com.simats.kolam.models.GCodeRequest(
                    image_id = uploadedImageId!!,
                    sensitivity = _sensitivity.value,
                    noise_reduction = _noiseReduction.value,
                    algorithm = _algorithm.value
                )
                val response = RetrofitClient.apiService.processImage(req)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val code = response.body()?.gcode ?: "; Empty GCode"
                    _generatedGCode.value = code
                    parseGCode(code)
                } else {
                    _generatedGCode.value = "; Error processing image"
                    _toolpathLines.value = emptyList()
                    _currentLineIndex.value = -1
                }
            } catch (e: Exception) {
                _generatedGCode.value = "; API Error: ${e.message}"
                _toolpathLines.value = emptyList()
                _currentLineIndex.value = -1
            }
            
            _processingProgress.value = 1f
            _isProcessingComplete.value = true
        }
    }

    // --- G-Code State ---
    private val _generatedGCode = MutableStateFlow("")
    val generatedGCode: StateFlow<String> = _generatedGCode.asStateFlow()

    fun generateGCode() {
        // GCode is now generated by the backend during startProcessing().
        // No op needed here.
    }

    // --- Bluetooth State ---
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    fun connectDevice() {
        viewModelScope.launch {
            _connectionStatus.value = "Connecting..."
            delay(1500)
            _isConnected.value = true
            _connectionStatus.value = "Connected (BLE)"
        }
    }

    fun disconnectDevice() {
        _isConnected.value = false
        _connectionStatus.value = "Disconnected"
    }

    // --- Drawing Simulation State ---
    private val _isDrawing = MutableStateFlow(false)
    val isDrawing: StateFlow<Boolean> = _isDrawing.asStateFlow()

    private val _drawingProgress = MutableStateFlow(0f)
    val drawingProgress: StateFlow<Float> = _drawingProgress.asStateFlow()
    
    private val _currentX = MutableStateFlow(0f)
    val currentX: StateFlow<Float> = _currentX.asStateFlow()
    
    private val _currentY = MutableStateFlow(0f)
    val currentY: StateFlow<Float> = _currentY.asStateFlow()
    
    private val _activeColor = MutableStateFlow("None")
    val activeColor: StateFlow<String> = _activeColor.asStateFlow()

    fun startDrawing() {
        if (!_isConnected.value) return
        val lines = _toolpathLines.value
        if (lines.isEmpty()) return
        
        viewModelScope.launch {
            _isDrawing.value = true
            _drawingProgress.value = 0f
            _currentLineIndex.value = 0
            val startTime = System.currentTimeMillis()
            
            val totalLines = lines.size
            lines.forEachIndexed { index, line ->
                if (!_isDrawing.value) return@launch // Handled Stop/Pause
                
                // Speed up simulation to make it fluid: 30ms for travel, 70ms for drawing
                val stepDelay = if (line.color == "Travel") 30L else 70L
                delay(stepDelay)
                
                _currentX.value = line.endX
                _currentY.value = line.endY
                _activeColor.value = when (line.color) {
                    "Red" -> "Red (Z1)"
                    "Yellow" -> "Yellow (Z2)"
                    "Blue" -> "Blue (Z3)"
                    else -> "None"
                }
                
                _currentLineIndex.value = index
                _drawingProgress.value = (index + 1).toFloat() / totalLines
            }
            
            val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            
            // Drawing is fully complete! Store details in database.
            saveCompletedDrawing(durationSeconds)
            
            _isDrawing.value = false
            _activeColor.value = "None"
            _currentLineIndex.value = -1
        }
    }
    
    fun saveCompletedDrawing(timeTakenSeconds: Int) {
        val userId = _currentUser.value?.id ?: 1
        val imageId = uploadedImageId ?: return
        val gcode = _generatedGCode.value
        if (gcode.isBlank()) return
        
        viewModelScope.launch {
            try {
                val req = com.simats.kolam.models.DrawingHistoryRequest(
                    user_id = userId,
                    image_id = imageId,
                    gcode = gcode,
                    time_taken = timeTakenSeconds
                )
                val response = RetrofitClient.apiService.saveDrawingHistory(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    println("Successfully stored drawing details, id: ${response.body()?.drawing_id}")
                    // Refresh Completed Drawings List automatically!
                    fetchCompletedDrawings()
                } else {
                    println("Failed to store drawing: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun fetchCompletedDrawings() {
        viewModelScope.launch {
            val userId = _currentUser.value?.id ?: return@launch
            try {
                val response = RetrofitClient.apiService.getCompletedDrawings(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _completedDrawings.value = response.body()?.drawings ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun parseGCode(gcode: String) {
        val parsedLines = mutableListOf<com.simats.kolam.models.ToolpathLine>()
        var curX = 0f
        var curY = 0f
        var activeCol = "Travel"
        
        gcode.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith(";")) return@forEach
            if (trimmed.isEmpty()) return@forEach
            
            when {
                trimmed.startsWith("Z1") -> activeCol = "Red"
                trimmed.startsWith("Z2") -> activeCol = "Yellow"
                trimmed.startsWith("Z3") -> activeCol = "Blue"
                trimmed.startsWith("G0") || trimmed.startsWith("G1") -> {
                    val isDraw = trimmed.startsWith("G1") && activeCol != "Travel"
                    var nextX = curX
                    var nextY = curY
                    val parts = trimmed.split(" ")
                    parts.forEach { part ->
                        if (part.startsWith("X")) nextX = part.substring(1).toFloatOrNull() ?: curX
                        if (part.startsWith("Y")) nextY = part.substring(1).toFloatOrNull() ?: curY
                    }
                    
                    parsedLines.add(
                        com.simats.kolam.models.ToolpathLine(
                            startX = curX,
                            startY = curY,
                            endX = nextX,
                            endY = nextY,
                            color = if (isDraw) activeCol else "Travel"
                        )
                    )
                    curX = nextX
                    curY = nextY
                }
            }
        }
        _toolpathLines.value = parsedLines
        _currentLineIndex.value = -1
    }
    
    fun stopDrawing() {
        _isDrawing.value = false
    }

    // --- Profile & Settings Actions ---
    fun updateProfile(username: String, email: String, password: String?, onResult: (Boolean, String) -> Unit) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            try {
                val req = com.simats.kolam.models.UpdateProfileRequest(
                    user_id = userId,
                    username = username,
                    email = email,
                    password = if (password.isNullOrBlank()) null else password
                )
                val response = RetrofitClient.apiService.updateProfile(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedUser = response.body()?.user
                    if (updatedUser != null) {
                        _currentUser.value = updatedUser
                        saveUser(updatedUser)
                        onResult(true, "Profile updated successfully")
                    } else {
                        onResult(false, "Failed to update profile data")
                    }
                } else {
                    onResult(false, response.body()?.message ?: "Error updating profile")
                }
            } catch (e: Exception) {
                onResult(false, "Exception: ${e.message}")
            }
        }
    }

    fun updateMachineSettings(stepsX: Float, stepsY: Float, stepsZ: Float, flowRate: Float, width: Float, height: Float, feedrate: Float) {
        _motorStepsX.value = stepsX
        _motorStepsY.value = stepsY
        _motorStepsZ.value = stepsZ
        _powderFlowRate.value = flowRate
        _bedWidth.value = width
        _bedHeight.value = height
        _machineFeedrate.value = feedrate
        
        sharedPrefs?.edit()?.apply {
            putFloat("motor_steps_x", stepsX)
            putFloat("motor_steps_y", stepsY)
            putFloat("motor_steps_z", stepsZ)
            putFloat("powder_flow_rate", flowRate)
            putFloat("bed_width", width)
            putFloat("bed_height", height)
            putFloat("machine_feedrate", feedrate)
            apply()
        }
    }
    
    fun setAppThemeMode(theme: String) {
        _appThemeMode.value = theme
        sharedPrefs?.edit()?.putString("app_theme_mode", theme)?.apply()
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sharedPrefs?.edit()?.putBoolean("notifications_enabled", enabled)?.apply()
    }
    
    fun setUnitsPreference(units: String) {
        _unitsPreference.value = units
        sharedPrefs?.edit()?.putString("units_preference", units)?.apply()
    }
}
