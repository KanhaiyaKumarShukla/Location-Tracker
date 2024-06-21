package com.exa.android.loctrace.Fragments.Employee.Location

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.exa.android.loctrace.Helper.AppConstants
import com.exa.android.loctrace.Helper.AppConstants.showToast
import com.exa.android.loctrace.Helper.AppConstants.userId
import com.exa.android.loctrace.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient : LocationClient
    private lateinit var debRef: DatabaseReference
    private lateinit var geoFire: GeoFire

    private lateinit var dbRef: DatabaseReference
    private lateinit var geofire: GeoFire

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        debRef = FirebaseDatabase.getInstance().reference.child("Locations")
        geoFire = GeoFire(debRef)

        userId?.let {
            dbRef = FirebaseDatabase.getInstance().reference.child(it)
            geofire = GeoFire(dbRef)
        } ?: run {
            stopSelf() // Stop the service if userId is null
            return
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(){
        val notification = NotificationCompat.Builder(this,"location")
            .setContentTitle("Tracking location...")
            .setContentText("Location : null")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)  // user can't swipe it

        startForeground(1,notification.build())
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(2000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude
                val long = location.longitude
                val updateNotification = notification.setContentText(
                    "Location : ($lat, $long)"
                )
                Log.d("LocationService", "lat : ${lat}, long : ${long}")
                saveLocationInDatabase(lat,long)
                notificationManager.notify(1,updateNotification.build())
            }
            .launchIn(serviceScope)

    }

    private fun saveLocationInDatabase(latitude: Double, longitude: Double) {
        val userId = userId.toString() // Make sure userId is correctly retrieved

        Log.d("LocationService", "Saving location for userId: $userId, lat: $latitude, long: $longitude")

        geoFire.setLocation(userId, GeoLocation(latitude, longitude)) { key, error ->
            if (error == null) {
                Log.d("LocationService", "Location saved successfully for userId: $userId")
            } else {
                Log.e("LocationService", "Failed to save location for userId: $userId. Error: $error")
            }
        }
        if (AppConstants.isCapturingStartLocation) {
            geofire.setLocation("start_location", GeoLocation(latitude, longitude))

            AppConstants.isCapturingStartLocation=false
        } else  {
            geofire.setLocation("destination_location", GeoLocation(latitude, longitude))
        }
    }

    private fun stop(){
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object{
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}