package com.transformer.dashboard.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.transformer.dashboard.R
import com.transformer.dashboard.model.Stats
import com.transformer.dashboard.network.HwMonitorApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class StatsFragment : Fragment(R.layout.fragment_stats) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun getStatsData() {
        val api = HwMonitorApi.retrofitService.getStatsData()

        api.enqueue(object : Callback<Stats> {
            override fun onResponse(call: Call<Stats>, response: Response<Stats>) {
                val stats = response.body()
                Log.i("TAG_TAG", "Got Response")
            }

            override fun onFailure(call: Call<Stats>, t: Throwable) {
                Log.d("TAG_TAG", "Failed: " + t.message)
            }

        })
    }
}