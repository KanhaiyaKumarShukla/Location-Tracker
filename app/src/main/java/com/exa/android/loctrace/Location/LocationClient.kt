package com.exa.android.loctrace.Fragments.Employee.Location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {

    // this is used to continuously get the location in certain interval
    fun getLocationUpdates(interval : Long) : Flow<Location>

    // when GPS  is not enabled or user decline permission
    class LocationException(message : String) : Exception()

}