package com.transformer.dashboard.model

import com.google.gson.annotations.SerializedName

data class Afterburner(

    @SerializedName("signature") val signature: Long,
    @SerializedName("version") val version: Int,
    @SerializedName("headerSize") val headerSize: Int,
    @SerializedName("entryCount") val entryCount: Int,
    @SerializedName("entrySize") val entrySize: Int,
    @SerializedName("time") val time: Long,
    @SerializedName("entries") val entries: List<Entries>
)