package tw.com.zenii.realtime

import android.app.Activity
import android.content.Context

// 1818A
fun Activity.setRouteId(route: String){
    getSharedPreferences("route", Context.MODE_PRIVATE)
        .edit()
        .putString("route", route)
        .apply()
}

// 1818A
fun Activity.getRouteId(): String {
    return getSharedPreferences("route", Context.MODE_PRIVATE)
        .getString("route", "")
}

// 1818A1
fun Activity.setMapRouteId(route: String) {
    getSharedPreferences("mapRoute", Context.MODE_PRIVATE)
        .edit()
        .putString("mapRoute", route)
        .apply()
}

// 1818A1
fun Activity.getMapRouteId(): String {
    return getSharedPreferences("mapRoute", Context.MODE_PRIVATE)
        .getString("mapRoute", "")
}