package com.example.safehome.data.api

import com.example.safehome.data.model.AddHomeRequest
import com.example.safehome.data.model.GetHomesResponse
import com.example.safehome.data.model.MessageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface HomeApi {
    @GET("homes")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun getHomes(
        @Header("Authorization") token: String?
    ): Response<GetHomesResponse>

    @POST("homes")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun addHome(
        @Header("Authorization") token: String?,
        @Body request: AddHomeRequest
    ): Response<MessageResponse>

    @DELETE("homes/{home_id}")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun deleteHome(
        @Header("Authorization") token: String?,
        @Path("home_id") homeId: String
    ): Response<Void>

    @POST("homes/{home_id}/archive")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun archiveHome(
        @Header("Authorization") token: String?,
        @Path("home_id") homeId: String
    ): Response<MessageResponse>

    @POST("homes/{home_id}/unarchive")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun unArchiveHome(
        @Header("Authorization") token: String?,
        @Path("home_id") homeId: String
    ): Response<MessageResponse>

    @POST("homes/{home_id}/security/armed")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun armedHome(
        @Header("Authorization") token: String?,
        @Path("home_id") homeId: String
    ): Response<MessageResponse>

    @POST("homes/{home_id}/security/disarmed")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun disarmedHome(
        @Header("Authorization") token: String?,
        @Path("home_id") homeId: String
    ): Response<MessageResponse>
}