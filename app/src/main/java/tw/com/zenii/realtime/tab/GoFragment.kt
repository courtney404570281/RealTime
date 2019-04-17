package tw.com.zenii.realtime.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_go.view.*
import tw.com.zenii.realtime.R

class GoFragment: Fragment() {

    companion object{
        val instance: GoFragment by lazy {
            GoFragment()
        }
    }

    var arrivals = mutableListOf(
        Arrival("Wed", "scattered clouds"),
        Arrival("Thu", "light rain"),
        Arrival("Fri", "light rain")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_go, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAdapter(view)
    }

    private fun setAdapter(view: View) {
        // 設定 RecyclerView 之 Adapter
        view?.recyclerView_go?.adapter = ArrivalAdapter(this!!.activity!!, arrivals)
        view?.recyclerView_go?.layoutManager = LinearLayoutManager(this!!.activity!!)
    }
}