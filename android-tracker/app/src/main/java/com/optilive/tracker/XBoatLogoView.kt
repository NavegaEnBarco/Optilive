package com.optilive.tracker
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

/** Dotted X brand mark for X BOAT onboarding. */
class XBoatLogoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): View(context, attrs) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val cx = width / 2f
        val u = resources.displayMetrics.density
        val cy = 47f * u
        p.style = Paint.Style.FILL
        for (row in -5..5) for (col in -5..5) {
            if (abs(abs(row)-abs(col)) > 1) continue
            val distance = (abs(row)+abs(col))/10f
            p.color = if (col > 1 && row < 2) Color.rgb(62,170,255) else Color.WHITE
            p.alpha = (245 - distance * 130).toInt().coerceIn(70,255)
            c.drawCircle(cx + col * 7.1f*u, cy + row*7.1f*u, (2.65f-distance*.75f)*u, p)
        }
        p.alpha = 255
        p.textAlign = Paint.Align.CENTER
        p.color = Color.WHITE
        p.typeface = android.graphics.Typeface.create("sans-serif-light", 0)
        p.textSize = 21f*u
        c.drawText("X  B  O  A  T", cx, 103f*u, p)
        p.textSize = 7f*u
        p.color = Color.rgb(78,144,192)
        c.drawText("T R A C K E R   O P T I M I S T", cx, 116f*u, p)
    }
}