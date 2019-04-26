package tw.com.zenii.realtime

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.alert
import tw.com.zenii.realtime.category.Category
import tw.com.zenii.realtime.category.CategoryAdapter

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        window.statusBarColor = getColor(R.color.colorPrimaryDark)
        //setSupportActionBar(toolbar)

        /*val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)*/

        // RecyclerView 之 Adapter
        setAdapter()

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            alert("確定要離開？"){
                positiveButton("是") { finish() }
                negativeButton("再看看！") { null }
            }
        } else {
            super.onBackPressed()
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }*/

    // 設定（右上角）設定
    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    // 設定選單
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

       /* when (item.itemId) {
            R.id.nav_camera -> {

            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)*/
        return true
    }

    // 設定 RecyclerView 之 Adapter
    private fun setAdapter() {

        // 各種交通工具列表
        var catogories = mutableListOf(
            Category(getString(R.string.bus), R.drawable.bus), // 客運
            Category(getString(R.string.plane), R.drawable.plane), // 飛機
            Category(getString(R.string.bike), R.drawable.bike), // 自行車
            Category(getString(R.string.railway), R.drawable.train) // 火車
        )

        recyclerView.adapter = CategoryAdapter(this, catogories)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide_from_right)
        recyclerView.layoutAnimation = animation
    }
}
