package com.example.safehome.data.repo

import com.example.safehome.data.local.FaceTemplateStorage
import com.example.safehome.presentation.biometric.utils.FaceUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricRepository @Inject constructor(
    private val faceTemplateStorage: FaceTemplateStorage,
    private val faceUtils: FaceUtils
) {

    fun saveFaceTemplate(embedding: FloatArray) {
        val normalizedEmbedding = faceUtils.normalizeEmbedding(embedding)
        faceTemplateStorage.saveFaceTemplate(normalizedEmbedding)
    }

    fun verifyFace(currentEmbedding: FloatArray): Boolean {
        val savedEmbedding = faceTemplateStorage.getFaceTemplate() ?: return false
        val normalizedCurrentEmbedding = faceUtils.normalizeEmbedding(currentEmbedding)

        return faceUtils.isSameFace(
            savedEmbedding = savedEmbedding,
            currentEmbedding = normalizedCurrentEmbedding
        )
    }

    fun isBiometricEnabled(): Boolean {
        return faceTemplateStorage.isBiometricEnabled()
    }

    fun clearBiometricData() {
        faceTemplateStorage.clearFaceTemplate()
    }
}
