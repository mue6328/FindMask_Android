package com.example.findmask.fragment

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.findmask.R
import com.example.findmask.Utils
import com.example.findmask.model.CoronaInfo
import com.example.findmask.model.MaskByGeoInfo
import com.example.findmask.service.CoronaService
import com.example.findmask.service.MaskService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import net.daum.mf.map.api.MapCircle
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainFragment : Fragment() {

    private var maskService: MaskService? = null

   // private var gpstracker: GpsTracker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_main, container, false);

        var map = view.findViewById<RelativeLayout>(R.id.map_view)
        val activity = activity
        initService()


            try {
                val lm : LocationManager? = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                var location : Location? = null

                if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(activity!!.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        0)
                }
                else {
                    location = lm!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    var longitude = location!!.longitude
                    var latitude = location!!.latitude

                    var g: Geocoder = Geocoder(activity.applicationContext)



                    val mapView = MapView(activity)

                    val mapViewContainer = map as ViewGroup

                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true)

                    mapViewContainer.addView(mapView)

                    Log.d("gps", "위도: " + latitude + " 경도: " + longitude + g.getFromLocation(latitude, longitude, 10))

                    var marker = MapPOIItem()
                    marker.itemName = "현재 위치"
                    marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                    marker.customImageResourceId = R.drawable.baseline_room_black_36dp
                    marker.markerType = MapPOIItem.MarkerType.CustomImage
                    marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
                    //MapPOIItem.MarkerType

                    var circle = MapCircle(
                        MapPoint.mapPointWithGeoCoord(latitude, longitude),
                        500,
                        Color.argb(128, 0, 0, 0),
                        Color.argb(64, 0, 255, 255)
                    )

                    mapView.addPOIItem(marker)
                    mapView.addCircle(circle)
//                    mapView.setPOIItemEventListener(MapView.POIItemEventListener() {
//
//                    })

                    maskService!!.getStoreByGeoInfo(latitude, longitude, 500).enqueue(object :
                        Callback<MaskByGeoInfo> {
                        override fun onFailure(call: Call<MaskByGeoInfo>, t: Throwable) {
                            Log.d("error",t.toString())
                        }

                        override fun onResponse(
                            call: Call<MaskByGeoInfo>,
                            response: Response<MaskByGeoInfo>
                        ) {
                            for(i in 0 until response.body()!!.count) {
                                var marker = MapPOIItem()
                                marker.itemName = response.body()!!.stores[i].name
                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(response.body()!!.stores[i].lat.toDouble(),
                                    response.body()!!.stores[i].lng.toDouble())
                                marker.customImageResourceId = R.drawable.baseline_room_black_36dp
                                marker.markerType = MapPOIItem.MarkerType.CustomImage

                                mapView.addPOIItem(marker)
                            }
//                            gpsTest.setText(
//                                response.body().toString() + response.code() + response.message() +
//                                "위도: " + longitude + "\n" +
//                                        "경도: " + latitude)

//                            lm.requestLocationUpdates(
//                                LocationManager.GPS_PROVIDER,
//                                1000,
//                                1.0f,
//                                locationListener)
//                            lm.requestLocationUpdates(
//                                LocationManager.NETWORK_PROVIDER,
//                                1000,
//                                1.0f,
//                                locationListener)
                        }
                    })
                }
            }
            catch (e: SecurityException) {
                e.printStackTrace()
            }


        return view
    }



    private fun initService() {
        maskService = Utils.retrofit_MASK.create(MaskService::class.java)
    }

}