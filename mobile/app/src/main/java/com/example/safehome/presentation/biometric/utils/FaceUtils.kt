package com.example.safehome.presentation.biometric.utils

import jakarta.inject.Inject
import kotlin.math.sqrt

class FaceUtils @Inject constructor(){

    fun isSameFace(
        savedEmbedding: FloatArray,
        currentEmbedding: FloatArray,
        threshold: Float = DEFAULT_FACE_THRESHOLD
    ): Boolean {
        if (savedEmbedding.size != currentEmbedding.size) return false

        val distance = calculateEuclideanDistance(savedEmbedding, currentEmbedding)
        return distance <= threshold
    }

    fun calculateEuclideanDistance(
        firstEmbedding: FloatArray,
        secondEmbedding: FloatArray
    ): Float {
        require(firstEmbedding.size == secondEmbedding.size) {
            "Embedding sizes must be equal"
        }

        var sum = 0f

        for (index in firstEmbedding.indices) {
            val difference = firstEmbedding[index] - secondEmbedding[index]
            sum += difference * difference
        }

        return sqrt(sum)
    }

    fun normalizeEmbedding(embedding: FloatArray): FloatArray {
        var squareSum = 0f

        for (value in embedding) {
            squareSum += value * value
        }

        val norm = sqrt(squareSum)
        if (norm == 0f) return embedding

        return FloatArray(embedding.size) { index ->
            embedding[index] / norm
        }
    }

    fun averageEmbeddings(embeddings: List<FloatArray>): FloatArray {
        require(embeddings.isNotEmpty()) {
            "Embeddings list cannot be empty"
        }

        val embeddingSize = embeddings.first().size

        embeddings.forEach { embedding ->
            require(embedding.size == embeddingSize) {
                "All embeddings must have the same size"
            }
        }

        val result = FloatArray(embeddingSize)

        for (embedding in embeddings) {
            for (index in embedding.indices) {
                result[index] += embedding[index]
            }
        }

        for (index in result.indices) {
            result[index] /= embeddings.size
        }

        return normalizeEmbedding(result)
    }

    companion object {
        const val DEFAULT_FACE_THRESHOLD = 1.0f
    }
}
