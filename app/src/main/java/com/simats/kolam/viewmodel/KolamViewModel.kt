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

class KolamViewModel : ViewModel() {

    // --- Auth State ---
    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun resetAuth() {
        _authState.value = null
    }

    fun signup(username: String, email: String, pass: String) {
        viewModelScope.launch {
            try {
                val req = AuthRequest(username = username, email = email, password = pass)
                val response = RetrofitClient.apiService.signup(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    _currentUser.value = User(id = response.body()?.user_id ?: 0, username = username, email = email)
                    _authState.value = "Signup Success"
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
                    _authState.value = "Login Success"
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
                val userIdBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                
                val response = RetrofitClient.apiService.uploadImage(body, userIdBody)
                if (response.isSuccessful && response.body()?.success == true) {
                    println("Upload successful: ${response.body()?.path}")
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

    fun startProcessing() {
        viewModelScope.launch {
            _processingProgress.value = 0f
            _isProcessingComplete.value = false
            for (i in 1..100) {
                delay(30) // Simulate processing time
                _processingProgress.value = i / 100f
            }
            _isProcessingComplete.value = true
        }
    }

    // --- G-Code State ---
    private val _generatedGCode = MutableStateFlow("")
    val generatedGCode: StateFlow<String> = _generatedGCode.asStateFlow()

    fun generateGCode() {
        val dummyGCode = """
            ; Smart Rangoli Generator
            G21 ; Set units to mm
            G90 ; Absolute positioning
            G28 ; Auto-Home X, Y

            ; Layer 1 - Red
            Z1 ; Select Color 1
            G0 X50 Y50 F1500
            G1 X60 Y60 F800
            G1 X70 Y50
            G1 X50 Y50

            ; Layer 2 - Yellow
            Z2 ; Select Color 2
            G0 X100 Y100 F1500
            G1 X110 Y110 F800
            G1 X120 Y100
            G1 X100 Y100
            
            ; Layer 3 - Blue
            Z3 ; Select Color 3
            G0 X150 Y150 F1500
            G1 X160 Y160 F800
            G1 X170 Y150
            G1 X150 Y150
            
            G28 ; Home
        """.trimIndent()
        _generatedGCode.value = dummyGCode
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
