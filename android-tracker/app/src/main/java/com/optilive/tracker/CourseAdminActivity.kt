package com.optilive.tracker
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
class CourseAdminActivity:AppCompatActivity(){
 private val server="https://optilive-node-production.up.railway.app"
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_course_admin)
  val type=findViewById<Spinner>(R.id.markType);type.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Salida babor","Salida estribor","Barlovento","Offset","Sotavento babor","Sotavento estribor","Llegada"))
  findViewById<Button>(R.id.assignMark).setOnClickListener{assign()}
 }
 private fun assign(){val race=findViewById<EditText>(R.id.regattaId).text.toString().trim();val dev=findViewById<EditText>(R.id.markDevice).text.toString().trim();val name=findViewById<EditText>(R.id.markName).text.toString().trim();val type=findViewById<Spinner>(R.id.markType).selectedItem.toString();val out=findViewById<TextView>(R.id.markResult)
  if(race.isBlank()||dev.isBlank()||name.isBlank()){out.text="Completa regata, GPS y nombre";return};val token=getSharedPreferences("optilive_admin",MODE_PRIVATE).getString("token","")?:""
  thread{try{val c=URL("$server/api/admin/regattas/$race/marks").openConnection() as HttpURLConnection;c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Authorization","Bearer $token");c.doOutput=true;c.outputStream.use{it.write(JSONObject().put("deviceId",dev).put("name",name).put("type",type).toString().toByteArray())};val code=c.responseCode;runOnUiThread{out.text=if(code==201)"✓ $dev asignado a $name" else "Error $code"}}catch(e:Exception){runOnUiThread{out.text="Error de conexión"}}}
 }
}