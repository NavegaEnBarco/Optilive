package com.optilive.tracker

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState); setContentView(R.layout.activity_admin)
  val user=findViewById<EditText>(R.id.adminUser); val pass=findViewById<EditText>(R.id.adminPass)
  val msg=findViewById<TextView>(R.id.adminMessage); val panel=findViewById<View>(R.id.adminPanel)
  findViewById<Button>(R.id.adminLogin).setOnClickListener {
   if(user.text.isBlank()||pass.text.isBlank()){ msg.text="Introduce usuario y contraseña"; return@setOnClickListener }
   // Pantalla preparada para autenticar contra el servidor. Nunca se guardan contraseñas en la APK.
   msg.text="Servidor de autenticación pendiente de activar"; panel.visibility=View.GONE
  }
  findViewById<Button>(R.id.newRegatta).setOnClickListener { Toast.makeText(this,"Crear regata: siguiente módulo",Toast.LENGTH_SHORT).show() }
  findViewById<Button>(R.id.manageRaces).setOnClickListener { Toast.makeText(this,"Días y pruebas: siguiente módulo",Toast.LENGTH_SHORT).show() }
  findViewById<Button>(R.id.manageCourse).setOnClickListener { Toast.makeText(this,"Boyas GPS: siguiente módulo",Toast.LENGTH_SHORT).show() }
 }
}