package com.optilive.tracker
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
class RaceAdminActivity:AppCompatActivity(){
 private val server="https://optilive-node-production.up.railway.app"
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_race_admin);val s=findViewById<Spinner>(R.id.raceStatus);s.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Pendiente","En curso","Finalizada"));findViewById<Button>(R.id.saveRace).setOnClickListener{save()}}
 private fun save(){val rid=findViewById<EditText>(R.id.raceRegattaId).text.toString().trim();val day=findViewById<EditText>(R.id.raceDay).text.toString().toIntOrNull();val num=findViewById<EditText>(R.id.raceNumber).text.toString().toIntOrNull();val status=findViewById<Spinner>(R.id.raceStatus).selectedItem.toString();val out=findViewById<TextView>(R.id.raceResult);if(rid.isBlank()||day==null||num==null){out.text="Completa regata, día y prueba";return};val token=getSharedPreferences("optilive_admin",MODE_PRIVATE).getString("token","")?:"";thread{try{val c=URL("$server/api/admin/regattas/$rid/races").openConnection() as HttpURLConnection;c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Authorization","Bearer $token");c.doOutput=true;c.outputStream.use{it.write(JSONObject().put("day",day).put("number",num).put("status",status).toString().toByteArray())};val code=c.responseCode;runOnUiThread{out.text=if(code==201)"✓ Día $day · Prueba $num · $status" else "Error $code"}}catch(e:Exception){runOnUiThread{out.text="Error de conexión"}}}}
}