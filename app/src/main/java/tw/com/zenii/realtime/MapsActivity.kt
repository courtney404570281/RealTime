package tw.com.zenii.realtime

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.pawegio.kandroid.runOnUiThread
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Route
import tw.com.zenii.realtime.tab.GoFragment
import tw.com.zenii.realtime.tab.PagerAdapter
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.NonCancellable.isCancelled
import org.jetbrains.anko.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.support.v4.viewPager


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, AnkoLogger {

    // 設定
    private val handler = InterCityBusHandler()
    private lateinit var mMap: GoogleMap

    // 常數
    private val DEFAULTE_ZOOM = 14.0f // 初始鏡頭
    private val LINE_WIDTH = 7.0f // 地圖上之線寬
    private var first = true
    private var once = true

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val route = getRouteId() // 1818A 傳入 Map 的值
        info { "route: $route" }

        // 設定狀態條之背景色
        window.statusBarColor = Color.rgb(236, 167, 44)

        // 設定地圖
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // 繪製地圖
        mapFragment.getMapAsync(this)

        // 每 5 秒更新一次資料
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            GlobalScope.launch {
                markers()
            }
            // 測試 5s
            info { "MapsActivityTimer: ${Date()}" }
        }, 0, 5, TimeUnit.SECONDS)

        // 繪製 Tabs
        setupViewPager(pager as ViewPager)

    }


    // 設定 ViewPager
    private fun setupViewPager(viewPager: ViewPager) {
        GlobalScope.launch {
            // 確認是否有來回
            val title = ArrayList<String>()
            title.add("往 ${handler.getDestination(getSearchRouteId())}")
//            info { "Destination: 往 ${handler.getDestination(getSearchRouteId())}" }
            title.add("往 ${handler.getDeparture(getSearchRouteId())}")
            runOnUiThread {

                    val fragmentAdapter = PagerAdapter(supportFragmentManager)
                    for(i in 0 until title.size) {
                        fragmentAdapter.addFragment(GoFragment(), title[i])
                    }
                    viewPager.adapter = fragmentAdapter
                    viewPager.addOnPageChangeListener(onPageChangeListener(getRouteId()))

            }
        }
    }

    // tab 選單設定
    private fun onPageChangeListener(route: String): ViewPager.OnPageChangeListener {
        return object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (first && positionOffset == 0f && positionOffsetPixels == 0) {
                    onPageSelected(0)
                    first = false
                }
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> setMapRouteId(route + "1")
                    1 -> setMapRouteId(route + "2")
                }
                info{ "setMapRouteId: ${getMapRouteId()}" }
                once = true
                initiateCamera()
                markers()
            }

        }
    }

    // 初始地圖設定
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        // 設定地圖主題
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.style_json)
        )
        GlobalScope.launch {
            initiateCamera()
            markStops()
            markBus()
        }
    }

    // 初始鏡頭
    private fun initiateCamera() {

        GlobalScope.launch {
            // 站牌所在位置之經緯度
            val stopPositions = handler.getStopPosition(getMapRouteId())
            runOnUiThread {
                // 鏡頭初始位置
                if (stopPositions.isNotEmpty() && once){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stopPositions[0], DEFAULTE_ZOOM))
                    once = false
                }
            }
        }
    }

    // 標示地圖上之各站站牌
    private fun markStops() {

        GlobalScope.launch {
            // 站牌所在位置之經緯度
            val stopPositions = handler.getStopPosition(getMapRouteId())
            // 各站站名
            val stopNames = handler.getStopName(getMapRouteId())
            // 真實地圖
            val realRoutes = handler.getRealRoute()

            runOnUiThread {

                // 站牌位置 Stop Markers
                for (i in 0 until stopPositions.size) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(stopPositions[i])
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus_stop))
                            .title(stopNames[stopPositions[i]])
                    )
                }

                // 劃線的地方 PolyLine Of Stops
                for (i in 0 until stopPositions.size - 1) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .addAll(stopPositions)
                            .color(Color.rgb(91, 142, 125))
                            .width(LINE_WIDTH)
                    )
                }
            }
        }
    }

    // 標示地圖上行駛中之客運
    private fun markBus() {

        GlobalScope.launch {
            // 客運目前所在之經緯度
            val busPositions = handler.getBusPosition(getMapRouteId())
            // 客運之車牌號碼
            val plateNumbs = handler.getPlateNumb(getMapRouteId())

            runOnUiThread {

                val busMarkerList = arrayOfNulls<Marker>(busPositions.size)

                // 客運現在位置 Bus Marker
                for (i in 0 until busPositions.size) {

                    info{ "busPosition : ${busPositions[i]}" }

                    busMarkerList[i] = mMap.addMarker(
                        MarkerOptions()
                            .position(busPositions[i])
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus))
                            .title(plateNumbs[busPositions[i]])
                    )
                }

                mMap!!.setOnMarkerClickListener { marker ->

                    for (i in 0 until busPositions.size) {
                        if (marker == busMarkerList[i]) {
                            val trackPlateNumb = plateNumbs[busPositions[i]]

                            alert("這台車牌是：$trackPlateNumb", "是否追蹤此車？") {
                                positiveButton("是") {
                                    GlobalScope.launch {

                                        setPlateNumb(trackPlateNumb!!)

                                        runOnUiThread {
                                            val intent = Intent(this@MapsActivity, InterCityBusSearch::class.java)
                                            intent.putExtra("trackPlateNumb", trackPlateNumb)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                }
                                negativeButton("否") { null }
                            }.show()
                        }
                    }

                    false
                }
            }
        }
    }

    // 更新所有 markers
    private fun markers() {

        GlobalScope.launch {
            runOnUiThread {
                Thread.sleep(500L)
                if(::mMap.isInitialized){
                    // 清除目前所有客運
                    mMap.clear()
                }
            }
            markStops()
            markBus()
        }
    }
}

