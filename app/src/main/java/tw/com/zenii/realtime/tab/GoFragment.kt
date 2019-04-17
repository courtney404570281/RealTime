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
import tw.com.zenii.realtime.InterCityBusHandler
import tw.com.zenii.realtime.R

class GoFragment: Fragment() {

    companion object{
        val instance: GoFragment by lazy {
            GoFragment()
        }
    }

    private val handler = InterCityBusHandler()
    val tabRoute = "181801"
    val arrivals = mutableListOf<Arrival>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_go, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        GlobalScope.launch {
            val stopName = handler.getStopNames(tabRoute)
            val estimateTime = handler.getEstimateTime(tabRoute)
            activity?.runOnUiThread{
                for(i in 0 until estimateTime.size) {
                    arrivals.add(Arrival(estimateTime[i], stopName[i]))
                    Log.d(TAG, "runOnUiThread: ${estimateTime[i]} ${stopName[i]}")
                }
                setAdapter(view)
            }
        }
    }

    private fun setAdapter(view: View) {
        // 設定 RecyclerView 之 Adapter
        view?.recyclerView_go?.adapter = ArrivalAdapter(this!!.activity!!, arrivals)
        view?.recyclerView_go?.layoutManager = LinearLayoutManager(this!!.activity!!)
    }
}