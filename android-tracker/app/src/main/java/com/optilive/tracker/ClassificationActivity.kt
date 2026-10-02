package com.optilive.tracker
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClassificationActivity:AppCompatActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_classification)
  findViewById<TextView>(R.id.subtitle).text="Clasificación en directo"
  findViewById<TextView>(R.id.navRanking).setTextColor(android.graphics.Color.parseColor("#1689E8"))
  val w=findViewById<WebView>(R.id.contentWeb);w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true
  w.loadDataWithBaseURL("https://optilive-node-production.up.railway.app",html(),"text/html","UTF-8",null)
  findViewById<TextView>(R.id.back).setOnClickListener{finish()}
  findViewById<TextView>(R.id.navMap).setOnClickListener{startActivity(Intent(this,MapActivity::class.java));finish()}
  findViewById<TextView>(R.id.navTracking).setOnClickListener{startActivity(Intent(this,TrackingActivity::class.java));finish()}
  findViewById<TextView>(R.id.navOptions).setOnClickListener{startActivity(Intent(this,MainActivity::class.java));finish()}
 }
 private fun html()="""<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><style>*{box-sizing:border-box}body{margin:0;background:#f4f7fa;font-family:Arial;color:#172433;padding:12px}.race,.card{background:#fff;border-radius:16px;box-shadow:0 2px 12px #16334b18}.race{padding:15px;margin-bottom:12px}.title{font-size:18px;font-weight:800}.sub{color:#6a7887;font-size:13px;margin-top:4px}.live{float:right;color:#0aa573;font-size:12px;font-weight:800}.tabs{display:flex;background:#e9eef3;border-radius:12px;padding:4px;margin:12px 0}.tab{flex:1;text-align:center;padding:9px;font-size:12px}.sel{background:#fff;color:#087fe3;border-radius:9px;font-weight:800}.head,.row{display:grid;grid-template-columns:38px 1fr 66px 48px;align-items:center}.head{padding:8px 12px;color:#7a8795;font-size:10px}.row{min-height:68px;padding:8px 12px;border-top:1px solid #edf1f4}.pos{font-size:18px;font-weight:800}.sail{font-weight:800}.meta{font-size:11px;color:#788695;margin-top:3px}.gap{text-align:right;font-size:12px;font-weight:700}.mark{text-align:right;color:#0aa573;font-size:11px}.empty{padding:28px;text-align:center;color:#748292}.dot{display:inline-block;width:8px;height:8px;border-radius:50%;background:#0aae78;margin-right:5px}</style></head><body><div class='race'><span class='live'><span class='dot'></span>EN DIRECTO</span><div class='title'>Trofeo Fornells 2026</div><div class='sub'>C.M. Mahón · Prueba 2/4</div></div><div class='tabs'><div class='tab sel'>GENERAL</div><div class='tab'>SUB 11</div><div class='tab'>SUB 13</div><div class='tab'>SUB 15</div></div><div class='card'><div class='head'><b>POS.</b><b>REGATISTA</b><b>DIF.</b><b>BALIZA</b></div><div id='rows'><div class='empty'>Conectando con los barcos…</div></div></div><script>
async function load(){try{let r=await fetch('/api/boats');let b=await r.json();b.sort((a,z)=>(z.timestamp||0)-(a.timestamp||0));let h='';b.forEach((p,i)=>{let age=Math.max(0,Math.round((Date.now()-(p.receivedAt||p.timestamp||Date.now()))/1000));h+=`<div class="row"><div class="pos">${i+1}</div><div><div class="sail">${p.sail}</div><div class="meta">Última señal hace ${age}s · ${(p.speedKnots||0).toFixed(1)} kn</div></div><div class="gap">${i==0?'LÍDER':'LIVE'}</div><div class="mark">● GPS</div></div>`});document.getElementById('rows').innerHTML=h||'<div class="empty">Esperando posiciones en directo</div>'}catch(e){document.getElementById('rows').innerHTML='<div class="empty">Sin conexión con el servidor</div>'}}load();setInterval(load,3000)</script></body></html>"""
}