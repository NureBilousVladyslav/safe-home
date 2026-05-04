package com.example.safehome.presentation.biometric.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

object ImageProxyUtils {
    private const val OUTPUT_PIXEL_STRIDE = 2

    @OptIn(ExperimentalGetImage::class)
    fun toInputImage(imageProxy: ImageProxy): InputImage? {
        return createInputImage(imageProxy)
    }

    @ExperimentalGetImage
    private fun createInputImage(imageProxy: ImageProxy): InputImage? {
        val mediaImage = imageProxy.image ?: return null

        return InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
    }

    @OptIn(ExperimentalGetImage::class)
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return createBitmapFromImageProxy(imageProxy)
    }

    @ExperimentalGetImage
    private fun createBitmapFromImageProxy(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        val nv21 = yuv420ToNv21(imageProxy)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val outputStream = ByteArrayOutputStream()

        yuvImage.compressToJpeg(
            Rect(0, 0, image.width, image.height),
            JPEG_QUALITY,
            outputStream
        )

        val imageBytes = outputStream.toByteArray()

        val bitmap = BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size
        ) ?: return null

        return rotateBitmap(
            bitmap = bitmap,
            rotationDegrees = imageProxy.imageInfo.rotationDegrees
        )
    }

    fun cropFaceFromBitmap(
        sourceBitmap: Bitmap,
        faceBoundingBox: Rect,
        paddingPercent: Float = DEFAULT_FACE_PADDING
    ): Bitmap? {
        val paddingX = (faceBoundingBox.width() * paddingPercent).toInt()
        val paddingY = (faceBoundingBox.height() * paddingPercent).toInt()

        val left = max(faceBoundingBox.left - paddingX, 0)
        val top = max(faceBoundingBox.top - paddingY, 0)
        val right = min(faceBoundingBox.right + paddingX, sourceBitmap.width)
        val bottom = min(faceBoundingBox.bottom + paddingY, sourceBitmap.height)

        val width = right - left
        val height = bottom - top

        if (width <= 0 || height <= 0) return null

        return Bitmap.createBitmap(sourceBitmap, left, top, width, height)
    }

    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return bitmap.scale(width, height)
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun yuv420ToNv21(imageProxy: ImageProxy): ByteArray {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)

        unpackPlane(
            buffer = vBuffer,
            rowStride = imageProxy.planes[2].rowStride,
            pixelStride = imageProxy.planes[2].pixelStride,
            width = imageProxy.width / 2,
            height = imageProxy.height / 2,
            output = nv21,
            offset = ySize
        )

        unpackPlane(
            buffer = uBuffer,
            rowStride = imageProxy.planes[1].rowStride,
            pixelStride = imageProxy.planes[1].pixelStride,
            width = imageProxy.width / 2,
            height = imageProxy.height / 2,
            output = nv21,
            offset = ySize + 1
        )

        return nv21
    }

    private fun unpackPlane(
        buffer: ByteBuffer,
        rowStride: Int,
        pixelStride: Int,
        width: Int,
        height: Int,
        output: ByteArray,
        offset: Int
    ) {
        var outputOffset = offset

        for (row in 0 until height) {
            val rowStart = row * rowStride

            for (column in 0 until width) {
                val inputIndex = rowStart + column * pixelStride

                if (inputIndex < buffer.limit() && outputOffset < output.size) {
                    output[outputOffset] = buffer.get(inputIndex)
                }

                outputOffset += OUTPUT_PIXEL_STRIDE
            }
        }
    }


    private const val JPEG_QUALITY = 90
    private const val DEFAULT_FACE_PADDING = 0.20f
}
