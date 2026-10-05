package com.optilive.tracker
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class AdminCourseMapActivity:AppCompatActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b)
  val w=WebView(this);setContentView(w);w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true;w.webViewClient=WebViewClient()
  val prefs=getSharedPreferences("optilive_admin",MODE_PRIVATE);val token=prefs.getString("token","")?:"";val rid=intent.getStringExtra("regattaId")?:prefs.getString("selected_regatta_id","")?:""
  w.loadUrl("https://optilive-node-production.up.railway.app/admin-course.html?regatta="+java.net.URLEncoder.encode(rid,"UTF-8")+"&token="+java.net.URLEncoder.encode(token,"UTF-8"))
 }
}