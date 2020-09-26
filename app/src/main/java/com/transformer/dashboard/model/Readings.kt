package com.transformer.dashboard.model

import com.google.gson.annotations.SerializedName

data class Readings(

    @SerializedName("entryIndex") val entryIndex: Int,
    @SerializedName("readingType") val readingType: Int,
    @SerializedName("sensorIndex") val sensorIndex: Int,
    @SerializedName("readingId") val readingId: Long,
    @SerializedName("labelOriginal") val labelOriginal: String,
    @SerializedName("labelUser") val labelUser: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("value") val value: Double,
    @SerializedName("valueMin") val valueMin: Double,
    @SerializedName("valueMax") val valueMax: Double,
    @SerializedName("valueAvg") val valueAvg: Double
)