package tw.com.zenii.realtime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.Constraints.TAG
import kotlinx.coroutines.selects.select
import org.jetbrains.anko.db.StringParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.info
import java.io.File

// 1818 4 碼
fun Activity.setSearchRouteId(route: String){
    getSharedPreferences("searchRoute", Context.MODE_PRIVATE)
        .edit()
        .putString("searchRoute", route)
        .apply()
}

// 1818 4 碼
fun Activity.getSearchRouteId(): String {
    return getSharedPreferences("searchRoute", Context.MODE_PRIVATE)
        .getString("searchRoute", "")
}


// 1818A 5 碼
fun Activity.setRouteId(route: String){
    getSharedPreferences("route", Context.MODE_PRIVATE)
        .edit()
        .putString("route", route)
        .apply()
}

// 1818A 5 碼
fun Activity.getRouteId(): String {
    return getSharedPreferences("route", Context.MODE_PRIVATE)
        .getString("route", "")
}

// 1818A1 6 碼
fun Activity.setMapRouteId(route: String) {
    getSharedPreferences("mapRoute", Context.MODE_PRIVATE)
        .edit()
        .putString("mapRoute", route)
        .apply()
}

// 1818A1 6 碼
fun Activity.getMapRouteId(): String {
    return getSharedPreferences("mapRoute", Context.MODE_PRIVATE)
        .getString("mapRoute", "")
}

fun Activity.setPlateNumb(trackPlateNumb: String) {
    database.use {
        insert("Tracker",
            "plateNumb" to trackPlateNumb
        )
    }
}

fun Activity.getPlateNumb(): List<String> {
    var plateNumbList = arrayListOf<String>()
    database.use {
        select ("Tracker", "plateNumb")
            .parseList(StringParser).forEach {
                plateNumbList.add(it)
            }
    }
    return plateNumbList
}
