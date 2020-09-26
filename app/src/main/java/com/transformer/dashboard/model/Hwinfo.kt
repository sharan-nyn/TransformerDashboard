package com.transformer.dashboard.model

import com.google.gson.annotations.SerializedName

data class Hwinfo(

	@SerializedName("signature") val signature: Long,
	@SerializedName("version") val version: Int,
	@SerializedName("revision") val revision: Int,
	@SerializedName("pollTime") val pollTime: Long,
	@SerializedName("sensorOffset") val sensorOffset: Int,
	@SerializedName("sensorSize") val sensorSize: Int,
	@SerializedName("sensorCount") val sensorCount: Int,
	@SerializedName("readingOffset") val readingOffset: Int,
	@SerializedName("readingSize") val readingSize: Int,
	@SerializedName("readingCount") val readingCount: Int,
	@SerializedName("sensors") val sensors: List<Sensors>,
	@SerializedName("readings") val readings: List<Readings>
)