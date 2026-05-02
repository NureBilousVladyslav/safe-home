package com.example.safehome.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.safehome.data.api.AuthApi
import com.example.safehome.data.model.AddDeviceRequest
import com.example.safehome.data.model.MessageResponse
import com.example.safehome.data.model.Result
import com.example.safehome.data.network.NetworkHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_prefs")

@Singleton
class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val networkHandler: NetworkHandler
) {
    private val fcmTokenKey = stringPreferencesKey("fcm_token")

    suspend fun saveFcmToken(fcmToken: String) {
        context.dataStore.edit { prefs ->
            prefs[fcmTokenKey] = fcmToken
        }
    }

    private suspend fun getFcmToken(): String? {
        return context.dataStore.data.first()[fcmTokenKey]
    }

    suspend fun addDevice(token: String): Result<MessageResponse> {
        var fcmToken = getFcmToken()
        if (fcmToken == null) {

            fcmToken = ""
        }

        val deviceInfo = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}, Android ${android.os.Build.VERSION.RELEASE}"
        return networkHandler.safeApiCall {
            authApi.addDevice(token, AddDeviceRequest(deviceInfo, fcmToken))
        }
    }

}