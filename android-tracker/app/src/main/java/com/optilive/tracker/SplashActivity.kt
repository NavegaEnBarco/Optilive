package com.optilive.tracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val openProfile = Runnable {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        @Suppress("DEPRECATION")
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.navigationBarColor = android.graphics.Color.rgb(5, 35, 76)
        setContentView(R.layout.activity_splash)
        findViewById<TextView>(R.id.splashVersion).text = "OptiLive ${packageManager.getPackageInfo(packageName, 0).versionName}"
        handler.postDelayed(openProfile, 3000L)
    }

    override fun onDestroy() {
        handler.removeCallbacks(openProfile)
        super.onDestroy()
    }
}
