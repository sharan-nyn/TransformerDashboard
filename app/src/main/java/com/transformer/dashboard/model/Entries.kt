package com.transformer.dashboard.model

import com.google.gson.annotations.SerializedName

data class Entries(

	@SerializedName("entryIndex") val entryIndex: Int,
	@SerializedName("name") val name: String,
	@SerializedName("units") val units: String,
	@SerializedName("localName") val localName: String,
	@SerializedName("localUnits") val localUnits: String,
	@SerializedName("digits") val digits: Int,
	@SerializedName("data") val data: Double,
	@SerializedName("minLimit") val minLimit: Double,
	@SerializedName("maxLimit") val maxLimit: Double,
	@SerializedName("flags") val flags: Int
)