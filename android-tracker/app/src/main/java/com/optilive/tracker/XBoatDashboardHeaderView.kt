package com.optilive.tracker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

/** Tracking-screen-only masthead: dotted X, centered lettering and blue wave mesh. */
class XBoatDashboardHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
 private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
 private var phase = 0f
 private var lastFrame = 0L
 override fun onAttachedToWindow() { super.onAttachedToWindow(); lastFrame = 0L; postInvalidateOnAnimation() }
 override fun onDetachedFromWindow() { lastFrame = 0L; super.onDetachedFromWindow() }
 override fun onDraw(canvas: Canvas) {
  super.onDraw(canvas)
  val now = android.os.SystemClock.uptimeMillis()
  if (lastFrame != 0L) phase += ((now - lastFrame).coerceIn(0L, 50L) / 1000f) * 1.15f
  lastFrame = now
  val d = resources.displayMetrics.density
  val w = width.toFloat()
  val cx = w / 2f
  val cy = 37f*d
  canvas.drawColor(Color.rgb(2, 20, 39))
  paint.style = Paint.Style.STROKE
  for (side in 0..1) for (line in 0..17) {
   val y0 = (66f + line * 2.9f)*d
   paint.color = Color.rgb(0, 145, 245)
   paint.alpha = (95 - line * 3).coerceAtLeast(17)
   paint.strokeWidth = if (line % 5 == 0) 1.0f*d else .45f*d
   val path = Path()
   for (i in 0..70) {
    val t=i/70f
    val x=if(side==0) t*cx else w-t*cx
    val wave=sin(t*6.0+line*.13+phase).toFloat()*11f*d
    val y=y0 - 21f*d*t + wave
    if(i==0)path.moveTo(x,y) else path.lineTo(x,y)
   }
   canvas.drawPath(path,paint)
  }
  paint.style = Paint.Style.FILL
  for(r in -6..6)for(c in -6..6) {
   if(abs(abs(r)-abs(c))>0)continue
   val edge=(abs(r)+abs(c))/12f
   paint.color=if(c>0 && r<1) Color.rgb(23,176,255) else Color.WHITE
   paint.alpha=(255-edge*100).toInt().coerceIn(120,255)
   canvas.drawCircle(cx+c*4.4f*d,cy+r*4.4f*d,(2.55f-edge*.35f)*d,paint)
  }
  paint.alpha=255
  paint.textAlign=Paint.Align.CENTER
  paint.typeface=Typeface.create("sans-serif-light",Typeface.NORMAL)
  paint.color=Color.WHITE
  paint.textSize=25f*d
  paint.letterSpacingCompat()
  canvas.drawText("X  B  O  A  T",cx,92f*d,paint)
  paint.color=Color.rgb(106,185,235)
  paint.textSize=10f*d
  canvas.drawText("G P S   T R A C K E R",cx,109f*d,paint)
  if (isAttachedToWindow && visibility == VISIBLE) postInvalidateDelayed(33L)
 }
 private fun Paint.letterSpacingCompat() { }
}