package com.optilive.tracker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val uiHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshState = object : Runnable { override fun run() { refreshGpsStatus(); uiHandler.postDelayed(this, 5000L) } }
    private fun setTrackingUi(active:Boolean) {
        findViewById<Button>(R.id.startTracking).text=if(active) "■  DETENER SEGUIMIENTO" else "▶  INICIAR SEGUIMIENTO"
        findViewById<Button>(R.id.startTracking).backgroundTintList=android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(if(active) "#E34B55" else "#1689E8"))
        findViewById<TextView>(R.id.trackingState).text=if(active) "Seguimiento activo" else "Seguimiento detenido"
    }
    private fun refreshGpsStatus() {
        val p=getSharedPreferences("optilive_runtime",MODE_PRIVATE)
        val active=p.getBoolean("running",false)
        setTrackingUi(active)
        val age=System.currentTimeMillis()-p.getLong("fix_at",0L)
        val fresh=active && age in 0..15000
        val acc=p.getFloat("accuracy",0f)
        val good=fresh && acc>0f && acc<=15f
        status.text=if(!active) "GPS DETENIDO" else if(!fresh) "BUSCANDO SEÑAL GPS" else (if(good) "SEÑAL GPS BUENA" else "SEÑAL GPS DÉBIL")+"\nPrecisión %.1f m".format(acc)
        satelliteDisplay.text=if(fresh) "${p.getInt("satellites",0)} satélites" else "— satélites"
        status.setTextColor(android.graphics.Color.parseColor(if(good) "#087F5B" else if(fresh) "#996000" else "#6B7B8C"))
        findViewById<android.view.View>(R.id.gpsCard).setBackgroundResource(if(good)R.drawable.card_green else R.drawable.card_white)
    }
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
    private lateinit var profilePhoto: ImageView
    private lateinit var profileCover: ImageView
    private val coverPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let { try { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}; getSharedPreferences("optilive_profile", MODE_PRIVATE).edit().putString("cover_uri", it.toString()).apply(); profileCover.setImageURI(it); profileCover.setPadding(0,0,0,0) } }
    private val avatarPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let { try { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}; getSharedPreferences("optilive_profile", MODE_PRIVATE).edit().putString("avatar_uri", it.toString()).apply(); profilePhoto.setImageURI(it); profilePhoto.setPadding(0,0,0,0) } }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val lat = intent?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lon = intent?.getDoubleExtra("lon", 0.0) ?: 0.0
            val acc = intent?.getFloatExtra("accuracy", 0f) ?: 0f
            val satellites = intent?.getIntExtra("satellites", 0) ?: 0
            refreshGpsStatus()
            val speed = intent?.getFloatExtra("speed", 0f) ?: 0f
            val distance = intent?.getFloatExtra("distance", 0f) ?: 0f
            coordinates.text = ""
            latDisplay.text = "Latitud\n%.6f".format(lat)
            lonDisplay.text = "Longitud\n%.6f".format(lon)
            accuracyDisplay.text = "Precisión\n%.1f m".format(acc)
            lastPosition.text = "Última posición: " + java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(getSharedPreferences("optilive_runtime",MODE_PRIVATE).getLong("fix_at",System.currentTimeMillis())))
            telemetry.text = ""
            speedDisplay.text = "%.1f kn".format(speed)
            distanceDisplay.text = if (distance >= 1000f) "%.1f km".format(distance / 1000f) else "%.0f m".format(distance)
        }
    }

    private val serverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connected = intent?.getBooleanExtra("connected", false) ?: false
            val sent = intent?.getIntExtra("sent", 0) ?: 0
            val pending = intent?.getIntExtra("pending", 0) ?: 0
            val runtime=getSharedPreferences("optilive_runtime",MODE_PRIVATE)
            val syncAt=runtime.getLong("last_sync",0L)
            val stamp=if(syncAt>0)java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.getDefault()).format(java.util.Date(syncAt))else "—"
            val recent=System.currentTimeMillis()-runtime.getLong("connection_at",0L)<30000
            val title=if(runtime.getLong("connection_at",0L)==0L) "Conexión sin comprobar" else if(connected && recent) "Conectado" else if(connected) "Último envío confirmado" else "Sin conexión"
            serverStatus.text=title+"\n"+(if(pending==0) "Todo sincronizado" else "$pending posiciones pendientes")+"\nÚltima sincronización · $stamp"
            sentDisplay.text = sent.toString()
            pendingDisplay.text = pending.toString()
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
        profilePhoto = findViewById(R.id.profilePhoto)
        profileCover = findViewById(R.id.profileCover)
        val photoPrefs = getSharedPreferences("optilive_profile", MODE_PRIVATE)
        photoPrefs.getString("cover_uri", photoPrefs.getString("photo_uri", null))?.let { try { profileCover.setImageURI(Uri.parse(it)); profileCover.setPadding(0,0,0,0) } catch (_: Exception) {} }
        photoPrefs.getString("avatar_uri", photoPrefs.getString("photo_uri", null))?.let { try { profilePhoto.setImageURI(Uri.parse(it)); profilePhoto.setPadding(0,0,0,0) } catch (_: Exception) {} }
        findViewById<android.view.View>(R.id.changePhoto).setOnClickListener { coverPicker.launch(arrayOf("image/*")) }
        findViewById<android.view.View>(R.id.profileCover).setOnClickListener { coverPicker.launch(arrayOf("image/*")) }
        findViewById<android.view.View>(R.id.profilePhoto).setOnClickListener { avatarPicker.launch(arrayOf("image/*")) }
        findViewById<android.view.View>(R.id.photoBadge).setOnClickListener { avatarPicker.launch(arrayOf("image/*")) }

        fun showProfileEditor() {
            val panel = findViewById<android.view.View>(R.id.settingsPanel)
            panel.visibility = android.view.View.VISIBLE
            panel.post {
                val scroll = panel.parent?.parent as? android.widget.ScrollView
                scroll?.smoothScrollTo(0, panel.top)
                findViewById<EditText>(R.id.sailNumber).requestFocus()
            }
        }
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
        if ((prefs.getString("server_url", "") ?: "").isBlank()) {
            prefs.edit().putString("server_url", DEFAULT_SERVER).apply()
        }
        serverUrl.setText(prefs.getString("server_url", DEFAULT_SERVER))
        fun refreshProfile() {
            val rawSail = sail.text.toString().trim().uppercase()
            val normalizedSail = when {
                rawSail.isBlank() -> "ESP ----"
                rawSail.startsWith("ESP") -> rawSail
                else -> "ESP $rawSail"
            }
            sailDisplay.text = normalizedSail
            val fullName = "${first.text} ${last.text}".trim().ifBlank { "Regatista" }
            val meta = listOf(category.selectedItem?.toString().orEmpty(), club.text.toString()).filter { it.isNotBlank() }.joinToString(" · ")
            sailorDisplay.text = if (meta.isBlank()) fullName else "$fullName\n$meta"
            findViewById<TextView>(R.id.profileBoat).text = "⛵  $normalizedSail"
            findViewById<TextView>(R.id.profileCategory).text = "👥  ${category.selectedItem?.toString().orEmpty()}"
            findViewById<TextView>(R.id.profileClub).text = "⚑  ${club.text.toString().ifBlank { "Club" }}"
        }
        refreshProfile()

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            prefs.edit().putString("sail", sail.text.toString().trim()).putString("first", first.text.toString().trim())
                .putString("last", last.text.toString().trim()).putString("category", category.selectedItem?.toString().orEmpty())
                .putString("club", club.text.toString().trim()).putString("server_url", serverUrl.text.toString().trim().ifBlank { DEFAULT_SERVER }).apply()
            refreshProfile()
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.mapButton).apply { isClickable=true; isFocusable=true; setOnClickListener { startActivity(Intent(this@MainActivity, MapActivity::class.java)) } }
        findViewById<android.view.View>(R.id.settingsButton).apply { isLongClickable=true; setOnLongClickListener { startActivity(Intent(this@MainActivity, AdminActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)); true } }
        findViewById<android.view.View>(R.id.settingsButton).setOnClickListener {
            val p = findViewById<android.view.View>(R.id.settingsPanel)
            if(p.visibility==android.view.View.VISIBLE)p.visibility=android.view.View.GONE else showProfileEditor()
        }
        findViewById<android.view.View>(R.id.homeButton).setOnClickListener { }
        findViewById<android.view.View>(R.id.topSettings).setOnClickListener { showProfileEditor() }
        findViewById<android.view.View>(R.id.racesButton).apply { isClickable=true; isFocusable=true; setOnClickListener { startActivity(Intent(this@MainActivity, ClassificationActivity::class.java)) } }
        findViewById<android.view.View>(R.id.historyButton).setOnClickListener { startActivity(Intent(this@MainActivity, RegattasActivity::class.java)) }
        findViewById<Button>(R.id.startTracking).setOnClickListener {
            if(getSharedPreferences("optilive_runtime",MODE_PRIVATE).getBoolean("running",false)){
                stopService(Intent(this,LocationService::class.java))
                getSharedPreferences("optilive_runtime",MODE_PRIVATE).edit().putBoolean("running",false).apply()
                refreshGpsStatus()
            } else requestAndStart()
        }
        findViewById<android.view.View>(R.id.stopTracking).setOnClickListener {
            stopService(Intent(this, LocationService::class.java))
            refreshGpsStatus()
        }
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(this, locationReceiver, IntentFilter(LocationService.ACTION_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED)
        ContextCompat.registerReceiver(this, serverReceiver, IntentFilter(LocationService.ACTION_SERVER), ContextCompat.RECEIVER_NOT_EXPORTED)
        val p=getSharedPreferences("optilive_runtime",MODE_PRIVATE)
        val queue=getSharedPreferences("optilive_queue",MODE_PRIVATE)
        val pending=try{org.json.JSONArray(queue.getString("items","[]")).length()}catch(_:Exception){0}
        serverReceiver.onReceive(this,Intent(LocationService.ACTION_SERVER).putExtra("connected",p.getBoolean("connected",false)).putExtra("sent",p.getInt("sent",0)).putExtra("pending",pending))
        if(p.contains("lat"))locationReceiver.onReceive(this,Intent(LocationService.ACTION_LOCATION).putExtra("lat",p.getString("lat","0")!!.toDouble()).putExtra("lon",p.getString("lon","0")!!.toDouble()).putExtra("accuracy",p.getFloat("accuracy",0f)).putExtra("speed",p.getFloat("speed",0f)).putExtra("distance",p.getFloat("distance",0f)).putExtra("satellites",p.getInt("satellites",0)))
        uiHandler.post(refreshState)
    }

    override fun onStop() {
        uiHandler.removeCallbacksAndMessages(null)
        unregisterReceiver(locationReceiver)
        unregisterReceiver(serverReceiver)
        super.onStop()
    }

    private fun requestAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) startGps()
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 20)
    }

    companion object {
        const val DEFAULT_SERVER = "https://optilive-node-production.up.railway.app"
    }

    private fun startGps() {
        ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
        setTrackingUi(true)
        status.text = "BUSCANDO SEÑAL GPS"
    }
}
