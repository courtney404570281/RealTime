package tw.com.zenii.realtime.tab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Constraints.TAG
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_go.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread
import tw.com.zenii.realtime.InterCityBusHandler
import tw.com.zenii.realtime.R
import tw.com.zenii.realtime.getMapRouteId
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GoFragment : Fragment() , AnkoLogger {

    companion object {
        val instance: GoFragment by lazy {
            GoFragment()
        }
    }

    private val handler = InterCityBusHandler()
    val arrivals = mutableListOf<Arrival>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_go, container, false)

        GlobalScope.launch {
            // 每 1 秒更新一次資料
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                listStop(view)
                // 測試 1s
                info { "GoFragmentTimer: ${Date()}" }
            }, 0, 1, TimeUnit.SECONDS)
        }

        return view
    }

    // 列出所有站牌與到站時間
    private fun listStop(view: View) {

        GlobalScope.launch {
            val tabRoute = activity!!.getMapRouteId()
            //val tabRoute = "181801"

            // 各站站牌名稱
            val stopName = handler.getStopNames(tabRoute)
            // 預計到站之時間
            val estimateTime = handler.getEstimateTime(tabRoute)

            // 清除所有站牌與預估到站時間資料
            if (arrivals.isNotEmpty()) {
                arrivals.clear()
            }

            if (estimateTime.size == stopName.size) {
                for (i in 0 until estimateTime.size) {
                    arrivals.add(Arrival(estimateTime[i], stopName[i]))
                    info { "runOnUiThread: ${estimateTime[i]} ${stopName[i]}" }
                }
            }

            activity!!.runOnUiThread{
                // 繪製 RecyclerView
                setAdapter(view)
            }
        }
    }

    // 繪製 RecyclerView
    private fun setAdapter(view: View) {
        // 設定 RecyclerView 之 Adapter
        view?.recyclerView_go?.adapter = ArrivalAdapter(activity!!, arrivals)
        view?.recyclerView_go?.layoutManager = LinearLayoutManager(activity!!)
    }
}