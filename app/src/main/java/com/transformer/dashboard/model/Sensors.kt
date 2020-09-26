package com.transformer.dashboard.model

import com.google.gson.annotations.SerializedName

data class Sensors(

    @SerializedName("entryIndex") val entryIndex: Int,
    @SerializedName("sensorId") val sensorId: Long,
    @SerializedName("sensorInst") val sensorInst: Int,
    @SerializedName("sensorNameOriginal") val sensorNameOriginal: String,
    @SerializedName("sensorNameUser") val sensorNameUser: String
)