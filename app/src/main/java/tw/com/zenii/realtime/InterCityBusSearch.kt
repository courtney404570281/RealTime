package tw.com.zenii.realtime

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_inter_city_bus_search.*
import java.util.*

class InterCityBusSearch : AppCompatActivity() {

    val TAG = InterCityBusSearch::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inter_city_bus_search)

        // 在 searchView 內取值
        search.setQueryHint(getString(R.string.please_enter_the_route))
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("search", query)
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
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

        })
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
                        Log.d("result", result)
                        routeNameResults.add(result)
                        routeIdResults.add(resultId)
                    }

                    runOnUiThread {
                        if(routeNameResults.isEmpty()){
                            AlertDialog.Builder(this@InterCityBusSearch)
                                .setTitle(getString(R.string.message))
                                .setMessage(subRouteId + "\t" + getString(R.string.not_found))
                                .setPositiveButton(getString(R.string.search_others), { dialog, which ->
                                    search.setQuery("", false)
                                    search.isIconified()
                                })
                                .show()
                        }

                        val adapter =
                            ArrayAdapter(this@InterCityBusSearch, android.R.layout.simple_list_item_1, routeNameResults)
                        list.setAdapter(adapter)
                        var itemListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            var type = routeIdResults.get(position).substring(4)
                            var route = routeIdResults.get(position).substring(0, 4)
                            if (type.equals("")) {
                                route += "0"
                            } else {
                                route = routeIdResults.get(position).substring(0, 5)
                            }
                            Log.d(TAG, "itemListener: $route")

                            val intent = Intent(this@InterCityBusSearch, MapsActivity::class.java)
                            intent.putExtra("route", route)
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
