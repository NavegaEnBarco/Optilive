package com.optilive.tracker
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import android.graphics.Bitmap
import android.view.View
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var regattaServer = MainActivity.DEFAULT_SERVER
    private val regattaRefresh = object : Runnable { override fun run() { loadCurrentRegatta(); handler.postDelayed(this, 15000L) } }
    private var raceStartedAt = 0L
    private val clockTick = object:Runnable{override fun run(){val now=System.currentTimeMillis();val local=SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(Date(now));val elapsed=(now-raceStartedAt).coerceAtLeast(0)/1000;val race=String.format(Locale.getDefault(),"%02d:%02d:%02d",elapsed/3600,(elapsed%3600)/60,elapsed%60);findViewById<TextView>(R.id.localTimeTop)?.text=local;findViewById<TextView>(R.id.localTimeBottom)?.text=local;findViewById<TextView>(R.id.raceTimeTop)?.text=race;findViewById<TextView>(R.id.raceTimeBottom)?.text=race;handler.postDelayed(this,1000)}}
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_map)
        val racePrefs=getSharedPreferences("optilive_race",MODE_PRIVATE)
        raceStartedAt=racePrefs.getLong("started_at",0L)
        if(raceStartedAt==0L){raceStartedAt=System.currentTimeMillis();racePrefs.edit().putLong("started_at",raceStartedAt).apply()}
        val p=getSharedPreferences("optilive_profile",MODE_PRIVATE)
        var u=(p.getString("server_url","")?:"").trim()
        if(u.isNotBlank()&&!u.startsWith("http")) u="http://$u"
        regattaServer=(if(u.isBlank()) MainActivity.DEFAULT_SERVER else u).trimEnd('/')
        findViewById<TextView>(R.id.raceName).text="Cargando regata…"
        findViewById<TextView>(R.id.raceClub).text=""
        handler.post(regattaRefresh)
        findViewById<TextView>(R.id.liveStatus).text=if(u.isBlank())"● SIN CONEXIÓN" else "● EN DIRECTO"
        findViewById<TextView>(R.id.gpsStatus).text="▮▮▮ GPS · esperando"
        val w=findViewById<WebView>(R.id.webMap)
        w.settings.javaScriptEnabled=true
        val mapError=findViewById<TextView>(R.id.mapError)
        w.webChromeClient=object:WebChromeClient(){override fun onConsoleMessage(cm:ConsoleMessage):Boolean{Log.e("OptiLiveMap","JS: "+cm.message()+" @"+cm.lineNumber());return true}}
        w.webViewClient=object:WebViewClient(){
            override fun onPageStarted(view:WebView?,url:String?,favicon:Bitmap?){mapError.visibility=View.GONE}
            override fun onPageFinished(view:WebView?,url:String?){
                view?.evaluateJavascript("(function(){return JSON.stringify({leaflet:typeof L!=='undefined',map:!!window.map,tiles:document.querySelectorAll('.leaflet-tile-loaded').length,url:location.href})})()"){v->
                    Log.i("OptiLiveMap","CHECK "+v)
                    if(v.contains("\\\"leaflet\\\":false")||v.contains("\\\"map\\\":false")){mapError.text="Mapa no iniciado · diagnóstico registrado";mapError.visibility=View.VISIBLE}
                }
            }
            override fun onReceivedError(view:WebView?,request:WebResourceRequest?,error:WebResourceError?){
                Log.e("OptiLiveMap","Web error "+error?.errorCode+": "+error?.description+" url="+request?.url)
                if(request?.isForMainFrame==true){mapError.text="Error cargando servidor: "+error?.description;mapError.visibility=View.VISIBLE}
            }
        }
        w.settings.domStorageEnabled=true
        w.settings.mixedContentMode=WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        w.settings.cacheMode=WebSettings.LOAD_NO_CACHE
        w.clearCache(true)
        val mapUrl=(if(u.isBlank()) "https://optilive-node-production.up.railway.app" else u).trimEnd('/')+"/?v=20261004-boat-v2-"+System.currentTimeMillis()
        w.loadUrl(mapUrl)
        handler.post(clockTick)
        loadWind()
        findViewById<TextView>(R.id.windInfo).setOnClickListener{startActivity(Intent(this,WeatherActivity::class.java))}
        findViewById<TextView>(R.id.weatherNav).setOnClickListener{startActivity(Intent(this,WeatherActivity::class.java))}
        findViewById<TextView>(R.id.backTracking).setOnClickListener{finish()}
        findViewById<TextView>(R.id.zoomIn).setOnClickListener{w.evaluateJavascript("if(window.map)map.zoomIn()",null)}
        findViewById<TextView>(R.id.zoomOut).setOnClickListener{w.evaluateJavascript("if(window.map)map.zoomOut()",null)}
        var overlaysVisible=true
        findViewById<TextView>(R.id.layersMap).setOnClickListener{v->overlaysVisible=!overlaysVisible;val vis=if(overlaysVisible) View.VISIBLE else View.GONE;findViewById<View>(R.id.racePanel).visibility=vis;findViewById<View>(R.id.compassPanel).visibility=vis;findViewById<View>(R.id.windInfo).visibility=vis;findViewById<View>(R.id.livePanel).visibility=vis;(v as TextView).text=if(overlaysVisible)"▱" else "▰"}
        findViewById<TextView>(R.id.fullscreenMap).setOnClickListener{w.evaluateJavascript("if(window.map)map.invalidateSize()",null)}
        findViewById<TextView>(R.id.centerMap).setOnClickListener{w.evaluateJavascript("if(window.focusLive)window.focusLive()",null)}
        findViewById<TextView>(R.id.classificationNav).setOnClickListener{startActivity(Intent(this,ClassificationActivity::class.java));finish()}
        findViewById<TextView>(R.id.trackingNav).setOnClickListener{startActivity(Intent(this,TrackingActivity::class.java));finish()}
        findViewById<TextView>(R.id.optionsNav).setOnClickListener{startActivity(Intent(this,MainActivity::class.java));finish()}
    }
    private fun loadCurrentRegatta() {
        Thread {
            val connection = URL(regattaServer + "/api/regattas").openConnection() as java.net.HttpURLConnection
            try {
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val items = org.json.JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
                var latest: JSONObject? = null
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val old = latest
                    if (old == null || item.optLong("createdAt") > old.optLong("createdAt") ||
                        (item.optLong("createdAt") == old.optLong("createdAt") && item.optString("id").compareTo(old.optString("id")) > 0)) latest = item
                }
                val regatta = latest
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        findViewById<TextView>(R.id.raceName).text = regatta?.optString("name")?.takeIf { it.isNotBlank() } ?: "Sin regatas creadas"
                        findViewById<TextView>(R.id.raceClub).text = regatta?.optString("club") ?: ""
                    }
                }
            } catch (_: Exception) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed && findViewById<TextView>(R.id.raceName).text == "Cargando regata…") {
                        findViewById<TextView>(R.id.raceName).text = "Regata no disponible"
                    }
                }
            } finally { connection.disconnect() }
        }.start()
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
                    val j=JSONObject(URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,precipitation,wind_speed_10m,wind_direction_10m,wind_gusts_10m&wind_speed_unit=kn").readText()).getJSONObject("current")
                    val sp=j.getDouble("wind_speed_10m");val deg=j.getDouble("wind_direction_10m");val gust=j.optDouble("wind_gusts_10m",sp);val temp=j.optDouble("temperature_2m",Double.NaN);val rain=j.optDouble("precipitation",0.0)
                    val dirs=arrayOf("N","NE","E","SE","S","SO","O","NO");val dir=dirs[((deg+22.5)/45).toInt()%8]
                    runOnUiThread{out.text="💨  VIENTO (MODELO)  %.1f kn · %s (%d°)    |    RACHA %.1f kn    |    TEMP %.0f°C    |    LLUVIA %.1f mm".format(sp,dir,deg.toInt(),gust,temp,rain);findViewById<TextView>(R.id.gpsStatus).text="▮▮▮ GPS OK · "+((System.currentTimeMillis()-best.optLong("timestamp")).coerceAtLeast(0)/1000)+" s"}
                }catch(e:Exception){runOnUiThread{out.text="💨 Viento no disponible"}}
            }.start()
        }
        handler.postDelayed({refresh()},3000)
        handler.postDelayed(object:Runnable{override fun run(){refresh();handler.postDelayed(this,300000)}},300000)
    }
    override fun onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy()}
}