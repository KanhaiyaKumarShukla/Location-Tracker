package com.exa.android.loctrace.Fragments.Employee.Location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient (
    private val context : Context,
    private val client : FusedLocationProviderClient
): LocationClient {
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
          return callbackFlow { // used if you have some callback that can be transformed in flow or we want a model that has certain lifecycle
              if(!context.hasLocationPermission()){
                  throw LocationClient.LocationException("Missing Location Permission")
              }

              val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
              val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
              val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

              if(!isGpsEnabled && !isNetworkEnabled){
                  throw LocationClient.LocationException("GPS is Disabled")
              }

              val request = LocationRequest.Builder(
                  Priority.PRIORITY_HIGH_ACCURACY,interval*2 // time interval in which we want to update location
              ).apply {
                  setWaitForAccurateLocation(false)
                  setMinUpdateIntervalMillis(interval) // update location in certain interval even if  the new location is coming frequently
                  setMaxUpdateDelayMillis(interval*4)  // instead of sending new location one by one in every 10 sec it create a batch ie store location till 20 sec and then send all location at once to save battery and power
              }.build()

              val locationCallback = object : LocationCallback(){
                  override fun onLocationResult(result: LocationResult) { // this will be called whenever our locationclient fetches new location
                      super.onLocationResult(result)
                      result.locations.lastOrNull()?.let{location->   // because last value of result is the only updated value                          launch { send(location) }
                         launch { send(location) }
                      }
                  }
              }

              client.requestLocationUpdates(
                  request,
                  locationCallback,
                  Looper.getMainLooper()
              )

              awaitClose {
                  client.removeLocationUpdates(locationCallback)
              }
          }
    }
}