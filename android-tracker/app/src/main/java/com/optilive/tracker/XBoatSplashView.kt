package com.optilive.tracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.abs
import kotlin.math.sin

/** X BOAT brand artwork, drawn natively so it stays sharp at any screen resolution. */
class XBoatSplashView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val navy = Color.rgb(2, 13, 27)
    private val cyan = Color.rgb(91, 203, 255)
    private val white = Color.rgb(239, 248, 255)
    private var phase = 0f
    private val started = System.currentTimeMillis()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawColor(navy)
        val unit = w / 390f
        // Flowing, translucent ribbon waves, intentionally confined to the lower half.
        for (layer in 0..5) {
            val path = Path()
            val baseline = h * (0.61f + layer * 0.052f)
            val amplitude = (19 + layer * 5) * unit
            for (step in 0..100) {
                val x = w * step / 100f
                val y = baseline + sin(x / w * 8.0 + phase + layer * 0.52).toFloat() * amplitude +
                    sin(x / w * 3.2 + layer * 1.3).toFloat() * 10f * unit
                if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = (1.0f + layer * .16f) * unit
            paint.color = Color.argb(48 + layer * 9, 88, 191, 242)
            canvas.drawPath(path, paint)
        }
        // Dotted X: a distinctive original dot matrix rather than a boat pictogram.
        val centerX = w * .5f
        val centerY = h * .375f
        val spacing = 8.4f * unit
        paint.style = Paint.Style.FILL
        for (row in -6..6) {
            for (col in -6..6) {
                if (abs(abs(row) - abs(col)) > 1) continue
                val distance = (abs(row) + abs(col)) / 12f
                val radius = (2.7f - distance * 1.0f) * unit
                val isBlue = col > 1 && row < 3
                paint.color = if (isBlue) cyan else white
                paint.alpha = (240 - distance * 100).toInt()
                canvas.drawCircle(centerX + col * spacing, centerY + row * spacing, radius, paint)
            }
        }
        paint.alpha = 255
        paint.typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        paint.color = white
        paint.textSize = 27f * unit
        paint.letterSpacingCompat()
        canvas.drawText("X  B O A T", centerX, centerY + 102f * unit, paint)
        paint.textSize = 11f * unit
        paint.color = cyan
        canvas.drawText("G P S   T R A C K E R", centerX, centerY + 135f * unit, paint)
        paint.color = white
        paint.textSize = 10f * unit
        canvas.drawText("O P T I M I S T", centerX, centerY + 154f * unit, paint)
        phase = ((System.currentTimeMillis() - started) / 2600f)
        postInvalidateDelayed(32)
    }

    private fun Paint.letterSpacingCompat() { /* spacing encoded in text for Canvas */ }
}
