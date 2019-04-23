package tw.com.zenii.realtime

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_inter_city_bus_search.*
import tw.com.zenii.realtime.tracker.Tracker
import tw.com.zenii.realtime.tracker.TrackerAdapter
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter
import com.pawegio.kandroid.onQuerySubmit
import com.pawegio.kandroid.runOnUiThread
import kotlinx.android.synthetic.main.cardview_tracker.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.db.select
import org.jetbrains.anko.info
import org.jetbrains.anko.textView

class InterCityBusSearch : AppCompatActivity(), AnkoLogger {

    private val RC_SEARCH = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inter_city_bus_search)

        // 在 searchView 內取值
        search.queryHint = getString(R.string.please_enter_the_route)

            search.onQuerySubmit { query ->
            info { "search: $query" }
            val mongoRunnable = MongoRunnable()
            Thread(mongoRunnable).start()
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (mongoRunnable.handler != null && query != "") {
                val msg = Message()
                msg.obj = query
                mongoRunnable.handler!!.sendMessage(msg)
            }
        }

        var trackNearStop = getSharedPreferences("tracker", Context.MODE_PRIVATE).getString("nearStop", "捷運大橋頭站")
        var trackPlateNumb = getSharedPreferences("tracker", Context.MODE_PRIVATE).getString("plateNumb", "KKA-0925")
        var trackBusStatus = getSharedPreferences("tracker", Context.MODE_PRIVATE).getString("busStatus", "正常")
        var trackA2EventType = getSharedPreferences("tracker", Context.MODE_PRIVATE).getString("a2EventType", "離站")
        var trackRouteName = getSharedPreferences("tracker", Context.MODE_PRIVATE).getString("routeName", "2022")

        if (trackPlateNumb != null) {

            // 追蹤清單
            var tracker_list = mutableListOf(
                Tracker(trackNearStop, trackPlateNumb, trackBusStatus, trackA2EventType, trackRouteName)
            )

            recyclerView_tracker.layoutManager = LinearLayoutManager(this)
            recyclerView_tracker.adapter = TrackerAdapter(this, tracker_list)

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
                            tracker_list.removeAt(position)
                            recyclerView_tracker.adapter?.notifyItemRemoved(position)
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
                            tracker_list.removeAt(position)
                            recyclerView_tracker.adapter?.notifyItemRemoved(position)
                        }
                        recyclerView_tracker.adapter?.notifyDataSetChanged()
                    }

                }
            )

            recyclerView_tracker.addOnItemTouchListener(swipeTouchListener)

        }

    }

    inner class MongoRunnable : Runnable {

        internal var handler: Handler? = null

        @SuppressLint("HandlerLeak")
        override fun run() {
            // 將 Looper 與 worker thread 連結在一起
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }
            // 設定 Handler，讓 producer 可以插入訊息
            handler = object : Handler() {
                // 當訊息被送到 MongoRunnable 時的 callback
                override fun handleMessage(msgIn: Message) {
                    val subRouteId = msgIn.obj.toString()
                    val routeNameResults = ArrayList<String>() // 路線名稱
                    val routeIdResults = ArrayList<String>() // 路線代號
                    var result: String
                    var resultId: String
                    val interCityBusHandler = InterCityBusHandler()

                    val resJa = interCityBusHandler.getRouteSearchResult(subRouteId)
                    for (je in resJa) {
                        val jo = je.getAsJsonObject()
                        result = jo.get("SubRouteID").getAsString() + "\n" + jo.get("Headsign").getAsString()
                        resultId = jo.get("SubRouteID").getAsString()
                        info { "result: $result" }
                        routeNameResults.add(result)
                        routeIdResults.add(resultId)
                    }

                    info { "getTest(): ${interCityBusHandler.getTest()}"  }

                    runOnUiThread {
                        if(routeNameResults.isEmpty()){
                            AlertDialog.Builder(this@InterCityBusSearch)
                                .setTitle(getString(R.string.message))
                                .setMessage(subRouteId + "\t" + getString(R.string.not_found))
                                .setPositiveButton(getString(R.string.search_others)) { dialog, which ->
                                    search.setQuery("", false)
                                    search.isIconified
                                }
                                .show()
                        }

                        val adapter =
                            ArrayAdapter(this@InterCityBusSearch, android.R.layout.simple_list_item_1, routeNameResults)
                        list.adapter = adapter
                        var itemListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            var type = routeIdResults[position].substring(4)
                            var route = routeIdResults[position].substring(0, 4)
                            if (type == "") {
                                route += "0"
                            } else {
                                route = routeIdResults.get(position).substring(0, 5)
                            }
                            info { "itemListener: $route" }

                            val intent = Intent(this@InterCityBusSearch, MapsActivity::class.java)
                            // 設定 RouteId 1818
                            setRouteId(route)
                            info { "setRouteId: $route" }
                            startActivity(intent)
                        }
                        list.onItemClickListener = itemListener

                    }
                }
            }

            // blocking 呼叫，讓 message queue 可發送訊息給 consumer thread
            Looper.loop()
        }
    }

}
