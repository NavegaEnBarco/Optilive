package com.optilive.tracker
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class RegattasActivity:AppCompatActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_regattas)
 findViewById<TextView>(R.id.back).setOnClickListener{finish()}
 val w=findViewById<WebView>(R.id.web);w.webViewClient=WebViewClient();w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true
 w.loadUrl("https://optilive-node-production.up.railway.app/regattas.html")
 }
}
