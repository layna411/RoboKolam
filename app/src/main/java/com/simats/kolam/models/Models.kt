package com.simats.kolam.models

data class AuthRequest(
    val username: String? = null,
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int? = null,
    val user: User? = null
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val image_id: Int? = null,
    val path: String? = null
)

data class GCodeRequest(
    val image_id: Int,
    val sensitivity: Float? = null,
    val noise_reduction: Float? = null,
    val algorithm: String? = null
)

data class GCodeResponse(
    val success: Boolean,
    val message: String? = null,
    val gcode: String? = null
)

data class ImageRecord(
    val id: Int,
    val filename: String,
    val status: String,
    val created_at: String,
    val url: String
)

data class ImagesResponse(
    val success: Boolean,
    val images: List<ImageRecord>? = null,
    val message: String? = null
)

data class DrawingHistoryRequest(
    val user_id: Int,
    val image_id: Int,
    val gcode: String,
    val time_taken: Int
)

data class DrawingHistoryResponse(
    val success: Boolean,
    val message: String? = null,
    val drawing_id: Int? = null
)

data class CompletedDrawingRecord(
    val id: Int,
    val image_id: Int,
    val gcode: String,
    val time_taken: Int,
    val created_at: String,
    val filename: String,
    val path: String,
    val image_url: String
)

data class CompletedDrawingsResponse(
    val success: Boolean,
    val drawings: List<CompletedDrawingRecord>? = null,
    val message: String? = null
)

data class ToolpathLine(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: String // "Red", "Yellow", "Blue", or "Travel"
)

data class UpdateProfileRequest(
    val user_id: Int,
    val username: String,
    val email: String,
    val password: String? = null
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)

