package com.optilive.tracker

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {
    private lateinit var client: FusedLocationProviderClient
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            getSharedPreferences("optilive_tracking", MODE_PRIVATE).edit()
                .putLong("lat", java.lang.Double.doubleToRawLongBits(location.latitude))
                .putLong("lon", java.lang.Double.doubleToRawLongBits(location.longitude))
                .putFloat("accuracy", location.accuracy)
                .putLong("time", System.currentTimeMillis())
                .apply()
            sendBroadcast(Intent(ACTION_LOCATION).setPackage(packageName)
                .putExtra("lat", location.latitude)
                .putExtra("lon", location.longitude)
                .putExtra("accuracy", location.accuracy))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        client = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("OptiLive GPS activo")
            .setContentText("Registrando posición del regatista")
            .setOngoing(true).build())
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L).build()
            client.requestLocationUpdates(request, callback, mainLooper)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        client.removeLocationUpdates(callback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Seguimiento GPS", NotificationManager.IMPORTANCE_LOW))
    }

    companion object {
        const val CHANNEL = "optilive_gps"
        const val ACTION_LOCATION = "com.optilive.tracker.LOCATION"
    }
}
