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
