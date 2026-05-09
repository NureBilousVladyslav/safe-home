package com.example.safehome.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceTemplateStorage @Inject constructor(
    private val encryptedSharedPreferences: SharedPreferences,
    private val prefs: SharedPreferences
) {

    fun saveFaceTemplate(embedding: FloatArray) {
        val embeddingString = embedding.joinToString(separator = PrefKeys.EMBEDDING_SEPARATOR)

        encryptedSharedPreferences.edit {
            putString(PrefKeys.KEY_FACE_EMBEDDING, embeddingString)
            putBoolean(PrefKeys.KEY_FACE_ID_ENABLED, true)
        }
    }

    fun getFaceTemplate(): FloatArray? {
        val embeddingString = encryptedSharedPreferences.getString(PrefKeys.KEY_FACE_EMBEDDING, null)
            ?: return null

        val values = embeddingString
            .split(PrefKeys.EMBEDDING_SEPARATOR)
            .mapNotNull { value -> value.toFloatOrNull() }

        if (values.isEmpty()) return null

        return values.toFloatArray()
    }

    fun isBiometricEnabled(): Boolean {
        return encryptedSharedPreferences.getBoolean(PrefKeys.KEY_FACE_ID_ENABLED, false)
    }

    fun clearFaceTemplate() {
        encryptedSharedPreferences.edit {
            remove(PrefKeys.KEY_FACE_EMBEDDING)
            putBoolean(PrefKeys.KEY_FACE_ID_ENABLED, false)
        }
    }

    fun wasFaceIdPromptShown(): Boolean {
        return prefs.getBoolean(PrefKeys.KEY_FACE_ID_PROMPT_SHOWN, false)
    }

    fun markFaceIdPromptShown() {
        prefs.edit {
            putBoolean(PrefKeys.KEY_FACE_ID_PROMPT_SHOWN, true)
        }
    }
}
