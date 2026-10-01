package com.optilive.tracker

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class LocationService : Service() {
    private lateinit var client: FusedLocationProviderClient
    private val io = Executors.newSingleThreadExecutor()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val now = System.currentTimeMillis()
            getSharedPreferences("optilive_tracking", MODE_PRIVATE).edit()
                .putLong("lat", java.lang.Double.doubleToRawLongBits(location.latitude))
                .putLong("lon", java.lang.Double.doubleToRawLongBits(location.longitude))
                .putFloat("accuracy", location.accuracy).putLong("time", now).apply()
            sendBroadcast(Intent(ACTION_LOCATION).setPackage(packageName)
                .putExtra("lat", location.latitude).putExtra("lon", location.longitude)
                .putExtra("accuracy", location.accuracy))
            sendPosition(location.latitude, location.longitude, location.accuracy, now)
        }
    }

    override fun onCreate() { super.onCreate(); createChannel(); client = LocationServices.getFusedLocationProviderClient(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("OptiLive GPS activo")
            .setContentText("Registrando y enviando posición").setOngoing(true).build())
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).setMinUpdateIntervalMillis(1000L).build(), callback, mainLooper)
        }
        return START_STICKY
    }

    private fun sendPosition(lat: Double, lon: Double, accuracy: Float, timestamp: Long) {
        val profile = getSharedPreferences("optilive_profile", MODE_PRIVATE)
        val sail = profile.getString("sail", "") ?: ""
        val serverUrl = profile.getString("server_url", "") ?: ""
        if (sail.isBlank() || serverUrl.isBlank()) return
        io.execute {
            try {
                val body = JSONObject().put("sail", sail).put("lat", lat).put("lon", lon)
                    .put("accuracy", accuracy.toDouble()).put("timestamp", timestamp).toString()
                val conn = URL(serverUrl.trimEnd('/') + "/api/position").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"; conn.connectTimeout = 4000; conn.readTimeout = 4000
                conn.doOutput = true; conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(body.toByteArray()) }
                conn.inputStream.close(); conn.disconnect()
            } catch (_: Exception) { /* next version: durable offline queue */ }
        }
    }

    override fun onDestroy() { client.removeLocationUpdates(callback); io.shutdown(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createChannel() { getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, "Seguimiento GPS", NotificationManager.IMPORTANCE_LOW)) }

    companion object { const val CHANNEL="optilive_gps"; const val ACTION_LOCATION="com.optilive.tracker.LOCATION" }
}
