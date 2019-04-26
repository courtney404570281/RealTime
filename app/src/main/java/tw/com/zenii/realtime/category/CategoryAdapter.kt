package tw.com.zenii.realtime.category

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.pawegio.kandroid.inflateLayout
import kotlinx.android.synthetic.main.cardview_main.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import tw.com.zenii.realtime.InterCityBusSearch
import tw.com.zenii.realtime.MainActivity
import tw.com.zenii.realtime.R

class CategoryAdapter (val context: Context, private val category: MutableList<Category>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() , AnkoLogger{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = context.inflateLayout(R.layout.cardview_main)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.category.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = category[position].name
        holder.img.setImageResource(category[position].img)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, InterCityBusSearch::class.java)
            when (position) {
                0 -> {
                    startActivity(context, intent, null)
                }
            }
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.txt_category
        val img :ImageView = itemView.img_background
    }
}
