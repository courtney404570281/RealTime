package tw.com.zenii.realtime

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewParent
import androidx.annotation.RequiresApi
import androidx.viewpager.widget.ViewPager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tw.com.zenii.realtime.tab.BackFragment
import tw.com.zenii.realtime.tab.GoFragment
import tw.com.zenii.realtime.tab.PagerAdapter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val TAG = MapsActivity::class.java.simpleName
    private val handler = InterCityBusHandler()
    private lateinit var mMap: GoogleMap
    //val route = intent.getStringExtra("route") // 1818A 傳入 Map 的值
    private var mapRoute = "1818A2" // 1818A1
    var tabRoute = "1818A" // 傳進 tab 是 1818A 傳出 tab 是 1818A1
    private val DEFAULTE_ZOOM = 10.0f
    private val LINE_WIDTH = 7.0f

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        window.statusBarColor = Color.rgb(236, 167, 44)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            GlobalScope.launch {

                val busPositions = handler.getBusPosition(mapRoute)
                val plateNumbs = handler.getPlateNumb(mapRoute)

                runOnUiThread {

                    // 客運現在位置 Bus Marker
                    for (i in 0 until busPositions.size) {

                        Log.d(TAG, "busPosition : ${busPositions[i]}")

                        mMap.addMarker(MarkerOptions()
                            .position(busPositions[i])
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus))
                            .title(plateNumbs[busPositions[i]]))
                    }
                }
            }
            Log.d("Time", Date().toString())
        }, 0, 10, TimeUnit.SECONDS)

        mapFragment.getMapAsync(this)

        setupViewPager(pager as ViewPager)
    }

    override fun onMapReady(googleMap: GoogleMap) {

       // mMap.clear()
        mMap = googleMap
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.style_json)
        )
        GlobalScope.launch {
            val stopPositions = handler.getStopPosition(mapRoute)
            val stopNames = handler.getStopName(mapRoute)

            runOnUiThread {
                // 鏡頭初始位置
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stopPositions[0], DEFAULTE_ZOOM))

                // 站牌位置 Stop Markers
                for (i in 0 until stopPositions.size) {
                    mMap.addMarker(MarkerOptions()
                        .position(stopPositions[i])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus_stop))
                        .title(stopNames[stopPositions[i]]))
                }

                // 劃線的地方 PolyLine Of Stops
                for (i in 0 until stopPositions.size - 1) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .addAll(stopPositions)
                            .color(Color.rgb(91, 142, 125))
                            .width(LINE_WIDTH))
                }
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val fragmentAdapter = PagerAdapter(supportFragmentManager)
        fragmentAdapter.addFragment(GoFragment(), "Go")
        fragmentAdapter.addFragment(BackFragment(), "Back")

        viewPager.adapter = fragmentAdapter
    }
}
