package tw.com.zenii.realtime.tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.arrival_item_list.view.*
import tw.com.zenii.realtime.R

class ArrivalAdapter (val context: Context, private val arrivals: List<Arrival>)
    : RecyclerView.Adapter<ArrivalAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.arrival_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.arrivals.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.estimateTime.text = arrivals[position].estimateTime
        holder.stopName.text = arrivals[position].stopName
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val estimateTime: TextView = itemView.txtEstimateTime
        val stopName: TextView = itemView.txtStopName
    }

}