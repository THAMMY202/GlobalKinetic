package com.kinetic.global.models
import com.google.gson.annotations.SerializedName

data class Main(
    @SerializedName("feels_like")
    var feelsLike: Double,
    var humidity: Int,
    var pressure: Int,
    var temp: Double,
    @SerializedName("temp_max")
    var tempMax: Double,
    @SerializedName("temp_min")
    var tempMin: Double
)