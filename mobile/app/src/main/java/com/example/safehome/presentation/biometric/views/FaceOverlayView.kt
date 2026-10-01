package com.example.safehome.presentation.biometric.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#99000000")  // темно-сірий напівпрозорий
        style = Paint.Style.FILL
    }

    private val framePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 5f
        alpha = 220
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.FILL
    }

    private val rect = RectF()

    // Підказки
    var showLeftArrow = false
    var showRightArrow = false
    var showUpArrow = false
    var showDownArrow = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthSize = width * 0.70f
        val heightSize = height * 0.42f

        val left = (width - widthSize) / 2f
        val top = (height - heightSize) / 2f - (height * 0.04f)

        rect.set(left, top, left + widthSize, top + heightSize)

        // 1. Затемнення всього екрану
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // 2. Прозорий овал всередині
        canvas.drawOval(rect, clearPaint)

        // 3. Біла рамка
        canvas.drawOval(rect, framePaint)

        // Стрілки
        val centerX = width / 2f
        val centerY = rect.centerY()

        if (showLeftArrow) drawArrow(canvas, centerX - widthSize/2 - 45, centerY, -1, 0)
        if (showRightArrow) drawArrow(canvas, centerX + widthSize/2 + 45, centerY, 1, 0)
        if (showUpArrow) drawArrow(canvas, centerX, centerY - heightSize/2 - 40, 0, -1)
        if (showDownArrow) drawArrow(canvas, centerX, centerY + heightSize/2 + 40, 0, 1)
    }

    private fun drawArrow(canvas: Canvas, x: Float, y: Float, dx: Int, dy: Int) {
        val path = android.graphics.Path().apply {
            moveTo(x, y)
            lineTo(x - dx * 30f, y - dy * 30f)
            lineTo(x - dx * 12f, y - dy * 12f)
            lineTo(x + dx * 30f, y + dy * 30f)
            close()
        }
        canvas.drawPath(path, Paint().apply {
            style = Paint.Style.FILL
            color = Color.YELLOW
            alpha = 230
        })
    }

    fun updateGuidance(
        left: Boolean = false,
        right: Boolean = false,
        up: Boolean = false,
        down: Boolean = false
    ) {
        showLeftArrow = left
        showRightArrow = right
        showUpArrow = up
        showDownArrow = down
        invalidate()
    }
}