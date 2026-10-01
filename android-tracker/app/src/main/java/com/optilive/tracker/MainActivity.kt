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

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val lat = intent?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lon = intent?.getDoubleExtra("lon", 0.0) ?: 0.0
            val acc = intent?.getFloatExtra("accuracy", 0f) ?: 0f
            status.text = "GPS ACTIVO"
            val speed = intent?.getFloatExtra("speed", 0f) ?: 0f
            val distance = intent?.getFloatExtra("distance", 0f) ?: 0f
            coordinates.text = "Lat: %.6f\nLon: %.6f\nPrecisión: %.1f m".format(lat, lon, acc)
            telemetry.text = "VELOCIDAD  %.1f kn     DISTANCIA  %.0f m".format(speed, distance)
        }
    }

    private val serverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connected = intent?.getBooleanExtra("connected", false) ?: false
            val sent = intent?.getIntExtra("sent", 0) ?: 0
            val pending = intent?.getIntExtra("pending", 0) ?: 0
            serverStatus.text = if (connected) "🟢 Servidor conectado · Enviadas: $sent · Pendientes: $pending"
                else "🔴 Sin conexión · Guardadas: $pending"
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

        val sail = findViewById<EditText>(R.id.sailNumber)
        val first = findViewById<EditText>(R.id.firstName)
        val last = findViewById<EditText>(R.id.lastName)
        val category = findViewById<EditText>(R.id.category)
        val club = findViewById<EditText>(R.id.club)
        val serverUrl = findViewById<EditText>(R.id.serverUrl)
        val prefs = getSharedPreferences("optilive_profile", MODE_PRIVATE)
        sail.setText(prefs.getString("sail", ""))
        first.setText(prefs.getString("first", ""))
        last.setText(prefs.getString("last", ""))
        category.setText(prefs.getString("category", ""))
        club.setText(prefs.getString("club", ""))
        serverUrl.setText(prefs.getString("server_url", ""))
        fun refreshProfile() {
            sailDisplay.text = (sail.text.toString().ifBlank { "ESP ----" }).uppercase()
            sailorDisplay.text = "${first.text} ${last.text} · ${category.text} · ${club.text}"
        }
        refreshProfile()

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            prefs.edit().putString("sail", sail.text.toString().trim()).putString("first", first.text.toString().trim())
                .putString("last", last.text.toString().trim()).putString("category", category.text.toString().trim())
                .putString("club", club.text.toString().trim()).putString("server_url", serverUrl.text.toString().trim()).apply()
            refreshProfile()
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.startTracking).setOnClickListener { requestAndStart() }
        findViewById<Button>(R.id.stopTracking).setOnClickListener {
            stopService(Intent(this, LocationService::class.java))
            status.text = "GPS detenido"
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

    private fun startGps() {
        ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
        status.text = "Iniciando GPS..."
    }
}
