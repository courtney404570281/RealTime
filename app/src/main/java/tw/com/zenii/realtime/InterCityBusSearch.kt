package tw.com.zenii.realtime

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_inter_city_bus_search.*
import tw.com.zenii.realtime.tracker.Tracker
import tw.com.zenii.realtime.tracker.TrackerAdapter
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener
import com.paulrybitskyi.persistentsearchview.utils.VoiceRecognitionDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.pawegio.kandroid.onQuerySubmit
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.info
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InterCityBusSearch : AppCompatActivity(), AnkoLogger {

    private val interCityBusHandler = InterCityBusHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inter_city_bus_search)

        with(search) {
            setOnLeftBtnClickListener {
                // Handle the left button click
            }
            setOnClearInputBtnClickListener {
                // Handle the clear input button click
            }

            // Setting a delegate for the voice recognition input
            setVoiceRecognitionDelegate(VoiceRecognitionDelegate(this@InterCityBusSearch))

            setOnSearchConfirmedListener { searchView, query ->
                info { "search: $query" }
                setSearchRouteId(query)
                searchRoute(query)
                // Handle a search confirmation. This is the place where you'd
                // want to perform a search against your data provider.
            }

            // Disabling the suggestions since they are unused in
            // the simple implementation
            setSuggestionsDisabled(true)
        }

        // 在 searchView 內取值
        //search.queryHint = getString(R.string.please_enter_the_route)

        /*search.onQuerySubmit { query ->
            info { "search: $query" }
            setSearchRouteId(query)
            searchRoute(query)
        }*/

        // 每 5 秒更新一次資料
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            GlobalScope.launch {
                drawTrackerList()
            }
            // 測試 5s
            info { "InterCityBusTimer: ${Date()}" }
        }, 0, 60, TimeUnit.SECONDS)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Calling the voice recognition delegate to properly handle voice input results
        VoiceRecognitionDelegate.handleResult(search, requestCode, resultCode, data)
    }

    // 搜尋
    private fun searchRoute(query: String) {
        if (query.isNotEmpty()) {
            val routeNameResults = ArrayList<String>() // 路線名稱
            val routeIdResults = ArrayList<String>() // 路線代號
            var result: String // 1818 臺北→中壢 1818A 臺北→中壢[繞駛中原大學站]
            var resultId: String // 1818 1818A

            GlobalScope.launch {
                val resJa = interCityBusHandler.getRouteSearchResult(query)
                for (je in resJa) {
                    val jo = je.asJsonObject
                    result = jo.get("SubRouteID").asString + "\n" + jo.get("Headsign").asString
                    resultId = jo.get("SubRouteID").asString
                    info { "result: $result" }
                    routeNameResults.add(result)
                    routeIdResults.add(resultId)
                }

                info { "${interCityBusHandler.getRealRoute()}" }

                runOnUiThread {
                    if (routeNameResults.isEmpty()) {
                        alert(query + "\t" + getString(R.string.not_found), getString(R.string.message)) {
                            positiveButton(getString(R.string.search_others)) {
                                //search.setQuery("", false)
                                //search.isIconified
                            }
                        }.show()
                    }

                    val adapter =
                        ArrayAdapter(this@InterCityBusSearch, android.R.layout.simple_list_item_1, routeNameResults)
                    list.adapter = adapter
                    var itemListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        var type = routeIdResults[position].substring(4)
                        var route = routeIdResults[position].substring(0, 4)
                        if (type == "") {
                            route += "0"
                        } else {
                            route = routeIdResults[position].substring(0, 5)
                        }
                        info { "itemListener: $route" }

                        val intent = Intent(this@InterCityBusSearch, MapsActivity::class.java)
                        // 設定 RouteId 1818
                        setRouteId(route)
                        info { "setRouteId: $route" }
                        startActivity(intent)
                        finish()
                    }
                    list.onItemClickListener = itemListener
                }

            }

        }
    }

    // 繪製追蹤清單
    private fun drawTrackerList() {
        GlobalScope.launch {

            var trackPlateNumb = ""
            var trackNearStop = interCityBusHandler.getNearStop(trackPlateNumb)[trackPlateNumb]
            var trackBusStatus = interCityBusHandler.getBusStatus(trackPlateNumb)[trackPlateNumb]
            var trackA2EventType = interCityBusHandler.getA2EventType(trackPlateNumb)[trackPlateNumb]
            var trackRouteName = interCityBusHandler.getSubRouteName(trackPlateNumb)[trackPlateNumb]

            for (i in 0 until getPlateNumb().size) {
                trackPlateNumb = getPlateNumb()[i]
                trackNearStop = interCityBusHandler.getNearStop(trackPlateNumb)[trackPlateNumb]
                trackBusStatus = interCityBusHandler.getBusStatus(trackPlateNumb)[trackPlateNumb]
                trackA2EventType = interCityBusHandler.getA2EventType(trackPlateNumb)[trackPlateNumb]
                trackRouteName = interCityBusHandler.getSubRouteName(trackPlateNumb)[trackPlateNumb]
            }

            info { "Tracker: $trackPlateNumb  $trackNearStop  $trackBusStatus  $trackA2EventType  $trackRouteName" }

            if (trackBusStatus != null) {

                // 追蹤清單
                var tracker = arrayListOf<Tracker>()
                for (i in 0 until getPlateNumb().size) {
                    tracker.add(
                        Tracker(
                            trackNearStop,
                            getPlateNumb()[i],
                            trackBusStatus,
                            trackA2EventType,
                            trackRouteName
                        )
                    )
                }

                runOnUiThread {

                    recyclerView_tracker.layoutManager = LinearLayoutManager(this@InterCityBusSearch)
                    recyclerView_tracker.adapter = TrackerAdapter(this@InterCityBusSearch, tracker)


                    // CardView 滑動刪除項目
                    val swipeTouchListener = SwipeableRecyclerViewTouchListener(recyclerView_tracker,
                        object : SwipeableRecyclerViewTouchListener.SwipeListener {
                            override fun canSwipeLeft(position: Int): Boolean {
                                return true
                            }

                            // 向左滑刪除
                            override fun onDismissedBySwipeLeft(
                                recyclerView: RecyclerView,
                                reverseSortedPositions: IntArray
                            ) {
                                for (position in reverseSortedPositions) {
                                    tracker.removeAt(position)
                                    recyclerView_tracker.adapter?.notifyItemRemoved(position)
                                    /*database.use{
                                        execSQL("delete from Tracker where ROWID = position")
                                    }*/
                                }
                                recyclerView_tracker.adapter?.notifyDataSetChanged()
                            }

                            override fun canSwipeRight(position: Int): Boolean {
                                return true
                            }

                            // 向右滑刪除
                            override fun onDismissedBySwipeRight(
                                recyclerView: RecyclerView,
                                reverseSortedPositions: IntArray
                            ) {
                                for (position in reverseSortedPositions) {
                                    tracker.removeAt(position)
                                    recyclerView_tracker.adapter?.notifyItemRemoved(position)
                                    /*database.use{
                                        execSQL("delete from Tracker where id = position")
                                    }*/
                                }
                                recyclerView_tracker.adapter?.notifyDataSetChanged()
                            }

                        }
                    )

                    recyclerView_tracker.addOnItemTouchListener(swipeTouchListener)
                }

            }

        }
    }
}
