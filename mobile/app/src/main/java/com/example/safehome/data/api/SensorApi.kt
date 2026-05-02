package com.example.safehome.data.api

import com.example.safehome.data.model.ActiveSensorRequest
import com.example.safehome.data.model.AddSensorRequest
import com.example.safehome.data.model.GetSensorsResponse
import com.example.safehome.data.model.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SensorApi {
    @GET("sensors/{home_id}")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun getSensors(
        @Header("Authorization") token: String?,
        @Path("home_id") homeId: String
    ): Response<GetSensorsResponse>

    @POST("sensors")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun addSensor(
        @Header("Authorization") token: String?,
        @Body request: AddSensorRequest
    ): Response<MessageResponse>

    @DELETE("sensors/{sensor_id}")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun deleteSensor(
        @Header("Authorization") token: String?,
        @Path("sensor_id") sensorId: String
    ): Response<MessageResponse>

    @PATCH("sensors/{sensor_id}/activity")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun setActiveSensor(
        @Header("Authorization") token: String?,
        @Path("sensor_id") sensorId: String,
        @Body request: ActiveSensorRequest,
    ): Response<MessageResponse>

    @POST("sensors/{sensor_id}/archive")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun archiveSensor(
        @Header("Authorization") token: String?,
        @Path("sensor_id") sensorId: String
    ): Response<MessageResponse>

    @POST("sensors/{sensor_id}/unarchive")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun unArchiveSensor(
        @Header("Authorization") token: String?,
        @Path("sensor_id") sensorId: String
    ): Response<MessageResponse>
}