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
        val country = findViewById<TextView>(R.id.entryCountry)
        var countryCode = "ESP"
        country.setOnClickListener {
            val codes = arrayOf("ESP", "POR", "FRA", "ITA", "GBR", "GER", "NED", "BEL", "USA")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("País de la vela")
                .setItems(codes) { _, index ->
                    countryCode = codes[index]
                    country.text = "$"+"{countryCode}  ⌄"
                }.show()
        }
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
            val savedSail = prefs.getString("sail", "").orEmpty().trim()
            val match = Regex("^([A-Za-z]{3})\\\\s+(.*)$").find(savedSail)
            if (match != null) {
                countryCode = match.groupValues[1].uppercase()
                sail.setText(match.groupValues[2])
            } else sail.setText(savedSail.filter { it.isDigit() })
            country.text = "$"+"{countryCode}  ⌄"
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
            prefs.edit().putString("sail", countryCode + " " + sail.text.toString().trim())
                .putString("first", first.text.toString().trim()).putString("last", last.text.toString().trim())
                .putString("club", club.text.toString().trim()).putString("category", category.selectedItem.toString()).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun showCategoryPicker(spinner: Spinner, options: Array<String>) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Material_NoActionBar_Fullscreen)
        val root = createPickerRoot("Categoría")
        val content = (root.getChildAt(1) as LinearLayout).getChildAt(2) as LinearLayout
        val selected = intArrayOf(spinner.selectedItemPosition.coerceAtLeast(0))
        options.forEachIndexed { index, name ->
            val item = pickerOption(name, index == selected[0])
            content.addView(item, LinearLayout.LayoutParams(-1, dp(76)).apply { bottomMargin = dp(12) })
            item.setOnClickListener {
                selected[0] = index
                for (i in 0 until options.size) {
                    (content.getChildAt(i) as TextView).background = optionBackground(i == index)
                    (content.getChildAt(i) as TextView).text = options[i] + if (i == index) "    ✓" else "    ○"
                }
            }
        }
        addConfirm(root) { spinner.setSelection(selected[0]); dialog.dismiss() }
        dialog.setContentView(root)
        dialog.show()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun optionBackground(active: Boolean): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            if (active) intArrayOf(Color.rgb(2, 83, 150), Color.rgb(0, 32, 67))
            else intArrayOf(Color.rgb(3, 27, 53), Color.rgb(2, 19, 38))
        ).apply {
            cornerRadius = dp(15).toFloat()
            setStroke(dp(1), if (active) Color.rgb(47, 189, 255) else Color.rgb(28, 75, 115))
        }
    }

    private fun pickerOption(label: String, active: Boolean): TextView =
        TextView(this).apply {
            text = label + if (active) "    ✓" else "    ○"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(22), 0, dp(14), 0)
            background = optionBackground(active)
        }

    private fun createPickerRoot(title: String): android.widget.FrameLayout {
        val frame = android.widget.FrameLayout(this)
        frame.addView(XBoatWaveView(this), android.widget.FrameLayout.LayoutParams(-1, -1))
        val vertical = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(50), dp(24), dp(30))
        }
        frame.addView(vertical, android.widget.FrameLayout.LayoutParams(-1, -1))
        vertical.addView(TextView(this).apply {
            text = "✦  X  B O A T"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.WHITE)
        })
        vertical.addView(TextView(this).apply {
            text = title
            textSize = 25f
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, dp(22), 0, dp(22))
        })
        val options = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        vertical.addView(options, LinearLayout.LayoutParams(-1, 0, 1f))
        return frame
    }

    private fun addConfirm(root: android.widget.FrameLayout, action: () -> Unit) {
        val vertical = root.getChildAt(1) as LinearLayout
        vertical.addView(Button(this).apply {
            text = "Confirmar"
            textSize = 19f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = androidx.core.content.ContextCompat.getDrawable(this@ProfileActivity, R.drawable.xboat_blue_button)
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, dp(60)))
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
