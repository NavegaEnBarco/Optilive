package com.optilive.tracker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var coordinates: TextView
    private lateinit var serverStatus: TextView
    private lateinit var telemetry: TextView
    private lateinit var sailDisplay: TextView
    private lateinit var sailorDisplay: TextView
    private lateinit var speedDisplay: TextView
    private lateinit var distanceDisplay: TextView
    private lateinit var sentDisplay: TextView
    private lateinit var pendingDisplay: TextView
    private lateinit var latDisplay: TextView
    private lateinit var lonDisplay: TextView
    private lateinit var accuracyDisplay: TextView
    private lateinit var lastPosition: TextView
    private lateinit var satelliteDisplay: TextView

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val lat = intent?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lon = intent?.getDoubleExtra("lon", 0.0) ?: 0.0
            val acc = intent?.getFloatExtra("accuracy", 0f) ?: 0f
            val satellites = intent?.getIntExtra("satellites", 0) ?: 0
            satelliteDisplay.text = "$satellites sat."
            status.text = "●  GPS ACTIVO\nPrecisión %.1f m".format(acc)
            val speed = intent?.getFloatExtra("speed", 0f) ?: 0f
            val distance = intent?.getFloatExtra("distance", 0f) ?: 0f
            coordinates.text = ""
            latDisplay.text = "Latitud\n%.6f".format(lat)
            lonDisplay.text = "Longitud\n%.6f".format(lon)
            accuracyDisplay.text = "Precisión\n%.1f m".format(acc)
            lastPosition.text = "Última posición: " + java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            telemetry.text = ""
            speedDisplay.text = "%.1f\nnudos\nVelocidad".format(speed)
            distanceDisplay.text = if (distance >= 1000f) "%.1f\nkm\nDistancia recorrida".format(distance / 1000f) else "%.0f\nm\nDistancia recorrida".format(distance)
        }
    }

    private val serverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connected = intent?.getBooleanExtra("connected", false) ?: false
            val sent = intent?.getIntExtra("sent", 0) ?: 0
            val pending = intent?.getIntExtra("pending", 0) ?: 0
            val serverUrl = getSharedPreferences("optilive_profile", MODE_PRIVATE).getString("server_url", DEFAULT_SERVER) ?: DEFAULT_SERVER
            serverStatus.text = if (connected) "●  Servidor conectado\n$serverUrl" else "●  Servidor sin conexión\n$serverUrl"
            sentDisplay.text = "$sent\nposiciones enviadas"
            pendingDisplay.text = "$pending\npendientes (offline)"
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) startGps()
        else status.text = "Permiso de ubicación necesario"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        coordinates = findViewById(R.id.coordinates)
        serverStatus = findViewById(R.id.serverStatus)
        telemetry = findViewById(R.id.telemetry)
        sailDisplay = findViewById(R.id.sailDisplay)
        sailorDisplay = findViewById(R.id.sailorDisplay)
        speedDisplay = findViewById(R.id.speedDisplay)
        distanceDisplay = findViewById(R.id.distanceDisplay)
        sentDisplay = findViewById(R.id.sentDisplay)
        pendingDisplay = findViewById(R.id.pendingDisplay)
        latDisplay = findViewById(R.id.latDisplay)
        lonDisplay = findViewById(R.id.lonDisplay)
        accuracyDisplay = findViewById(R.id.accuracyDisplay)
        lastPosition = findViewById(R.id.lastPosition)
        satelliteDisplay = findViewById(R.id.satelliteDisplay)

        val sail = findViewById<EditText>(R.id.sailNumber)
        val first = findViewById<EditText>(R.id.firstName)
        val last = findViewById<EditText>(R.id.lastName)
        val category = findViewById<Spinner>(R.id.category)
        val categoryOptions = arrayOf("Sub11", "Sub13", "Sub15")
        category.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
        val club = findViewById<EditText>(R.id.club)
        val serverUrl = findViewById<EditText>(R.id.serverUrl)
        val prefs = getSharedPreferences("optilive_profile", MODE_PRIVATE)
        sail.setText(prefs.getString("sail", ""))
        first.setText(prefs.getString("first", ""))
        last.setText(prefs.getString("last", ""))
        val savedCategory = prefs.getString("category", "Sub11") ?: "Sub11"
        category.setSelection(categoryOptions.indexOf(savedCategory).takeIf { it >= 0 } ?: 0)
        club.setText(prefs.getString("club", ""))
        if ((prefs.getString("server_url", "") ?: "").isBlank()) prefs.edit().putString("server_url", DEFAULT_SERVER).apply()\n        serverUrl.setText(prefs.getString("server_url", DEFAULT_SERVER))
        fun refreshProfile() {
            val rawSail = sail.text.toString().trim().uppercase()
            val normalizedSail = when {
                rawSail.isBlank() -> "ESP ----"
                rawSail.startsWith("ESP") -> rawSail
                else -> "ESP $rawSail"
            }
            sailDisplay.text = "🇪🇸  $normalizedSail"
            val fullName = "${first.text} ${last.text}".trim().ifBlank { "Regatista" }
            val meta = listOf(category.selectedItem?.toString().orEmpty(), club.text.toString()).filter { it.isNotBlank() }.joinToString("   |   ")
            sailorDisplay.text = if (meta.isBlank()) fullName else "$fullName\n$meta"
        }
        refreshProfile()

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            prefs.edit().putString("sail", sail.text.toString().trim()).putString("first", first.text.toString().trim())
                .putString("last", last.text.toString().trim()).putString("category", category.selectedItem?.toString().orEmpty())
                .putString("club", club.text.toString().trim()).putString("server_url", serverUrl.text.toString().trim().ifBlank { DEFAULT_SERVER }).apply()
            refreshProfile()
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.mapButton).setOnClickListener { startActivity(Intent(this, MapActivity::class.java)) }
        findViewById<android.view.View>(R.id.settingsButton).setOnClickListener {
            val p = findViewById<android.view.View>(R.id.settingsPanel)
            p.visibility = if (p.visibility == android.view.View.VISIBLE) android.view.View.GONE else android.view.View.VISIBLE
        }
        findViewById<android.view.View>(R.id.homeButton).setOnClickListener { findViewById<android.view.View>(R.id.settingsPanel).visibility = android.view.View.GONE }
        findViewById<android.view.View>(R.id.topSettings).setOnClickListener { findViewById<android.view.View>(R.id.settingsPanel).visibility = android.view.View.VISIBLE }
        findViewById<android.view.View>(R.id.racesButton).setOnClickListener { Toast.makeText(this, "Regatas: siguiente módulo", Toast.LENGTH_SHORT).show() }
        findViewById<android.view.View>(R.id.historyButton).setOnClickListener { Toast.makeText(this, "Historial: siguiente módulo", Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.startTracking).setOnClickListener { requestAndStart() }
        findViewById<android.view.View>(R.id.stopTracking).setOnClickListener {
            stopService(Intent(this, LocationService::class.java))
            status.text = "●  GPS DETENIDO\nSeguimiento parado"
        }
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(this, locationReceiver, IntentFilter(LocationService.ACTION_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED)
        ContextCompat.registerReceiver(this, serverReceiver, IntentFilter(LocationService.ACTION_SERVER), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        unregisterReceiver(locationReceiver)
        unregisterReceiver(serverReceiver)
        super.onStop()
    }

    private fun requestAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) startGps()
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 20)
    }

    companion object { const val DEFAULT_SERVER = "https://optilive-node-production.up.railway.app" }\n\n    private fun startGps() {
        ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
        status.text = "●  GPS INICIANDO\nBuscando posición..."
    }
}
