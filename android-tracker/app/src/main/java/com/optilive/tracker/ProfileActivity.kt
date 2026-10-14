package com.optilive.tracker

import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
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
        val clubs = arrayOf(
            "C.M. Mahón", "C.N. Ciutadella", "C.N. Villacarlos",
            "C.N. Sa Ràpita", "R.C.N. Palma", "C.N. Arenal",
            "C.N. Portitxol", "C.N. Cala Gamba", "C.N. Fornells",
            "C.N. S'Estanyol", "C.N. S'Arenal", "C.N. Can Pastilla",
            "C.N. Ibiza", "C.N. Santa Eulalia", "C.N. Sant Antoni",
            "C.N. Ciudadela", "R.C.N. Port de Pollença"
        ).distinct().sorted()
        club.setOnClickListener { showClubPicker(club, clubs) }
        val options = arrayOf("Sub11", "Sub13", "Sub15")
        category.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        category.setOnTouchListener { _, _ ->
            showCategoryPicker(category, options)
            true
        }
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
            if (club.text.toString().isBlank()) {
                club.error = "Selecciona tu club"
                return@setOnClickListener
            }
            prefs.edit().putString("sail", sail.text.toString().trim())
                .putString("first", first.text.toString().trim()).putString("last", last.text.toString().trim())
                .putString("club", club.text.toString().trim()).putString("category", category.selectedItem.toString()).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun showCategoryPicker(spinner: Spinner, options: Array<String>) {
        val selected = spinner.selectedItemPosition.coerceAtLeast(0)
        val group = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(28, 18, 28, 18)
            setBackgroundColor(Color.rgb(3, 19, 39))
        }
        options.forEachIndexed { index, option ->
            group.addView(RadioButton(this).apply {
                id = 1000 + index
                text = option
                textSize = 19f
                setTextColor(Color.WHITE)
                setPadding(14, 20, 14, 20)
                isChecked = index == selected
            })
        }
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Selecciona tu categoría")
            .setView(group)
            .setNegativeButton("Cancelar", null)
            .create()
        group.setOnCheckedChangeListener { _, checked ->
            spinner.setSelection(checked - 1000)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showClubPicker(target: EditText, clubs: List<String>) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 16, 22, 16)
            setBackgroundColor(Color.rgb(3, 19, 39))
        }
        val search = EditText(this).apply {
            hint = "Buscar club náutico..."
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
        }
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            addView(results)
        }
        container.addView(search)
        container.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, (360 * resources.displayMetrics.density).toInt()
        ))
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Selecciona tu club")
            .setView(container)
            .setNegativeButton("Volver", null)
            .create()
        fun render(query: String) {
            results.removeAllViews()
            clubs.filter { it.contains(query, ignoreCase = true) }.forEach { name ->
                results.addView(android.widget.TextView(this).apply {
                    text = name
                    textSize = 17f
                    setTextColor(Color.WHITE)
                    setPadding(18, 24, 18, 24)
                    setOnClickListener {
                        target.setText(name)
                        dialog.dismiss()
                    }
                })
            }
            if (results.childCount == 0) {
                results.addView(android.widget.TextView(this).apply {
                    text = "No se encontraron clubes"
                    setTextColor(Color.LTGRAY)
                    setPadding(18, 24, 18, 24)
                })
            }
        }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        render("")
        dialog.show()
    }

}
