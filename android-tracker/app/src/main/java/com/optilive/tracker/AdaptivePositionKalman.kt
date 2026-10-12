package com.optilive.tracker

import android.location.Location
import kotlin.math.*

/** Adaptive 2D position filter. Raw GNSS fixes must always be retained separately. */
class AdaptivePositionKalman {
    private var lat = Double.NaN
    private var lon = Double.NaN
    private var variance = 100.0
    private var lastNanos = 0L
    private var lastBearing = Double.NaN

    fun update(fix: Location): Pair<Double, Double> {
        val time = fix.elapsedRealtimeNanos
        val accuracy = fix.accuracy.toDouble().coerceAtLeast(2.0)
        if (!lat.isFinite() || time <= lastNanos || (time-lastNanos)>20_000_000_000L) {
            lat=fix.latitude;lon=fix.longitude;variance=accuracy*accuracy;lastNanos=time
            lastBearing=if(fix.hasBearing()) fix.bearing.toDouble() else Double.NaN
            return lat to lon
        }
        val dt=((time-lastNanos)/1e9).coerceIn(0.01,20.0)
        val turn=if(fix.hasBearing() && lastBearing.isFinite()) {
            abs((fix.bearing.toDouble()-lastBearing+540.0)%360.0-180.0)
        } else 0.0
        // More process noise during manoeuvres reduces filtering lag.
        val motion=if(fix.hasSpeed()) fix.speed.toDouble() else 1.0
        val processNoise=(0.8+motion*0.5+if(turn>20.0) 4.0 else 0.0)
        variance+=processNoise*processNoise*dt
        val gain=(variance/(variance+accuracy*accuracy)).coerceIn(0.0,1.0)
        lat+=gain*(fix.latitude-lat)
        var deltaLon=(fix.longitude-lon+540.0)%360.0-180.0
        lon+=gain*deltaLon
        lon=(lon+540.0)%360.0-180.0
        variance=(1.0-gain)*variance
        lastNanos=time
        lastBearing=if(fix.hasBearing())fix.bearing.toDouble() else Double.NaN
        return lat to lon
    }
}
