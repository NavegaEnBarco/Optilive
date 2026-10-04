package com.optilive.tracker
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
class AdminActivity:AppCompatActivity(){
 private val server="https://optilive-node-production.up.railway.app"
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_admin)
  val user=findViewById<EditText>(R.id.adminUser);val pass=findViewById<EditText>(R.id.adminPass);val msg=findViewById<TextView>(R.id.adminMessage);val panel=findViewById<View>(R.id.adminPanel)
  findViewById<Button>(R.id.adminLogin).setOnClickListener{
   if(user.text.isBlank()||pass.text.isBlank()){msg.text="Introduce usuario y contraseña";return@setOnClickListener}
   msg.text="Conectando…";thread{try{val c=URL("$server/api/auth/login").openConnection() as HttpURLConnection;c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.doOutput=true;c.outputStream.use{it.write(JSONObject().put("username",user.text.toString()).put("password",pass.text.toString()).toString().toByteArray())};val ok=c.responseCode==200;val body=(if(ok)c.inputStream else c.errorStream).bufferedReader().readText();runOnUiThread{if(ok){val j=JSONObject(body);getSharedPreferences("optilive_admin",MODE_PRIVATE).edit().putString("token",j.getString("token")).putString("role",j.optString("role")).apply();pass.text.clear();msg.text="Administrador conectado";panel.visibility=View.VISIBLE}else msg.text="Usuario o contraseña incorrectos"}}catch(e:Exception){runOnUiThread{msg.text="No se puede conectar al servidor"}}}}
  findViewById<Button>(R.id.newRegatta).setOnClickListener{createRegatta()}
  findViewById<Button>(R.id.manageRaces).setOnClickListener{Toast.makeText(this,"Selecciona primero una regata",Toast.LENGTH_SHORT).show()}
  findViewById<Button>(R.id.manageCourse).setOnClickListener{startActivity(android.content.Intent(this,CourseAdminActivity::class.java))}
 }
 private fun createRegatta(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,8,32,0)}
  fun field(h:String)=EditText(this).apply{hint=h;box.addView(this)}
  val name=field("Nombre de la regata");val club=field("Club organizador");val venue=field("Lugar");val days=field("Pruebas por día, ej. 3,3")
  android.app.AlertDialog.Builder(this).setTitle("Nueva regata").setView(box).setNegativeButton("Cancelar",null).setPositiveButton("Crear"){_,_->postRegatta(name.text.toString(),club.text.toString(),venue.text.toString(),days.text.toString())}.show()
 }
 private fun postRegatta(name:String,club:String,venue:String,days:String){if(name.isBlank()){Toast.makeText(this,"Falta el nombre",Toast.LENGTH_SHORT).show();return};val token=getSharedPreferences("optilive_admin",MODE_PRIVATE).getString("token","")?:"";thread{try{val arr=org.json.JSONArray();days.split(",").mapNotNull{it.trim().toIntOrNull()}.forEachIndexed{i,n->arr.put(JSONObject().put("day",i+1).put("maxRaces",n))};val c=URL("$server/api/admin/regattas").openConnection() as HttpURLConnection;c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Authorization","Bearer $token");c.doOutput=true;c.outputStream.use{it.write(JSONObject().put("name",name).put("club",club).put("venue",venue).put("days",arr).toString().toByteArray())};val code=c.responseCode;runOnUiThread{Toast.makeText(this,if(code==201)"Regata creada" else "Error al crear regata ($code)",Toast.LENGTH_LONG).show()}}catch(e:Exception){runOnUiThread{Toast.makeText(this,"Error de conexión",Toast.LENGTH_SHORT).show()}}}}
}