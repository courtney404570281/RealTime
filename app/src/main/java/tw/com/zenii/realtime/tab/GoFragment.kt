package tw.com.zenii.realtime.tab

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_go.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import tw.com.zenii.realtime.InterCityBusHandler
import tw.com.zenii.realtime.getMapRouteId
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import android.view.animation.AnimationUtils.loadLayoutAnimation
import org.jetbrains.anko.support.v4.runOnUiThread


class GoFragment : Fragment() , AnkoLogger {

    companion object {
        val instance: GoFragment by lazy {
            GoFragment()
        }
    }

    private val handler = InterCityBusHandler()
    private val arrivals = mutableListOf<Arrival>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(tw.com.zenii.realtime.R.layout.fragment_go, container, false)

        GlobalScope.launch {
            // 每 5 秒更新一次資料
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                if(activity!=null){
                    listStop(view)
                    // 測試 5s
                    info { "GoFragmentTimer: ${Date()}" }
                }
            }, -2, 5, TimeUnit.SECONDS)
        }

        return view
    }

    // 列出所有站牌與到站時間
    private fun listStop(view: View) {

            GlobalScope.launch {
                val tabRoute = activity!!.getMapRouteId()
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

                if (activity != null) {
                    activity?.runOnUiThread{
                        // 繪製 RecyclerView
                        setAdapter(view)
                    }
                }
            }
    }

    // 繪製 RecyclerView
    private fun setAdapter(view: View) {
        // 設定 RecyclerView 之 Adapter
        view?.recyclerView_go?.adapter = ArrivalAdapter(activity!!, arrivals)
        view?.recyclerView_go?.layoutManager = LinearLayoutManager(activity!!)

        val animation = loadLayoutAnimation(activity, tw.com.zenii.realtime.R.anim.layout_animation_fall_down)
        view?.recyclerView_go?.layoutAnimation = animation

    }
}