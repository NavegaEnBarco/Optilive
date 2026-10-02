package com.optilive.tracker
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MapActivity: AppCompatActivity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState); setContentView(R.layout.activity_map)
  val prefs=getSharedPreferences("optilive_profile",MODE_PRIVATE)
  val url=prefs.getString("server_url","") ?: ""
  val club=prefs.getString("club","") ?: ""
  findViewById<TextView>(R.id.raceClub).text=if(club.isBlank()) "En directo" else club
  val web=findViewById<WebView>(R.id.webMap)
  web.settings.javaScriptEnabled=true; web.settings.domStorageEnabled=true; web.webViewClient=WebViewClient()
  if(url.isNotBlank()) web.loadUrl(url) else web.loadData("<body style='font-family:sans-serif;background:#eaf2f7;text-align:center;padding-top:180px'><h2>Mapa OptiLive</h2><p>Configura la dirección del servidor para ver los barcos en directo.</p></body>","text/html","UTF-8")
  findViewById<TextView>(R.id.backTracking).setOnClickListener{finish()}
  findViewById<android.view.View>(R.id.trackingNav).setOnClickListener{finish()}
  findViewById<TextView>(R.id.zoomIn).setOnClickListener{web.zoomIn()}
  findViewById<TextView>(R.id.zoomOut).setOnClickListener{web.zoomOut()}
  findViewById<TextView>(R.id.centerMap).setOnClickListener{web.reload()}
 }
}