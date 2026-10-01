package com.optilive.tracker
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MapActivity: AppCompatActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContentView(R.layout.activity_map)
  val prefs=getSharedPreferences("optilive_profile",MODE_PRIVATE)
  val url=prefs.getString("server_url","") ?: ""
  val web=findViewById<WebView>(R.id.webMap)
  web.settings.javaScriptEnabled=true; web.settings.domStorageEnabled=true; web.webViewClient=WebViewClient()
  if(url.isNotBlank()) web.loadUrl(url) else web.loadData("<h2>Configura primero el servidor OptiLive</h2>","text/html","UTF-8")
  findViewById<Button>(R.id.backTracking).setOnClickListener{finish()}
 }
}