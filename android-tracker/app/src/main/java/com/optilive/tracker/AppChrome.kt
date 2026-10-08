package com.optilive.tracker
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
object AppChrome {
 fun install(a:AppCompatActivity, selected:Int, hasOldNav:Boolean=true){
  val root=a.findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0) as? LinearLayout ?: return
  val header=root.getChildAt(0) as? LinearLayout
  header?.setBackgroundColor(Color.WHITE)
  fun recolor(v:View){if(v is TextView)v.setTextColor(Color.parseColor("#193D50"));if(v is android.view.ViewGroup)for(k in 0 until v.childCount)recolor(v.getChildAt(k))}
  header?.let{recolor(it)}
  if(a !is MainActivity){val icon=ImageView(a);icon.setImageResource(R.drawable.ic_launcher_optilive);val size=(40*a.resources.displayMetrics.density).toInt();header?.addView(icon,0,LinearLayout.LayoutParams(size,size))}
  if(hasOldNav) root.removeViewAt(root.childCount-1)
  val dp={x:Int -> (x*a.resources.displayMetrics.density).toInt()}
  val bar=LinearLayout(a);bar.orientation=LinearLayout.HORIZONTAL;bar.setBackgroundColor(Color.WHITE)
  val titles=arrayOf("Inicio","Regatas","Mapa","Historial","Más")
  val icons=intArrayOf(R.drawable.ic_nav_home,R.drawable.ic_nav_flag,R.drawable.ic_nav_map,R.drawable.ic_nav_history,R.drawable.ic_nav_more)
  val targets=arrayOf(MainActivity::class.java,RegattasActivity::class.java,MapActivity::class.java,RegattasActivity::class.java,MainActivity::class.java)
  titles.forEachIndexed{i,title ->
   val cell=LinearLayout(a);cell.orientation=LinearLayout.VERTICAL;cell.gravity=android.view.Gravity.CENTER
   val color=Color.parseColor(if(i==selected) "#087CAF" else "#617286")
   val image=ImageView(a);image.setImageResource(icons[i]);image.setColorFilter(color);cell.addView(image,LinearLayout.LayoutParams(dp(24),dp(24)))
   val text=TextView(a);text.text=title;text.textSize=11f;text.setTextColor(color);cell.addView(text)
   cell.contentDescription=title;cell.setOnClickListener{
    if(i==4){androidx.appcompat.app.AlertDialog.Builder(a).setTitle("Más").setItems(arrayOf("Clasificación", "Seguimiento de flota", "Meteo", "Perfil y ajustes", "Organización")){_,which ->
      val target=arrayOf(ClassificationActivity::class.java,TrackingActivity::class.java,WeatherActivity::class.java,MainActivity::class.java,AdminActivity::class.java)[which]
      val next=Intent(a,target);if(which==3)next.putExtra("settings",true);a.startActivity(next)
    }.show()}
    else {val intent=Intent(a,targets[i]);if(i==3)intent.putExtra("history",true);a.startActivity(intent)}
   };bar.addView(cell,LinearLayout.LayoutParams(0,dp(64),1f))
  }
  root.addView(bar,LinearLayout.LayoutParams(-1,dp(64)))
 }
}
