package com.optilive.tracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sail = findViewById<EditText>(R.id.sailNumber)
        val first = findViewById<EditText>(R.id.firstName)
        val last = findViewById<EditText>(R.id.lastName)
        val category = findViewById<EditText>(R.id.category)
        val club = findViewById<EditText>(R.id.club)
        val status = findViewById<TextView>(R.id.status)
        val prefs = getSharedPreferences("optilive_profile", MODE_PRIVATE)
        sail.setText(prefs.getString("sail", ""))
        first.setText(prefs.getString("first", ""))
        last.setText(prefs.getString("last", ""))
        category.setText(prefs.getString("category", ""))
        club.setText(prefs.getString("club", ""))
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            prefs.edit().putString("sail", sail.text.toString().trim()).putString("first", first.text.toString().trim()).putString("last", last.text.toString().trim()).putString("category", category.text.toString().trim()).putString("club", club.text.toString().trim()).apply()
            status.text = "Perfil guardado · " + sail.text.toString()
        }
    }
}
