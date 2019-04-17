package tw.com.zenii.realtime.tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tw.com.zenii.realtime.R

class ArrivalAdapter (val context: Context, private val arrivals: List<Arrival>)
    : RecyclerView.Adapter<ArrivalAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.arrival_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.arrivals.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bind(arrivals[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val estimateTime = itemView?.findViewById<TextView>(R.id.txtEstimateTime)
        private val stopName = itemView?.findViewById<TextView>(R.id.txtStopName)

        fun bind(arrival: Arrival) {
            estimateTime?.text = arrival.estimateTime
            stopName?.text = arrival.stopName
        }
    }

}