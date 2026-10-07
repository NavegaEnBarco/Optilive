package com.optilive.tracker
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.GeolocationPermissions
import androidx.appcompat.app.AppCompatActivity
import java.net.URLEncoder
class RaceAdminActivity:AppCompatActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b)
 val prefs=getSharedPreferences("optilive_admin",MODE_PRIVATE)
 val token=prefs.getString("token","")?:""
 val rid=intent.getStringExtra("regattaId")?:prefs.getString("selected_regatta_id","")?:""
 val w=WebView(this);setContentView(w);w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true;w.webViewClient=WebViewClient();w.settings.setGeolocationEnabled(true)
 w.webChromeClient=object:WebChromeClient(){override fun onGeolocationPermissionsShowPrompt(origin:String?,callback:GeolocationPermissions.Callback?){callback?.invoke(origin,origin=="https://optilive-node-production.up.railway.app/"||origin=="https://optilive-node-production.up.railway.app",false)}}
 w.loadUrl("https://optilive-node-production.up.railway.app/admin-races.html?regatta="+URLEncoder.encode(rid,"UTF-8")+"&token="+URLEncoder.encode(token,"UTF-8"))
 }
}
