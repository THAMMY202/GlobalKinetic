package com.kinetic.global

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.dvt.weather.utils.Constants
import com.google.gson.Gson
import com.kinetic.global.models.Main
import com.kinetic.global.utils.VolleySingleton
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION) !==
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        getLocation()

        findViewById<Button>(R.id.location_button).setOnClickListener {
            getLocation()
        }
    }

    private fun getWeatherByLocation(location: Location) {
        //TODO Check location values , hit api and conver respond
        //val url ="https://api.openweathermap.org/data/2.5/weather?lat=35&lon=139&units=metric&appid=636e6df4d5c9c1d14640e9ff8a629364"
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&units=metric&appid=${Constants.API_KEY}"

        //init items
        val textViewCurrentTemp: TextView = findViewById(R.id.temperature_text)
        val textViewMaxTemp: TextView = findViewById(R.id.max_text)
        val textViewMinTemp: TextView = findViewById(R.id.min_text)
        val textViewFeelTemp: TextView = findViewById(R.id.feeling_text)

        val request = JsonObjectRequest(
                Request.Method.GET, // method
                url, // url
                null, // json request
                { response -> // response listener
                    try {
                        val obj: JSONObject = response
                        val json = obj.getJSONObject("main")
                        val gson = Gson()

                        var placeTemp: Main = gson.fromJson(json.toString(), Main::class.java)
                        Log.i("Place Temperature", placeTemp.temp.toString())

                        textViewCurrentTemp.text = ("${placeTemp.temp} ° ")
                        textViewMaxTemp.text = ("Max :  ${placeTemp.tempMax} ° ")
                        textViewMinTemp.text = ("Min :  ${placeTemp.tempMin} ° ")
                        textViewFeelTemp.text = ("it feels like ${placeTemp.feelsLike} ° ")

                        //loadingDialog.dismissDialog()

                    } catch (e: JSONException) {
                        Log.i("Place Temperature", e.toString())
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
                        //loadingDialog.dismissDialog()
                    }

                },
                { error -> // error listener
                    Log.i("Place Temperature", error.toString())
                    Toast.makeText(applicationContext, "City name is incorrect", Toast.LENGTH_SHORT)
                            .show()
                    //loadingDialog.dismissDialog()
                }
        )

        // add network request to volley queue
        VolleySingleton.getInstance(applicationContext).addToRequestQueue(request)
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode
            )
        }

        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)
    }

    override fun onLocationChanged(location: Location) {

        getWeatherByLocation(location)
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(applicationContext, "Gps Disabled", Toast.LENGTH_SHORT)
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION) ===
                                    PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}