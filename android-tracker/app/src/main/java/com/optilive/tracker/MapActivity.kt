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
  findViewById<TextView>(R.id.raceClub).text=if(club.isBlank()) "En directo" else club
  val web=findViewById<WebView>(R.id.webMap)
  web.settings.javaScriptEnabled=true
  web.settings.domStorageEnabled=true
  web.settings.mixedContentMode=WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
  web.settings.setSupportZoom(true); web.settings.builtInZoomControls=false
  web.webViewClient=object:WebViewClient(){
   override fun onReceivedError(v:WebView?,r:WebResourceRequest?,e:WebResourceError?){if(r?.isForMainFrame==true) showOffline(web,url)}
   override fun onReceivedHttpError(v:WebView?,r:WebResourceRequest?,e:WebResourceResponse?){if(r?.isForMainFrame==true) showOffline(web,url)}
  }
  if(url.isNotBlank()) web.loadUrl(url) else showOffline(web,url)
  findViewById<TextView>(R.id.backTracking).setOnClickListener{finish()}
  findViewById<android.view.View>(R.id.trackingNav).setOnClickListener{finish()}
  findViewById<TextView>(R.id.zoomIn).setOnClickListener{web.evaluateJavascript("if(window.map){map.zoomIn();}",null)}
  findViewById<TextView>(R.id.zoomOut).setOnClickListener{web.evaluateJavascript("if(window.map){map.zoomOut();}",null)}
  findViewById<TextView>(R.id.centerMap).setOnClickListener{web.evaluateJavascript("if(window.map){map.locate({setView:true,maxZoom:17});}",null)}
 }
 private fun showOffline(web:WebView,url:String){
  val target=if(url.isBlank()) "Servidor no configurado" else url
  web.loadDataWithBaseURL(null,"""<html><body style='margin:0;background:#d9edf5;font-family:sans-serif;display:flex;height:100vh;align-items:center;justify-content:center;text-align:center'><div><div style='font-size:58px'>⛵</div><h2 style='color:#07558a'>Mapa OptiLive</h2><p style='color:#526777'>No se puede conectar al servidor.</p><p style='color:#07558a'><b>$target</b></p><p>El móvil y el ordenador deben estar en la misma red y el servidor OptiLive iniciado.</p></div></body></html>""","text/html","UTF-8",null)
 }
}