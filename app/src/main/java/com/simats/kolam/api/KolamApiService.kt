package com.simats.kolam.api

import com.simats.kolam.models.AuthRequest
import com.simats.kolam.models.AuthResponse
import com.simats.kolam.models.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface KolamApiService {
    @POST("auth/signup")
    suspend fun signup(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("user_id") userId: RequestBody
    ): Response<UploadResponse>

    @POST("process_image")
    suspend fun processImage(@Body request: com.simats.kolam.models.GCodeRequest): Response<com.simats.kolam.models.GCodeResponse>

    @retrofit2.http.GET("images/{user_id}")
    suspend fun getUserImages(@retrofit2.http.Path("user_id") userId: Int): Response<com.simats.kolam.models.ImagesResponse>

    @POST("drawing/complete")
    suspend fun saveDrawingHistory(@Body request: com.simats.kolam.models.DrawingHistoryRequest): Response<com.simats.kolam.models.DrawingHistoryResponse>

    @retrofit2.http.GET("drawings/{user_id}")
    suspend fun getCompletedDrawings(@retrofit2.http.Path("user_id") userId: Int): Response<com.simats.kolam.models.CompletedDrawingsResponse>

    @retrofit2.http.POST("auth/update")
    suspend fun updateProfile(@retrofit2.http.Body request: com.simats.kolam.models.UpdateProfileRequest): Response<com.simats.kolam.models.UpdateProfileResponse>
}
