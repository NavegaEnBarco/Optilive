package com.optilive.tracker
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class TrackingActivity:AppCompatActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_tracking);findViewById<TextView>(R.id.subtitle).text="Seguimiento de flota";findViewById<TextView>(R.id.navTracking).setTextColor(android.graphics.Color.parseColor("#1689E8"));val w=findViewById<WebView>(R.id.contentWeb);w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true;w.webViewClient=android.webkit.WebViewClient();val server=(getSharedPreferences("optilive_profile",MODE_PRIVATE).getString("server_url",MainActivity.DEFAULT_SERVER)?:MainActivity.DEFAULT_SERVER).trimEnd('/');w.loadUrl(server+"/fleet.html");findViewById<TextView>(R.id.trackingOptions).setOnClickListener{startActivity(Intent(this,MainActivity::class.java))};findViewById<TextView>(R.id.back).setOnClickListener{finish()};findViewById<TextView>(R.id.navMap).setOnClickListener{startActivity(Intent(this,MapActivity::class.java));finish()};findViewById<TextView>(R.id.navRanking).setOnClickListener{startActivity(Intent(this,ClassificationActivity::class.java));finish()};findViewById<TextView>(R.id.navOptions).setOnClickListener{startActivity(Intent(this,MainActivity::class.java));finish()}}

}
