package tw.com.zenii.realtime

import android.content.ContentValues.TAG
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonElement
import kotlin.collections.ArrayList


class InterCityBusHandler  {

    val mongo = Mongo()

    // 取得搜尋結果
    fun getRouteSearchResult(key: String): JsonArray {
        val jaToReturn = JsonArray()
        val subRouteNamesGotten = ArrayList<String>()

        val result = mongo.call("getRouteSearchResult", key) ?: return jaToReturn
        //Log.d("result", result)
        val resObj = JsonParser().parse(result!!).asJsonObject

        // build jaToReturn here
        val subRoutes = resObj.get("SubRoutes").asJsonArray
        for (subRoute in subRoutes) {
            val joToAdd = JsonObject()
            val subRouteObj = subRoute.asJsonObject
            var subRouteName = subRouteObj.get("SubRouteName").asJsonObject.get("Zh_tw").asString
            if (/*subRouteName.length() == 5 && */subRouteName.substring(4, 5) == "0") {
                subRouteName = subRouteName.substring(0, 4)
            }
            Log.d(TAG, "subRouteName: $subRouteName")
            val headSign = subRouteObj.get("Headsign").asString
            if (!subRouteNamesGotten.contains(subRouteName)) {
                subRouteNamesGotten.add(subRouteName)
                joToAdd.addProperty("SubRouteID", subRouteName)
                joToAdd.addProperty("Headsign", headSign)
                jaToReturn.add(joToAdd)
            }
        }

        return jaToReturn
    }

    // TODO: routeID 取得五碼

    // 取得站牌位置
    fun getStopPosition(subRouteId: String): List<LatLng> {
        var lat: Double
        var lng: Double
        val stopPositions = ArrayList<LatLng>()

        val results = mongo.call("getStopOfRoute", subRouteId) ?: return stopPositions
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stops = res.get("Stops").asJsonArray
            for (stop in stops) {
                lat = stop.asJsonObject
                    .get("StopPosition").asJsonObject
                    .get("PositionLat").asDouble
                lng = stop.asJsonObject
                    .get("StopPosition").asJsonObject
                    .get("PositionLon").asDouble
                stopPositions.add(LatLng(lat, lng))
            }
        }
        return stopPositions
    }

    // 取得站牌名稱（搭配經緯度）
    fun getStopName(subRouteId: String): Map<LatLng, String> {
        var lat: Double
        var lng: Double
        val stopName = HashMap<LatLng, String>()
        var name: String

        val results = mongo.call("getStopOfRoute", subRouteId) ?: return stopName
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stops = res.get("Stops").asJsonArray
            for (stop in stops) {
                lat = stop.asJsonObject
                    .get("StopPosition").asJsonObject
                    .get("PositionLat").asDouble
                lng = stop.asJsonObject
                    .get("StopPosition").asJsonObject
                    .get("PositionLon").asDouble
                name = stop.asJsonObject
                    .get("StopName").asJsonObject
                    .get("Zh_tw").asString
                stopName[LatLng(lat, lng)] = name
            }
        }
        return stopName
    }

    // 取得公車位置
    fun getBusPosition(subRouteId: String): List<LatLng> {
        var lat: Double
        var lng: Double
        val busPositions = ArrayList<LatLng>()

        val results = mongo.call("getFrequency", subRouteId) ?: return busPositions
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stops = res.get("BusPosition").asJsonObject
            lat = stops.asJsonObject
                .get("PositionLat").asDouble
            lng = stops.asJsonObject
                .get("PositionLon").asDouble
            busPositions.add(LatLng(lat, lng))
        }
        return busPositions
    }

    // 取得車牌號碼（搭配經緯度）
    fun getPlateNumb(subRouteId: String): Map<LatLng, String> {
        var lat: Double
        var lng: Double
        val plateNumb = HashMap<LatLng, String>()
        var numb: String

        val results = mongo.call("getFrequency", subRouteId) ?: return plateNumb
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stops = res.get("BusPosition").asJsonObject
            numb = res.get("PlateNumb").asString
            lat = stops.asJsonObject
                .get("PositionLat").asDouble
            lng = stops.asJsonObject
                .get("PositionLon").asDouble
            plateNumb[LatLng(lat, lng)] = numb
        }
        return plateNumb
    }

    // 預計到站時間
    fun getEstimateTime(subRouteId: String): List<String> {
        val estimateTimes = ArrayList<String>()

        val results = mongo.call("getEstimated", subRouteId) ?: return estimateTimes
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val jsonObject = je.asJsonObject
            var estimateTime = "未發車"
            if (jsonObject.has("EstimateTime")) {
                estimateTime = "${jsonObject.get("EstimateTime").asInt / 60}  min"
            }
            estimateTimes.add(estimateTime)
        }

        return estimateTimes
    }

    // 預計到站之站牌名稱
    fun getStopNames(subRouteId: String): List<String> {
        val stopNames = ArrayList<String>()

        val results = mongo.call("getEstimated", subRouteId) ?: return stopNames
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stop = res.get("StopName").asJsonObject
            val tw = stop.asJsonObject
                .get("Zh_tw").asString
            stopNames.add(tw)
        }
        return stopNames
    }

    // test
    fun getTest(): String {
        val test = ArrayList<String>()
        val results = mongo.call("getRealRoute", "test") ?: return ""

        return results
    }

    // 取得最近站牌
    fun getNearStop(plateNumb: String): Map<String, String> {
        val nearStop = HashMap<String, String>()
        var name: String
        var numb: String

        val results = mongo.call("getNearStop", plateNumb) ?: return nearStop
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stops = res.get("StopName").asJsonObject
            numb = res.get("PlateNumb").asString
            name = stops.asJsonObject
                .get("Zh_tw").asString
            nearStop[numb] = name
        }
        return nearStop
    }

    // 取得車況
    fun getBusStatus(plateNumb: String): Map<String, String> {
        val nearStop = HashMap<String, String>()
        var name: String
        var numb: String

        val results = mongo.call("getNearStop", plateNumb) ?: return nearStop
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            numb = res.get("PlateNumb").asString
            name = res.get("BusStatus").asString
            var message = "正常"
            when (name) {
                "0" -> "正常"
                "100" -> "客滿"
                else -> "異常"
            }
            nearStop[numb] = message
        }
        return nearStop
    }

    // 取得車況
    fun getA2EventType(plateNumb: String): Map<String, String> {
        val nearStop = HashMap<String, String>()
        var name: String
        var numb: String

        val results = mongo.call("getNearStop", plateNumb) ?: return nearStop
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            numb = res.get("PlateNumb").asString
            name = res.get("BusStatus").asString
            var message = "離站"
            when (name) {
                "0" -> "正常"
                "1" -> "進站"
            }
            nearStop[numb] = message
        }
        return nearStop
    }

    // 取得路線
    fun getRoute(plateNumb: String): Map<String, String> {
        val nearStop = HashMap<String, String>()
        var name: String
        var numb: String

        val results = mongo.call("getNearStop", plateNumb) ?: return nearStop
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stops = res.get("RouteName").asJsonObject
            numb = res.get("PlateNumb").asString
            name = stops.asJsonObject
                .get("Zh_tw").asString
            nearStop[numb] = name
        }
        return nearStop
    }

}