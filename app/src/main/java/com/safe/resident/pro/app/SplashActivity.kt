package com.safe.resident.pro.app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.safe.resident.pro.app.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SplashActivity, R.layout.activity_splash)

        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false)

        binding.tvVersionName.text = "Version: V${BuildConfig.VERSION_NAME}"

        Handler(mainLooper).postDelayed({
            val destination = if (isLoggedIn) MainActivity::class.java else LoginActivity::class.java
            val intent = Intent(this, destination)
            if (isNetworkAvailable()) {
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this@SplashActivity, "No internet connection", Toast.LENGTH_LONG).show()
                finish()
            }
        }, 2000)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}