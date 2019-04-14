package tw.com.zenii.realtime.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tw.com.zenii.realtime.R

class CategoryAdapter (val context: Context, val category: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cardview_main, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.category.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(category[position])

    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView?.findViewById(R.id.txt_category)
        private val img :ImageView = itemView?.findViewById(R.id.img_background)

        fun bind(category: Category) {
            name?.text = category.name
            img?.setImageResource(category.img)
        }
    }
}