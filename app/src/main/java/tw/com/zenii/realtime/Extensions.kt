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

val plateNumbList = arrayListOf<String>()
// 1818A
fun Activity.setRouteId(route: String){
// TODO: 改成 SQLite
    /*database.use {
        insert("Route", "route" to route)
    }*/

    getSharedPreferences("route", Context.MODE_PRIVATE)
        .edit()
        .putString("route", route)
        .apply()
}

// 1818A
fun Activity.getRouteId(): String {
// TODO: 改成 SQLite
    return getSharedPreferences("route", Context.MODE_PRIVATE)
        .getString("route", "")
    /*val route = database.readableDatabase
        .select("Route", "route")
        .whereArgs("(_id = {userId})", "userId" to 0)
        .toString()
    Log.d(TAG, "getRouteId: $route")
    return route*/
}

// 1818A1
fun Activity.setMapRouteId(route: String) {
    // TODO: 改成 SQLite
    getSharedPreferences("mapRoute", Context.MODE_PRIVATE)
        .edit()
        .putString("mapRoute", route)
        .apply()
}

// 1818A1
fun Activity.getMapRouteId(): String {
    // TODO: 改成 SQLite
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
    // TODO: 改成 SQLite
    var plateNumbList = arrayListOf<String>()
    database.use {
        select ("Tracker", "plateNumb")
            .parseList(StringParser).forEach {
                plateNumbList.add(it)
            }
    }
    return plateNumbList
}
