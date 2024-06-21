package com.exa.android.loctrace

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.exa.android.loctrace.data.DirectionsResponse
import com.exa.android.loctrace.databinding.FragmentEmployeeLocationBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class EmployeeLocation : Fragment(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private var _binding: FragmentEmployeeLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var debRef: DatabaseReference
    private lateinit var geoFire: GeoFire
    private lateinit var dbRef: DatabaseReference
    private lateinit var geofire: GeoFire
    private lateinit var mMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEmployeeLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debRef = FirebaseDatabase.getInstance().reference.child("Locations")
        geoFire = GeoFire(debRef)
        // Get a handle to the fragment and register the callback.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)  // to set up an asynchronous callback.  you register the fragment as a callback to be notified when the map is ready to be used. This means that once the map has been initialized and is ready for use, the onMapReady(GoogleMap) method of this fragment will be called automatically.
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("map", "map is ready")

        retrieveAndDisplayLocations()
        // Optionally move the camera to the marker
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 10f))
    }

    @SuppressWarnings("all")
    private fun addLocationToMap(key: String, location: GeoLocation) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(latLng).title(key))
        mMap.setOnMarkerClickListener { marker ->
            val markerTitle = marker.title
            // You can perform actions based on the marker title (which should be the key in your case)
            Log.d("MarkerClick", "Clicked marker with key: $markerTitle")
            if (isAdded) {
                fatchPointsOf(markerTitle!!)
            }
            false
        }
    }


    private fun fatchPointsOf(employee: String) {
        dbRef = FirebaseDatabase.getInstance().reference.child(employee)
        geofire = GeoFire(dbRef)

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("polyline 1", snapshot.key.toString())
                val dest = snapshot.child("destination_location/l")
                    .getValue(object : GenericTypeIndicator<List<Double>>() {})
                val src = snapshot.child("start_location/l")
                    .getValue(object : GenericTypeIndicator<List<Double>>() {})
                Log.d("destination_loc", dest.toString())
                Log.d("start_location", src.toString())

                drawRoute(
                    LatLng(src?.get(0) ?: 0.0, src?.get(1) ?: 0.0),
                    LatLng(dest?.get(0) ?: 0.0, dest?.get(1) ?: 0.0)
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("polyline", error.message)
            }
        })
    }

    private fun drawPolyline(points: List<LatLng>) {
        /*if (points.isNotEmpty()) {
            Log.d("poly", points.toString())
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .width(8f)
                .color(Color.BLUE)
                .geodesic(true)
                .clickable(true)

            mMap.addPolyline(polylineOptions)
            // Add a marker at the starting point
            for (i in points.indices) {
                val point = points[i]
                Log.d("polyP", point.toString())
                val markerOptions = MarkerOptions().position(point).title("Point $i")
                // Change marker color
                when (i) {
                    0 -> markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // Start point
                    points.size - 1 -> markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // End point
                    else -> markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // Intermediate points
                }
                mMap.addMarker(markerOptions)

                // Optionally move the camera to the start point
                if (i == 0) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
                }
            }
        }*/
        if (points.size > 1) drawRoute(points[0], points[points.size - 1])
    }

    private fun retrieveAndDisplayLocations() {
        debRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("GeoFire", "location track")
                readData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GeoFire", "Error retrieving locations: $error")
            }
        })
    }

    private fun readData() {
        debRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (childSnapShot in snapshot.children) {
                    handleLocationChange(childSnapShot)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GeoFire", "Error retrieving locations: $error")

            }
        })
    }

    private fun handleLocationChange(snapshot: DataSnapshot) {
        val key = snapshot.key
        val location =
            snapshot.child("l").getValue(object : GenericTypeIndicator<List<Double>>() {})
        Log.d("GeoFire", key.toString() + location.toString())
        if (key != null && location != null && location.size == 2) {
            val geoLocation = GeoLocation(location[0], location[1])
            addLocationToMap(key, geoLocation)
            moveCameraToLocation(geoLocation)
            Log.d("mLoc", "$geoLocation $key")
        }
    }

    private fun moveCameraToLocation(location: GeoLocation) {
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate =
            CameraUpdateFactory.newLatLngZoom(latLng, 15f) // Adjust zoom level as needed
        mMap.animateCamera(cameraUpdate)
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {
        val originParam = "origin=${origin.latitude},${origin.longitude}"
        val destParam = "destination=${dest.latitude},${dest.longitude}"
        val key = "AIzaSyDd4gRN7vvt0t0vACMAa5tKuTy5Pqze24Y"
        return "https://maps.googleapis.com/maps/api/directions/json?$originParam&$destParam&key=$key"
    }

    private suspend fun fetchRoute(origin: LatLng, dest: LatLng, callback: (List<LatLng>) -> Unit) {

        val client = OkHttpClient()
/* //            val url = getDirectionsUrl(origin, dest)
//        val request = Request.Builder().url(url).build()
//        client.newCall(request).enqueue(object : okhttp3.Callback {
//            override fun onFailure(call: okhttp3.Call, e: IOException) {
//                e.printStackTrace()
//                Log.d("route error", e.message.toString())
//            }
//
//            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
//                response.body?.let { responseBody ->
//                    val responseText = responseBody.string()
//                    Log.d("route", responseText)
//                    val directionsResponse =
//                        Gson().fromJson(responseText, DirectionsResponse::class.java)
//                    if (directionsResponse.routes.isNotEmpty()) {
//                        Log.d("route size", directionsResponse.routes.size.toString())
//                        val route = directionsResponse.routes[0]
//                        val decodedPath = PolyUtil.decode(route.overview_polyline.points)
//                        callback(decodedPath)
//                    }
//                }
//            } })*/
        withContext(Dispatchers.IO) {
            val url = getDirectionsUrl(origin, dest)
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseText = response.body?.string() ?: ""
                val directionsResponse = Gson().fromJson(responseText, DirectionsResponse::class.java)

                if (directionsResponse.routes.isNotEmpty()) {
                    val route = directionsResponse.routes[0]
                    val decodedPath = PolyUtil.decode(route.overview_polyline.points)
                    callback(decodedPath)
                } else {
                    throw IOException("No routes found")
                }
            }
        }
    }

    private fun drawRoute(origin: LatLng, dest: LatLng) {
        Log.d("drawRoute", "begin")
        GlobalScope.launch(Dispatchers.Main) {
            try {
                fetchRoute(origin, dest) { routePoints ->
                    if (routePoints.isNotEmpty()) {
                        Log.d("route", routePoints.toString())
                        val polylineOptions = PolylineOptions()
                            .addAll(routePoints)
                            .width(10f)
                            .color(Color.BLUE)
                            .geodesic(true)
                            .clickable(true)

                        activity?.runOnUiThread {
                            if (isAdded) {
                                mMap.addPolyline(polylineOptions)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("fetchRoute", "Error fetching route", e)
                // Handle error (e.g., show error message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPolylineClick(polyline: Polyline) {
        polyline.color = polyline.color xor 0x00ffffff
    }
}