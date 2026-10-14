package com.optilive.tracker
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.util.AttributeSet
import kotlin.math.sin

class XBoatWaveView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val start = System.currentTimeMillis()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawColor(Color.rgb(2, 13, 27))
        val phase = (System.currentTimeMillis() - start) / 4200.0
        for (row in 0..21) {
            val path = Path()
            for (step in 0..110) {
                val x = w * step / 110f
                val wave = sin(x / w * 9.0 + phase + row * .18) * (18 + row * 1.5)
                val y = h * .84f + row * 5.0f + wave.toFloat()
                if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = if (row % 4 == 0) 1.4f else .65f
            paint.color = Color.argb(if (row % 4 == 0) 145 else 60, 44, 174, 255)
            canvas.drawPath(path, paint)
        }
        paint.style = Paint.Style.FILL
        for (row in 0..13) for (col in 0..45) {
            val x = w * col / 45f
            val y = h * .81f + row * 10f + sin(col * .25 + phase + row * .2).toFloat() * 22f
            paint.color = Color.argb(55 + (row % 3) * 20, 71, 194, 255)
            canvas.drawCircle(x, y, .8f, paint)
        }
        postInvalidateDelayed(50)
    }
}