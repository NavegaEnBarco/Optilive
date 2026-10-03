package com.optilive.tracker
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL

class MapActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_map)
        val p=getSharedPreferences("optilive_profile",MODE_PRIVATE)
        var u=(p.getString("server_url","")?:"").trim()
        if(u.isNotBlank()&&!u.startsWith("http")) u="http://$u"
        findViewById<TextView>(R.id.raceName).text="Trofeo Fornells 2026"
        findViewById<TextView>(R.id.raceClub).text=p.getString("club","C.M. Mahón")?:"C.M. Mahón"
        findViewById<TextView>(R.id.liveStatus).text=if(u.isBlank())"● Sin conexión" else "● En directo"
        val w=findViewById<WebView>(R.id.webMap)
        w.settings.javaScriptEnabled=true
        w.settings.domStorageEnabled=true
        w.settings.mixedContentMode=WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        w.loadUrl(if(u.isBlank()) "https://optilive-node-production.up.railway.app" else u)
        loadWind()
        findViewById<TextView>(R.id.windInfo).setOnClickListener{startActivity(Intent(this,WeatherActivity::class.java))}
        findViewById<TextView>(R.id.backTracking).setOnClickListener{finish()}
        findViewById<TextView>(R.id.zoomIn).setOnClickListener{w.evaluateJavascript("if(window.map)map.zoomIn()",null)}
        findViewById<TextView>(R.id.zoomOut).setOnClickListener{w.evaluateJavascript("if(window.map)map.zoomOut()",null)}
        findViewById<TextView>(R.id.centerMap).setOnClickListener{w.evaluateJavascript("if(window.focusLive)window.focusLive()",null)}
        findViewById<TextView>(R.id.classificationNav).setOnClickListener{startActivity(Intent(this,ClassificationActivity::class.java));finish()}
        findViewById<TextView>(R.id.trackingNav).setOnClickListener{startActivity(Intent(this,TrackingActivity::class.java));finish()}
        findViewById<TextView>(R.id.optionsNav).setOnClickListener{startActivity(Intent(this,MainActivity::class.java));finish()}
    }
    private fun loadWind(){
        val out=findViewById<TextView>(R.id.windInfo)
        fun refresh(){
            Thread{
                try{
                    val boats=org.json.JSONArray(URL("https://optilive-node-production.up.railway.app/api/boats").readText())
                    if(boats.length()==0) return@Thread
                    var best=boats.getJSONObject(0)
                    for(i in 1 until boats.length()){val x=boats.getJSONObject(i);if(x.optLong("timestamp")>best.optLong("timestamp"))best=x}
                    val lat=best.getDouble("lat");val lon=best.getDouble("lon")
                    val j=JSONObject(URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=wind_speed_10m,wind_direction_10m,wind_gusts_10m&wind_speed_unit=kn").readText()).getJSONObject("current")
                    val sp=j.getDouble("wind_speed_10m");val deg=j.getDouble("wind_direction_10m");val gust=j.optDouble("wind_gusts_10m",sp)
                    val dirs=arrayOf("N","NE","E","SE","S","SO","O","NO");val dir=dirs[((deg+22.5)/45).toInt()%8]
                    runOnUiThread{out.text="💨 %.1f kn  → %s  · R %.1f".format(sp,dir,gust)}
                }catch(e:Exception){runOnUiThread{out.text="💨 Viento no disponible"}}
            }.start()
        }
        handler.postDelayed({refresh()},3000)
        handler.postDelayed(object:Runnable{override fun run(){refresh();handler.postDelayed(this,300000)}},300000)
    }
    override fun onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy()}
}