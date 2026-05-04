package com.example.safehome.presentation.biometric.utils

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

class FaceEmbeddingUtils @Inject constructor(
    @param:ApplicationContext private val context: Context
) {


    private val interpreter: Interpreter by lazy {
        Interpreter(loadModelFile())
    }


    fun getFaceEmbedding(faceBitmap: Bitmap): FloatArray {
        val resizedBitmap = ImageProxyUtils.resizeBitmap(
            bitmap = faceBitmap,
            width = INPUT_IMAGE_SIZE,
            height = INPUT_IMAGE_SIZE
        )

        val inputBuffer = convertBitmapToInputBuffer(resizedBitmap)

        val outputShape = interpreter.getOutputTensor(0).shape()
        val embeddingSize = outputShape.last()

        val output = Array(1) {
            FloatArray(embeddingSize)
        }

        interpreter.run(inputBuffer, output)

        return FaceUtils().normalizeEmbedding(output[0])
    }

    private fun convertBitmapToInputBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(
            1 * INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE * PIXEL_SIZE * FLOAT_SIZE
        )

        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE)

        bitmap.getPixels(
            pixels,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )

        for (pixel in pixels) {
            val red = pixel shr 16 and 0xFF
            val green = pixel shr 8 and 0xFF
            val blue = pixel and 0xFF

            inputBuffer.putFloat(normalizePixel(red))
            inputBuffer.putFloat(normalizePixel(green))
            inputBuffer.putFloat(normalizePixel(blue))
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun normalizePixel(value: Int): Float {
        return (value - 127.5f) / 128.0f
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(FACE_NET_MODEL_NAME)

        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel

            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    companion object {
        private const val FACE_NET_MODEL_NAME = "facenet.tflite"
        private const val INPUT_IMAGE_SIZE = 160
        private const val PIXEL_SIZE = 3
        private const val FLOAT_SIZE = 4
    }
}
