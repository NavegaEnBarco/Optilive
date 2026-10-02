package com.optilive.tracker
import android.os.Bundle
import android.webkit.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MapActivity: AppCompatActivity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState); setContentView(R.layout.activity_map)
  val prefs=getSharedPreferences("optilive_profile",MODE_PRIVATE)
  var url=(prefs.getString("server_url","") ?: "").trim()
  if(url.isNotBlank() && !url.startsWith("http://") && !url.startsWith("https://")) url="http://$url"
  val club=prefs.getString("club","") ?: ""
  findViewById<TextView>(R.id.raceName).text="Trofeo Fornells 2026"
  findViewById<TextView>(R.id.raceClub).text=if(club.isBlank()) "C.M. Mahón" else club
  findViewById<TextView>(R.id.liveStatus).text=if(url.isBlank()) "● Sin conexión" else "● En directo"
  val web=findViewById<WebView>(R.id.webMap)
  web.settings.javaScriptEnabled=true; web.settings.domStorageEnabled=true
  web.settings.mixedContentMode=WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
  web.webViewClient=object:WebViewClient(){override fun onReceivedError(v:WebView?,r:WebResourceRequest?,e:WebResourceError?){if(r?.isForMainFrame==true) loadDemo(web)}}
  if(url.isBlank()) loadDemo(web) else web.loadUrl(url)
  findViewById<TextView>(R.id.backTracking).setOnClickListener{finish()}
  findViewById<android.view.View>(R.id.trackingNav).setOnClickListener{finish()}
  findViewById<TextView>(R.id.zoomIn).setOnClickListener{web.evaluateJavascript("if(window.map){map.zoomIn();}",null)}
  findViewById<TextView>(R.id.zoomOut).setOnClickListener{web.evaluateJavascript("if(window.map){map.zoomOut();}",null)}
  findViewById<TextView>(R.id.centerMap).setOnClickListener{web.evaluateJavascript("if(window.map&&window.focusLive){window.focusLive();}",null)}
 }
 private fun loadDemo(web:WebView){
  web.loadUrl("https://optilive-node-production.up.railway.app")
 }
}