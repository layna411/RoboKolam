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
    }

    private fun saveUser(user: User) {
        sharedPrefs?.edit()?.putString("user_data", gson.toJson(user))?.apply()
    }

    private fun loadUser() {
        val userData = sharedPrefs?.getString("user_data", null)
        if (userData != null) {
            _currentUser.value = gson.fromJson(userData, User::class.java)
            fetchUserImages()
        }
    }

    private fun clearUser() {
        sharedPrefs?.edit()?.remove("user_data")?.apply()
    }

    // --- Auth State ---
    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _userImages = MutableStateFlow<List<ImageRecord>>(emptyList())
    val userImages: StateFlow<List<ImageRecord>> = _userImages.asStateFlow()

    fun resetAuth() {
        _authState.value = null
    }
    
    fun logout() {
        _currentUser.value = null
        _authState.value = null
        _userImages.value = emptyList()
        _selectedImageUri.value = null
        uploadedImageId = null
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
                    _generatedGCode.value = response.body()?.gcode ?: "; Empty GCode"
                } else {
                    _generatedGCode.value = "; Error processing image"
                }
            } catch (e: Exception) {
                _generatedGCode.value = "; API Error: ${e.message}"
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
        
        viewModelScope.launch {
            _isDrawing.value = true
            _drawingProgress.value = 0f
            
            // Simulating parsing and drawing G-Code
            val commands = _generatedGCode.value.lines().filter { it.isNotBlank() && !it.startsWith(";") }
            val totalCommands = commands.size
            
            commands.forEachIndexed { index, command ->
                if (!_isDrawing.value) return@launch // Handled Stop/Pause
                
                delay(500) // Delay between commands to simulate real movement
                
                when {
                    command.startsWith("Z1") -> _activeColor.value = "Red (Z1)"
                    command.startsWith("Z2") -> _activeColor.value = "Yellow (Z2)"
                    command.startsWith("Z3") -> _activeColor.value = "Blue (Z3)"
                    command.startsWith("G0") || command.startsWith("G1") -> {
                        // Extract X and Y
                        val parts = command.split(" ")
                        parts.forEach { part ->
                            if (part.startsWith("X")) _currentX.value = part.substring(1).toFloatOrNull() ?: _currentX.value
                            if (part.startsWith("Y")) _currentY.value = part.substring(1).toFloatOrNull() ?: _currentY.value
                        }
                    }
                }
                _drawingProgress.value = (index + 1).toFloat() / totalCommands
            }
            
            _isDrawing.value = false
            _activeColor.value = "None"
        }
    }
    
    fun stopDrawing() {
        _isDrawing.value = false
    }
}
