package com.firstapp.dogscanai.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class ScanFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A69FF")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A69FF")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var pulseAlpha = 0.3f
    private val cornerLength = 60f

    private val pulseAnimator = ValueAnimator.ofFloat(0.3f, 0.7f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            pulseAlpha = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        pulseAnimator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulseAnimator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val pad = 4f

        // Outer pulse ring
        ringPaint.alpha = (pulseAlpha * 255).toInt()
        canvas.drawCircle(w / 2, h / 2, (w / 2) - pad, ringPaint)

        // Inner pulse ring (smaller, more subtle)
        ringPaint.alpha = (pulseAlpha * 0.5f * 255).toInt()
        canvas.drawCircle(w / 2, h / 2, (w / 2) - pad - 14f, ringPaint)

        // Corners — always fully visible
        cornerPaint.alpha = 255

        // Top-left
        canvas.drawLine(pad, pad + cornerLength, pad, pad, cornerPaint)
        canvas.drawLine(pad, pad, pad + cornerLength, pad, cornerPaint)

        // Top-right
        canvas.drawLine(w - pad - cornerLength, pad, w - pad, pad, cornerPaint)
        canvas.drawLine(w - pad, pad, w - pad, pad + cornerLength, cornerPaint)

        // Bottom-left
        canvas.drawLine(pad, h - pad - cornerLength, pad, h - pad, cornerPaint)
        canvas.drawLine(pad, h - pad, pad + cornerLength, h - pad, cornerPaint)

        // Bottom-right
        canvas.drawLine(w - pad - cornerLength, h - pad, w - pad, h - pad, cornerPaint)
        canvas.drawLine(w - pad, h - pad, w - pad, h - pad - cornerLength, cornerPaint)
    }
}