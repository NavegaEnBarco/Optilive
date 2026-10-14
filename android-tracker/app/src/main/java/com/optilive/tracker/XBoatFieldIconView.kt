package com.optilive.tracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/** Monochrome nautical line icons, not system emoji glyphs. */
class XBoatFieldIconView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val kind: Int
    init {
        val a = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.tag))
        a.recycle()
        kind = (attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "tag") ?: "0").toIntOrNull() ?: 0
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        canvas.save()
        canvas.translate(cx - 22f * scale, cy - 22f * scale)
        canvas.scale(scale, scale)
        paint.color = Color.rgb(115, 195, 255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.6f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        fun line(vararg points: Float) {
            val path = Path()
            path.moveTo(points[0], points[1])
            var i = 2
            while (i < points.size) { path.lineTo(points[i], points[i+1]); i += 2 }
            canvas.drawPath(path, paint)
        }
        when (kind) {
            0 -> { // Optimist sail
                line(22f, 4f, 22f, 35f)
                line(20f, 6f, 8f, 32f, 34f, 32f, 20f, 6f)
                line(8f, 36f, 36f, 36f, 31f, 41f, 13f, 41f, 8f, 36f)
            }
            1 -> { // Person
                canvas.drawCircle(22f, 13f, 7f, paint)
                canvas.drawArc(8f, 23f, 36f, 43f, 180f, 180f, false, paint)
                line(8f, 34f, 8f, 40f, 36f, 40f, 36f, 34f)
            }
            2 -> { // Group
                canvas.drawCircle(17f, 13f, 6f, paint)
                canvas.drawCircle(32f, 17f, 5f, paint)
                canvas.drawArc(3f, 22f, 31f, 43f, 180f, 180f, false, paint)
                canvas.drawArc(25f, 25f, 43f, 42f, 180f, 150f, false, paint)
            }
            3 -> { // Trophy
                line(11f, 8f, 33f, 8f, 31f, 24f, 26f, 30f, 18f, 30f, 13f, 24f, 11f, 8f)
                canvas.drawArc(2f, 11f, 17f, 27f, 85f, 195f, false, paint)
                canvas.drawArc(27f, 11f, 42f, 27f, 260f, 195f, false, paint)
                line(22f, 30f, 22f, 37f, 13f, 37f, 31f, 37f)
            }
            else -> { // Pennant
                line(10f, 4f, 10f, 42f)
                line(10f, 6f, 37f, 17f, 10f, 29f)
            }
        }
        canvas.restore()
    }
}