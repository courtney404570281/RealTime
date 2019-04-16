package tw.com.zenii.realtime.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_go.*
import tw.com.zenii.realtime.R

class GoFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_go, container, false)
        setupAdapter()
    }

    var arrivalList = mutableListOf(
        Arrival("Wed", "scattered clouds"),
        Arrival("Thu", "light rain"),
        Arrival("Fri", "light rain")
    )

    private fun setupAdapter() {
        recyclerView_go.adapter = ArrivalAdapter(this!!.activity!!, arrivalList)
        recyclerView_go.layoutManager = LinearLayoutManager(this!!.activity!!)
    }
}