package com.example.safehome.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceTemplateStorage @Inject constructor(
    private val encryptedSharedPreferences: SharedPreferences
) {

    fun saveFaceTemplate(embedding: FloatArray) {
        val embeddingString = embedding.joinToString(separator = EMBEDDING_SEPARATOR)

        encryptedSharedPreferences.edit {
            putString(KEY_FACE_EMBEDDING, embeddingString)
            putBoolean(KEY_BIOMETRIC_ENABLED, true)
        }
    }

    fun getFaceTemplate(): FloatArray? {
        val embeddingString = encryptedSharedPreferences.getString(KEY_FACE_EMBEDDING, null)
            ?: return null

        val values = embeddingString
            .split(EMBEDDING_SEPARATOR)
            .mapNotNull { value -> value.toFloatOrNull() }

        if (values.isEmpty()) return null

        return values.toFloatArray()
    }

    fun isBiometricEnabled(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun clearFaceTemplate() {
        encryptedSharedPreferences.edit {
            remove(KEY_FACE_EMBEDDING)
            putBoolean(KEY_BIOMETRIC_ENABLED, false)
        }
    }

    companion object {
        private const val KEY_FACE_EMBEDDING = "key_face_embedding"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val EMBEDDING_SEPARATOR = ";"
    }
}
