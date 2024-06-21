package com.exa.android.loctrace.data

data class DirectionsResponse(val routes: List<Route>)
data class Route(val overview_polyline: OverviewPolyline)
data class OverviewPolyline(val points: String)
