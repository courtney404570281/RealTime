package tw.com.zenii.realtime

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.Constraints
import androidx.viewpager.widget.ViewPager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tw.com.zenii.realtime.tab.Arrival
import tw.com.zenii.realtime.tab.GoFragment
import tw.com.zenii.realtime.tab.PagerAdapter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // 設定
    private val handler = InterCityBusHandler()
    private lateinit var mMap: GoogleMap

    // 變數
    //val route = intent.getStringExtra("route") // 1818A 傳入 Map 的值
    private var mapRoute = "181801" // 1818A1
    var tabRoute: String = "1818A" // 傳進 tab 是 1818A 傳出 tab 是 1818A1

    // 常數
    private val TAG = MapsActivity::class.java.simpleName
    private val DEFAULTE_ZOOM = 10.0f // 初始鏡頭
    private val LINE_WIDTH = 7.0f // 地圖上之線寬

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // 設定狀態條之背景色
        window.statusBarColor = Color.rgb(236, 167, 44)

         // 設定地圖
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // 每 10 秒更新一次資料
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            GlobalScope.launch {
                markBus()
            }
            // 測試 10s
            Log.d(TAG, "MapsActivityTimer: ${Date()}")
        }, 0, 10, TimeUnit.SECONDS)

        // 繪製地圖
        mapFragment.getMapAsync(this)

        // 繪製 Tabs
        setupViewPager(pager as ViewPager)

    }

    // 設定 ViewPager
    private fun setupViewPager(viewPager: ViewPager) {
        GlobalScope.launch {
            // 確認是否有來回
            val title = ArrayList<String>()
            title.add("往 A 地")
            title.add("往 B 地")
            runOnUiThread {
                val fragmentAdapter = PagerAdapter(supportFragmentManager)
                for(i in 0 until title.size) {
                    fragmentAdapter.addFragment(GoFragment(), title[i])
                }
                viewPager.adapter = fragmentAdapter
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
        }
    }

    // 初始鏡頭
    private fun initiateCamera() {
        // 站牌所在位置之經緯度
        val stopPositions = handler.getStopPosition(mapRoute)
        runOnUiThread {
            // 鏡頭初始位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stopPositions[0], DEFAULTE_ZOOM))
        }
    }

    // 標示地圖上之各站站牌
    private fun markStops() {

        // 站牌所在位置之經緯度
        val stopPositions = handler.getStopPosition(mapRoute)
        // 各站站名
        val stopNames = handler.getStopName(mapRoute)

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

    // 標示地圖上行駛中之客運
    private fun markBus() {

        // 站牌所在位置之經緯度
        val stopPositions = handler.getStopPosition(mapRoute)
        // 各站站名
        val stopNames = handler.getStopName(mapRoute)
        // 客運目前所在之經緯度
        val busPositions = handler.getBusPosition(mapRoute)
        // 客運之車牌號碼
        val plateNumbs = handler.getPlateNumb(mapRoute)

        runOnUiThread {

            // 清除目前所有客運
            mMap.clear()

            // 客運現在位置 Bus Marker
            for (i in 0 until busPositions.size) {

                Log.d(TAG, "busPosition : ${busPositions[i]}")

                mMap.addMarker(
                    MarkerOptions()
                        .position(busPositions[i])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus))
                        .title(plateNumbs[busPositions[i]])
                )

            }
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
