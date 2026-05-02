package com.example.safehome.data.repo

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKeyAlias: String by lazy {
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    private val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                "auth_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            Timber.tag("TokenRepository").e("Failed to initialize EncryptedSharedPreferences: ${e.message}")
            PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    fun saveToken(token: String) {
        Timber.tag("TokenRepository").d("Saving token")
        prefs.edit { putString("auth_token", token) }
    }

    fun getToken(): String? {
        Timber.tag("TokenRepository").d("Attempting to get token")
        return try {
            val token = prefs.getString("auth_token", null)
            Timber.tag("TokenRepository").d("Token retrieved: $token")
            token
        } catch (e: Exception) {
            Timber.tag("TokenRepository").e("Error getting token: ${e.message}")
            null
        }
    }

    fun clearToken() {
        Timber.tag("TokenRepository").d("Clearing token")
        prefs.edit { remove("auth_token") }
    }
}