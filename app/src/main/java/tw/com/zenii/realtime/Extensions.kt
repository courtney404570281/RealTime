package tw.com.zenii.realtime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.io.File

// 1818A
fun Activity.setRouteId(route: String){

    /*File("output.txt").bufferedWriter().use{
        it.write(route)
    }*/


    getSharedPreferences("route", Context.MODE_PRIVATE)
        .edit()
        .putString("route", route)
        .apply()
}

// 1818A
fun Activity.getRouteId(): String {

    //return File("output.txt").bufferedReader().lines().toString()
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