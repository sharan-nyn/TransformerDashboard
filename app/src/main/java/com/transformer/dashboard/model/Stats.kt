package com.transformer.dashboard.model

import com.google.gson.annotations.SerializedName


data class Stats(

	@SerializedName("hwinfo") val hwinfo: Hwinfo,
	@SerializedName("afterburner") val afterburner: Afterburner
)