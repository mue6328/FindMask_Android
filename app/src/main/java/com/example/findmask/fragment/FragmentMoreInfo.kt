package com.example.findmask.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findmask.R
import com.example.findmask.Utils
import com.example.findmask.adapter.MoreInfoAdapter
import com.example.findmask.model.MaskByGeoInfo
import com.example.findmask.model.MoreInfo
import com.example.findmask.service.MaskService
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentMoreInfo : Fragment() {

    private var maskService: MaskService? = null

    private var moreInfoList = ArrayList<MoreInfo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_moreinfo, container, false)

        initService()

        var moreInfoRecyclerView: RecyclerView = view.findViewById(R.id.moreInfoRecyclerView)

        var moreInfoAdapter = MoreInfoAdapter()

        val activity = activity

        moreInfoRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        moreInfoRecyclerView.setHasFixedSize(true)
        moreInfoRecyclerView.adapter = moreInfoAdapter

        try {
            val lm: LocationManager? =
                activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var location: Location? = null

            if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(
                    activity!!.applicationContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            } else {
                location = lm!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                var longitude = location!!.longitude
                var latitude = location!!.latitude

                maskService!!.getStoreByGeoInfo(latitude, longitude, 500).enqueue(object :
                    Callback<MaskByGeoInfo> {
                    override fun onFailure(call: Call<MaskByGeoInfo>, t: Throwable) {
                        Log.d("error", t.toString())
                    }

                    override fun onResponse(
                        call: Call<MaskByGeoInfo>,
                        response: Response<MaskByGeoInfo>
                    ) {
                        for (i in 0 until response.body()!!.count) {
                            moreInfoList.add(MoreInfo(response.body()!!.stores[i].name, response.body()!!.stores[i].remain_stat,
                                response.body()!!.stores[i].stock_at, response.body()!!.stores[i].created_at))
                        }
                        moreInfoAdapter.setItem(moreInfoList)

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
        } catch (e: SecurityException) {
            e.printStackTrace()
        }




        return view
    }

    private fun initService() {
        maskService = Utils.retrofit_MASK.create(MaskService::class.java)
    }
}