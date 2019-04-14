package tw.com.zenii.realtime

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.*

class InterCityBusHandler {

    fun getRouteSearchResult(key: String): JsonArray {
        val jaToReturn = JsonArray()
        val subRouteNamesGotten = ArrayList<String>()
        val mongo = Mongo()

        val result = mongo.call("getRouteSearchResult", key) ?: return jaToReturn
        Log.d("result", result)
        val resObj = JsonParser().parse(result!!).getAsJsonObject()

        // build jaToReturn here
        val subRoutes = resObj.get("SubRoutes").getAsJsonArray()
        for (subRoute in subRoutes) {
            val joToAdd = JsonObject()
            val subRouteObj = subRoute.getAsJsonObject()
            var subRouteName = subRouteObj.get("SubRouteName").getAsJsonObject().get("Zh_tw").getAsString()
            if (/*subRouteName.length() == 5 && */subRouteName.substring(4, 5) == "0") {
                subRouteName = subRouteName.substring(0, 4)
            }
            val headsign = subRouteObj.get("Headsign").getAsString()
            if (!subRouteNamesGotten.contains(subRouteName)) {
                subRouteNamesGotten.add(subRouteName)
                joToAdd.addProperty("SubRouteID", subRouteName)
                joToAdd.addProperty("Headsign", headsign)
                jaToReturn.add(joToAdd)
            }
        }

        return jaToReturn
    }
}