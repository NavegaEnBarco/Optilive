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
  findViewById<TextView>(R.id.raceName).text="Trofeo OptiLive · Prueba"
  findViewById<TextView>(R.id.raceClub).text=if(club.isBlank()) "C.M. Mahón · DEMO" else "$club · DEMO"
  findViewById<TextView>(R.id.liveStatus).text=if(url.isBlank()) "● DEMO" else "● LIVE"
  val web=findViewById<WebView>(R.id.webMap)
  web.settings.javaScriptEnabled=true; web.settings.domStorageEnabled=true
  web.settings.mixedContentMode=WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
  web.webViewClient=object:WebViewClient(){override fun onReceivedError(v:WebView?,r:WebResourceRequest?,e:WebResourceError?){if(r?.isForMainFrame==true) loadDemo(web)}}
  if(url.isBlank()) loadDemo(web) else web.loadUrl(url)
  findViewById<TextView>(R.id.backTracking).setOnClickListener{finish()}
  findViewById<android.view.View>(R.id.trackingNav).setOnClickListener{finish()}
  findViewById<TextView>(R.id.zoomIn).setOnClickListener{web.evaluateJavascript("if(window.map){map.zoomIn();}",null)}
  findViewById<TextView>(R.id.zoomOut).setOnClickListener{web.evaluateJavascript("if(window.map){map.zoomOut();}",null)}
  findViewById<TextView>(R.id.centerMap).setOnClickListener{web.evaluateJavascript("if(window.map){map.setView([39.865,4.305],15);}",null)}
 }
 private fun loadDemo(web:WebView){
  val html="""<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'><style>html,body,#map{height:100%;margin:0}.leaflet-control-attribution{font-size:8px}.lbl{background:#087fe3;color:white;border:0;border-radius:6px;padding:4px 7px;font-weight:bold}.boat{font-size:25px;text-shadow:0 1px 3px #000}.mark{background:#ff493d;color:#fff;border:2px solid #fff;border-radius:50%;font-weight:bold;text-align:center;line-height:22px;width:22px;height:22px}</style></head><body><div id='map'></div><script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script><script>
  window.map=L.map('map',{zoomControl:false}).setView([39.865,4.305],15);
  L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',{maxZoom:19}).addTo(map);
  const course=[[39.8615,4.298],[39.871,4.301],[39.868,4.317],[39.8585,4.313],[39.8615,4.298]];
  L.polyline(course,{color:'white',weight:2,dashArray:'7 7',opacity:.95}).addTo(map);
  course.slice(0,4).forEach((p,i)=>L.marker(p,{icon:L.divIcon({className:'',html:'<div class="mark">'+(i+1)+'</div>',iconSize:[26,26]})}).addTo(map));
  const boats=[['ESP 1606',39.8642,4.307,'#1689e8'],['ESP 1784',39.8630,4.310,'#14a56d'],['ESP 1512',39.8657,4.3115,'#ff8b22'],['ESP 1720',39.8618,4.305,'#ec3f46'],['ESP 1668',39.8605,4.309,'#9b59d0'],['ESP 1811',39.8665,4.309,'#f0d21a']];
  boats.forEach((b,i)=>{const start=[b[1]-.004-i*.00025,b[2]-.004+i*.0002];L.polyline([start,[b[1]-.002,b[2]-.001], [b[1],b[2]]],{color:b[3],weight:4,opacity:.9}).addTo(map);const m=L.marker([b[1],b[2]],{icon:L.divIcon({className:'',html:'<div class="boat" style="color:'+b[3]+'">▲</div>',iconSize:[28,28]})}).addTo(map);if(i===0)m.bindTooltip(b[0],{permanent:true,direction:'right',className:'lbl'});});
  L.polyline([[39.8605,4.296],[39.8588,4.301]],{color:'white',weight:5}).addTo(map);L.marker([39.8595,4.2985],{icon:L.divIcon({className:'',html:'<div style="font-size:24px">🏁</div>'})}).addTo(map);
  </script></body></html>"""
  web.loadDataWithBaseURL("https://unpkg.com/",html,"text/html","UTF-8",null)
 }
}