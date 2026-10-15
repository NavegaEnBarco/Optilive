package com.optilive.tracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

/** Compact dotted X for the dashboard header only. */
class XBoatHeaderMarkView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val unit = (width.coerceAtMost(height) / 13f)
        val centerX = width / 2f
        val centerY = height / 2f
        for (r in -5..5) for (c in -5..5) {
            if (abs(abs(r) - abs(c)) > 0) continue
            val edge = (abs(r) + abs(c)) / 10f
            p.color = if (c > 0 && r < 1) Color.rgb(33, 177, 255) else Color.WHITE
            p.alpha = (255 - edge * 95).toInt().coerceIn(120, 255)
            canvas.drawCircle(centerX + c * unit, centerY + r * unit,
                unit * (0.34f - edge * 0.05f), p)
        }
    }
}