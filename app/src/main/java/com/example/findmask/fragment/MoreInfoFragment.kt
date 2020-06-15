package com.example.findmask.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import kotlinx.android.synthetic.main.fragment_moreinfo.*
import net.daum.mf.map.api.MapPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import android.widget.EditText
import android.text.TextWatcher
import androidx.lifecycle.LiveData
import com.example.findmask.database.FavoriteDatabase
import com.example.findmask.databinding.FragmentMoreinfoBinding
import kotlin.collections.HashSet

class MoreInfoFragment : Fragment() {

    override fun onResume() {
        super.onResume()

        search_filter.text.clear()
    }

    private var moreInfoList = ArrayList<MoreInfo>()
    private var moreInfoListFavorite = ArrayList<MoreInfo>()

    private var favoriteList = listOf<MoreInfo>()

    private var favoriteDatabase: FavoriteDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMoreinfoBinding.inflate(inflater, container, false)
        val view = binding.root

        var cal = Calendar.getInstance()
        var week = cal.get(Calendar.DAY_OF_WEEK)

        if (week == 1 || week == 7) {
            binding.weekend.setTextColor(Color.rgb(0, 103, 163))
        }
        else if (week == 2) {
            binding.monday.setTextColor(Color.rgb(0, 103, 163))
        }
        else if (week == 3) {
            binding.tuesday.setTextColor(Color.rgb(0, 103, 163))
        }
        else if (week == 4) {
            binding.wednesday.setTextColor(Color.rgb(0, 103, 163))
        }
        else if (week == 5) {
            binding.thursday.setTextColor(Color.rgb(0, 103, 163))
        }
        else if (week == 6) {
            binding.friday.setTextColor(Color.rgb(0, 103, 163))
        }

        var moreInfoRecyclerView: RecyclerView = view.findViewById(R.id.moreInfoRecyclerView)

        var moreInfoAdapter = MoreInfoAdapter()

        val activity = activity

        moreInfoRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        moreInfoRecyclerView.setHasFixedSize(true)
        moreInfoRecyclerView.adapter = moreInfoAdapter

        favoriteDatabase = FavoriteDatabase.getInstance(view.context)

        val runnable = Runnable {
            favoriteList = favoriteDatabase?.favoriteDao()?.getFavorites()!!
        }

        val thread = Thread(runnable)
        thread.start()

        binding.searchFilter.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                moreInfoAdapter.filter(binding.searchFilter.text.toString().toLowerCase())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        try {
            val lm: LocationManager? =
                activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var location: Location? = null

                location = lm!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                // 휴대폰
//                var longitude = location!!.longitude
//                var latitude = location!!.latitude

                // 에뮬레이터 테스트
                var longitude = 127.0342169
                var latitude = 37.5010881

//                var longitude = 128.568975
//                var latitude = 35.8438071

                var isfavorite: Boolean = false

                MaskService.getStoreByGeoInfo(latitude, longitude, 500).enqueue(object : Callback<MaskByGeoInfo> {
                override fun onFailure(call: Call<MaskByGeoInfo>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<MaskByGeoInfo>,
                    response: Response<MaskByGeoInfo>
                ) {
                    moreInfoList.clear()
                    moreInfoListFavorite.clear()
                    if (favoriteList.isNotEmpty()) {
                        for (j in favoriteList.indices) {
                            for (i in 0 until response.body()!!.count) {
                                isfavorite = favoriteList[j].addr == response.body()!!.stores[i].addr
                                if(isfavorite) {
                                    moreInfoListFavorite.add(
                                        MoreInfo(
                                            response.body()!!.stores[i].name,
                                            response.body()!!.stores[i].addr,
                                            response.body()!!.stores[i].remain_stat,
                                            response.body()!!.stores[i].stock_at,
                                            response.body()!!.stores[i].created_at,
                                            isfavorite
                                        ))
                                }

                            }
                        }
                    }
                    else {
                        for (i in 0 until response.body()!!.count) {
                            isfavorite = false
                            moreInfoList.add(
                                MoreInfo(
                                    response.body()!!.stores[i].name,
                                    response.body()!!.stores[i].addr,
                                    response.body()!!.stores[i].remain_stat,
                                    response.body()!!.stores[i].stock_at,
                                    response.body()!!.stores[i].created_at,
                                    isfavorite
                                )
                            )
                        }
                    }

                    for (k in 0 until response.body()!!.count) {
                        Log.d("favoritesize", "" + moreInfoListFavorite.isNotEmpty() + response.body()!!.stores)
                        if(moreInfoListFavorite.isNotEmpty() && response.body()!!.stores[k].remain_stat != null) {
                            if(!moreInfoListFavorite.contains(MoreInfo(
                                    response.body()!!.stores[k].name,
                                    response.body()!!.stores[k].addr,
                                    response.body()!!.stores[k].remain_stat,
                                    response.body()!!.stores[k].stock_at,
                                    response.body()!!.stores[k].created_at,
                                    true))
                            ) {
                                moreInfoList.add(
                                    MoreInfo(
                                        response.body()!!.stores[k].name,
                                        response.body()!!.stores[k].addr,
                                        response.body()!!.stores[k].remain_stat,
                                        response.body()!!.stores[k].stock_at,
                                        response.body()!!.stores[k].created_at,
                                        false
                                    )
                                )
                            }
                        }
                        else if (response.body()!!.stores[k].remain_stat != null){
                            moreInfoList.add(
                                MoreInfo(
                                    response.body()!!.stores[k].name,
                                    response.body()!!.stores[k].addr,
                                    response.body()!!.stores[k].remain_stat,
                                    response.body()!!.stores[k].stock_at,
                                    response.body()!!.stores[k].created_at,
                                    false
                                )
                            )
                        }
                    }

                    moreInfoListFavorite.addAll(moreInfoList)

                    Log.i("listsize", "" + moreInfoListFavorite.size)

                    moreInfoAdapter.setItem(moreInfoListFavorite, view.context)
                }
            })

        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return view
    }
}