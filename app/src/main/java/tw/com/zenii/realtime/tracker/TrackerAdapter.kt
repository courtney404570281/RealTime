package tw.com.zenii.realtime.tracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tw.com.zenii.realtime.R

class TrackerAdapter (val context: Context, private val trackers: List<Tracker>)
    : RecyclerView.Adapter<TrackerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.cardview_tracker, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.trackers.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bind(trackers[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val nearStop = itemView?.findViewById<TextView>(R.id.txtNearStop)
        private val plateNumb = itemView?.findViewById<TextView>(R.id.txtPlateNumb)
        private val busStatus = itemView?.findViewById<TextView>(R.id.txtBusStatus)
        private val a2EventType = itemView?.findViewById<TextView>(R.id.txtA2EventType)
        private val routeName = itemView?.findViewById<TextView>(R.id.txtRouteName)

        fun bind(tracker: Tracker) {
            nearStop?.text = tracker.nearStop
            plateNumb?.text = tracker.plateNumb
            busStatus?.text = tracker.busStatus
            a2EventType?.text = tracker.a2EventType
            routeName?.text = tracker.routeName
        }
    }

}