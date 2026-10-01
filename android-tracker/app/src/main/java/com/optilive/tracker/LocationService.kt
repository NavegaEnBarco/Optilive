package com.optilive.tracker

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class LocationService : Service() {
    private lateinit var client: FusedLocationProviderClient
    private val io = Executors.newSingleThreadExecutor()
    private var sent = 0
    private var distanceM = 0f
    private var previous: Location? = null

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val l = result.lastLocation ?: return
            previous?.let { distanceM += it.distanceTo(l) }; previous = l
            val knots = if (l.hasSpeed()) l.speed * 1.943844f else 0f
            broadcastGps(l, knots)
            enqueue(l, knots)
            flushQueue()
        }
    }

    override fun onCreate() { super.onCreate(); createChannel(); client=LocationServices.getFusedLocationProviderClient(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("OptiLive GPS activo")
            .setContentText("Seguimiento de regata activo").setOngoing(true).build())
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,2000L).setMinUpdateIntervalMillis(1000L).build(),callback,mainLooper)
        }
        return START_STICKY
    }

    private fun broadcastGps(l: Location, knots: Float) {
        sendBroadcast(Intent(ACTION_LOCATION).setPackage(packageName)
            .putExtra("lat",l.latitude).putExtra("lon",l.longitude).putExtra("accuracy",l.accuracy)
            .putExtra("speed",knots).putExtra("distance",distanceM))
    }

    private fun enqueue(l: Location, knots: Float) {
        val p=getSharedPreferences("optilive_profile",MODE_PRIVATE)
        val sail=p.getString("sail","") ?: ""; if(sail.isBlank()) return
        val obj=JSONObject().put("sail",sail).put("lat",l.latitude).put("lon",l.longitude)
            .put("accuracy",l.accuracy.toDouble()).put("speedKnots",knots.toDouble()).put("timestamp",System.currentTimeMillis())
        synchronized(this) {
            val prefs=getSharedPreferences("optilive_queue",MODE_PRIVATE)
            val arr=try{JSONArray(prefs.getString("items","[]"))}catch(_:Exception){JSONArray()}
            arr.put(obj)
            while(arr.length()>5000) arr.remove(0)
            prefs.edit().putString("items",arr.toString()).apply()
            broadcastServer(false,arr.length())
        }
    }

    private fun flushQueue() {
        val url=getSharedPreferences("optilive_profile",MODE_PRIVATE).getString("server_url","") ?: ""
        if(url.isBlank()) return
        io.execute {
            synchronized(this) {
                val prefs=getSharedPreferences("optilive_queue",MODE_PRIVATE)
                val arr=try{JSONArray(prefs.getString("items","[]"))}catch(_:Exception){JSONArray()}
                var done=0
                try {
                    while(done<arr.length()) {
                        val body=arr.getJSONObject(done).toString()
                        val c=URL(url.trimEnd('/')+"/api/position").openConnection() as HttpURLConnection
                        c.requestMethod="POST"; c.connectTimeout=3000; c.readTimeout=3000; c.doOutput=true
                        c.setRequestProperty("Content-Type","application/json")
                        c.outputStream.use{it.write(body.toByteArray())}
                        val ok=c.responseCode in 200..299; c.disconnect()
                        if(!ok) break
                        done++; sent++
                    }
                } catch(_:Exception) {}
                if(done>0) {
                    val left=JSONArray(); for(i in done until arr.length()) left.put(arr.get(i))
                    prefs.edit().putString("items",left.toString()).apply()
                    broadcastServer(true,left.length())
                } else broadcastServer(false,arr.length())
            }
        }
    }

    private fun broadcastServer(connected:Boolean,pending:Int) {
        sendBroadcast(Intent(ACTION_SERVER).setPackage(packageName).putExtra("connected",connected).putExtra("sent",sent).putExtra("pending",pending))
    }

    override fun onDestroy(){client.removeLocationUpdates(callback);io.shutdown();super.onDestroy()}
    override fun onBind(intent:Intent?):IBinder?=null
    private fun createChannel(){getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"Seguimiento GPS",NotificationManager.IMPORTANCE_LOW))}
    companion object{const val CHANNEL="optilive_gps";const val ACTION_LOCATION="com.optilive.tracker.LOCATION";const val ACTION_SERVER="com.optilive.tracker.SERVER"}
}