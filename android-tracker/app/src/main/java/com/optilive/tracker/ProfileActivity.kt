package com.optilive.tracker

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val prefs = getSharedPreferences("optilive_profile", MODE_PRIVATE)
        val sail = findViewById<EditText>(R.id.entrySail)
        val first = findViewById<EditText>(R.id.entryFirst)
        val last = findViewById<EditText>(R.id.entryLast)
        val club = findViewById<EditText>(R.id.entryClub)
        val category = findViewById<Spinner>(R.id.entryCategory)
        val options = arrayOf("Sub11", "Sub13", "Sub15")
        category.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        if (savedInstanceState == null) {
            sail.setText(prefs.getString("sail", ""))
            first.setText(prefs.getString("first", ""))
            last.setText(prefs.getString("last", ""))
            club.setText(prefs.getString("club", ""))
            category.setSelection(options.indexOf(prefs.getString("category", "Sub11")).coerceAtLeast(0))
        }
        findViewById<Button>(R.id.entryContinue).setOnClickListener {
            val required = listOf(sail to "Introduce tu número de vela", first to "Introduce tu nombre", last to "Introduce tus apellidos")
            val missing = required.firstOrNull { it.first.text.toString().isBlank() }
            if (missing != null) {
                missing.first.error = missing.second
                missing.first.requestFocus()
                return@setOnClickListener
            }
            prefs.edit().putString("sail", sail.text.toString().trim())
                .putString("first", first.text.toString().trim()).putString("last", last.text.toString().trim())
                .putString("club", club.text.toString().trim()).putString("category", category.selectedItem.toString()).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
