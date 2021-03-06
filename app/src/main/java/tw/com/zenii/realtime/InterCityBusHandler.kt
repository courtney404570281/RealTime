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

    private val mongo = Mongo()

    // 取得搜尋結果
    fun getRouteSearchResult(key: String): JsonArray {
        val jaToReturn = JsonArray()
        val subRouteNamesGotten = ArrayList<String>()

        val result = mongo.call("getRouteSearchResult", key) ?: return jaToReturn
        Log.d("result", result)
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

    // 取得起點站
    fun getDeparture(subRouteId: String): String {
        var departure = ""

        val result = mongo.call("getRouteSearchResult", subRouteId) ?: return departure
        val resObj = JsonParser().parse(result!!).asJsonObject
        departure = resObj.get("DepartureStopNameZh").asString
        Log.d("result1", departure)

        return departure
    }

    // 取得終點站
    fun getDestination(subRouteId: String): String {
        var destination = ""

        val result = mongo.call("getRouteSearchResult", subRouteId) ?: return destination
        val resObj = JsonParser().parse(result!!).asJsonObject
        destination = resObj.get("DestinationStopNameZh").asString
        Log.d("result1", destination)

        return destination
    }

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

    // getRealRoute 暫時不使用，目前只有 1818 的資料
    fun getRealRoute(): List<LatLng> {
        var positions = ArrayList<LatLng>()

        val results = mongo.call("getRealRoute", "test") ?: return positions
        val ja = JsonParser().parse(results!!).asJsonObject
            .get("PositionPoints").asJsonArray
        for (je in ja) {
            val lat = je.asJsonArray[0].asDouble
            val lng = je.asJsonArray[1].asDouble
//            Log.d(TAG, "getRealRoute: $lat $lng")
            positions.add(LatLng(lat, lng))
        }
        return positions
    }

    // 取得最近站牌
    fun getNearStop(plateNumb: String): Map<String, String> {
        val nearStop = HashMap<String, String>()
        var name: String
        var numb: String
        var temp = ""

        val results = mongo.call("getNearStop", plateNumb) ?: return nearStop
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            val stops = res.get("StopName").asJsonObject
            numb = res.get("PlateNumb").asString
            name = stops.asJsonObject
                .get("Zh_tw").asString
            if (name != null) {
                nearStop[numb] = name
                temp = name
            } else {
                nearStop[numb] = temp
            }
        }
        return nearStop
    }

    // 取得車況
    fun getBusStatus(plateNumb: String): Map<String, String> {
        val busStatus = HashMap<String, String>()
        var name: String
        var numb: String
        var temp = ""

        val results = mongo.call("getNearStop", plateNumb) ?: return busStatus
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            numb = res.get("PlateNumb").asString
            name = res.get("BusStatus").asString

            if (name != null) {
                var message = "正常"
                when (name) {
                    "0" -> "正常"
                    "100" -> "客滿"
                    else -> "異常"
                }
                busStatus[numb] = message
                temp = name
            } else {

                var message = "正常"
                when (temp) {
                    "0" -> "正常"
                    "100" -> "客滿"
                    else -> "異常"
                }
                busStatus[numb] = message
            }
        }
        return busStatus
    }

    // 取得車況
    fun getA2EventType(plateNumb: String): Map<String, String> {
        val a2EventType = HashMap<String, String>()
        var name: String
        var numb: String
        var temp = ""

        val results = mongo.call("getNearStop", plateNumb) ?: return a2EventType
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            numb = res.get("PlateNumb").asString
            name = res.get("A2EventType").asString
            if (name != null) {
                var message = "離站"
                when (name) {
                    "0" -> "離站"
                    "1" -> "進站"
                }
                a2EventType[numb] = message
                temp = name
            } else {

                var message = "離站"
                when (temp) {
                    "0" -> "離站"
                    "1" -> "進站"
                }
                a2EventType[numb] = message
            }
        }
        return a2EventType
    }

    // 取得路線
    fun getSubRouteName(plateNumb: String): HashMap<String, String?> {
        val subRouteName = HashMap<String, String?>()
        var name: String
        var numb: String

        val results = mongo.call("getNearStop", plateNumb) ?: return subRouteName
        val ja = JsonParser().parse(results!!).asJsonArray

        for (je in ja) {
            val res = je.asJsonObject
            numb = res.get("PlateNumb").asString
            name = res.get("SubRouteName").asJsonObject
                .asJsonObject
                .get("Zh_tw").asString
            if (name.substring(4) == "0") {
                name = name.substring(0,4)
            }
            subRouteName[numb] = name
        }
        return subRouteName
    }

}