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
  val html="""<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'><style>
  html,body,#map{height:100%;margin:0}.leaflet-control-attribution{font-size:7px}.lbl{background:#087fe3;color:#fff;border:0;border-radius:7px;padding:5px 9px;font-weight:800}.mark{background:#ff493d;color:#fff;border:2px solid #fff;border-radius:50%;font-weight:800;text-align:center;line-height:22px;width:22px;height:22px;box-shadow:0 2px 5px #0005}.opti{position:relative;width:25px;height:34px;filter:drop-shadow(0 2px 2px #0008)}.opti .hull{position:absolute;bottom:0;left:5px;width:16px;height:7px;background:#fff;border-radius:50% 50% 45% 45%;border:1px solid #345}.opti .mast{position:absolute;left:12px;top:2px;width:2px;height:26px;background:#eee}.opti .sail{position:absolute;left:13px;top:3px;width:0;height:0;border-top:22px solid var(--c);border-right:10px solid transparent}.opti .sail2{position:absolute;right:12px;top:8px;width:0;height:0;border-bottom:17px solid #f7f7f7;border-left:7px solid transparent}</style></head><body><div id='map'></div><script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script><script>
  window.map=L.map('map',{zoomControl:false,attributionControl:true}).setView([39.873,4.307],15);
  L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',{maxZoom:19}).addTo(map);
  const course=[[39.8672,4.3035],[39.8780,4.3040],[39.8785,4.3180],[39.8668,4.3185],[39.8672,4.3035]];
  L.polyline(course,{color:'#fff',weight:2,dashArray:'7 7',opacity:.95}).addTo(map);
  course.slice(0,4).forEach((p,i)=>L.marker(p,{icon:L.divIcon({className:'',html:'<div class="mark">'+(i+1)+'</div>',iconSize:[26,26],iconAnchor:[13,13]})}).addTo(map));
  const boats=[['ESP 1606',39.8715,4.3090,'#1689e8',-18],['ESP 1784',39.8720,4.3120,'#17a76b',-20],['ESP 1512',39.8735,4.3142,'#ff8b22',-16],['ESP 1720',39.8697,4.3108,'#ec3f46',-22],['ESP 1668',39.8705,4.3150,'#9b59d0',-14],['ESP 1811',39.8740,4.3105,'#f0d21a',-12]];
  function boatIcon(col,rot){return L.divIcon({className:'',html:'<div class="opti" style="--c:'+col+';transform:rotate('+rot+'deg)"><i class="mast"></i><i class="sail"></i><i class="sail2"></i><i class="hull"></i></div>',iconSize:[28,38],iconAnchor:[14,30]})}
  boats.forEach((b,i)=>{const a=[b[1]-.0036,b[2]-.0024],m=[b[1]-.0018,b[2]-.0012];L.polyline([a,m,[b[1],b[2]]],{color:b[3],weight:3,opacity:.88,smoothFactor:2}).addTo(map);const mk=L.marker([b[1],b[2]],{icon:boatIcon(b[3],b[4])}).addTo(map);if(i===0)mk.bindTooltip(b[0],{permanent:true,direction:'right',className:'lbl',offset:[12,-5]});});
  const startA=[39.8667,4.3055],startB=[39.8669,4.3100];L.polyline([startA,startB],{color:'#fff',weight:4}).addTo(map);L.marker(startB,{icon:L.divIcon({className:'',html:'<div style="font-size:22px">🏁</div>',iconSize:[26,26]})}).addTo(map);
  map.fitBounds(L.latLngBounds(course),{padding:[25,25]});
  </script></body></html>"""
  web.loadDataWithBaseURL("https://unpkg.com/",html,"text/html","UTF-8",null)
 }
}