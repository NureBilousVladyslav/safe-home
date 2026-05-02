package com.example.safehome.data.api

import com.example.safehome.data.model.AddDeviceRequest
import com.example.safehome.data.model.FirebaseLoginRequest
import com.example.safehome.data.model.MessageResponse
import com.example.safehome.data.model.ResetPasswordRequest
import com.example.safehome.data.model.ResetPasswordResponse
import com.example.safehome.data.model.SignUpResponse
import com.example.safehome.data.model.VerifyTokenResponse
import com.example.safehome.data.model.SignInRequest
import com.example.safehome.data.model.SignUpRequest
import com.example.safehome.data.model.SignInResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @POST("login/token")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun login(
        @Body request: SignInRequest
    ): SignInResponse

    @POST("verify-token")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun checkToken(
        @Header("Authorization") token: String
    ): VerifyTokenResponse

    @POST("register")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun signUp(
        @Body request: SignUpRequest
    ): SignUpResponse

    @POST("reset-password")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): ResetPasswordResponse

    @POST("login/firebase")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun firebaseLogin(
        @Body request: FirebaseLoginRequest
    ): SignInResponse

    @POST("devices")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun addDevice(
        @Header("Authorization") token: String,
        @Body request: AddDeviceRequest
    ): MessageResponse
}