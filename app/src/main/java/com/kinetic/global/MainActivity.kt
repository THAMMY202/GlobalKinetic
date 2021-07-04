package com.kinetic.global

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dvt.weather.utils.Constants

import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    var location : Location? = null
    var reload :Boolean = true


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
            reload = true
            getLocation()
        }
    }

    private fun getWeatherByLocation(location: Location) {
        //TODO Check location values , hit api and conver respond
        //val url ="https://api.openweathermap.org/data/2.5/weather?lat=35&lon=139&units=metric&appid=636e6df4d5c9c1d14640e9ff8a629364"
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&units=metric&appid=${Constants.API_KEY}"

        //init items
      /*  val textViewCurrentTemp: TextView = findViewById(R.id.temperature_text)
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

                        spinner?.visibility = View.GONE


                    } catch (e: JSONException) {
                        Log.i("Place Temperature", e.toString())
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
                        spinner?.visibility = View.GONE
                    }

                },
                { error -> // error listener
                    Log.i("Place Temperature", error.toString())
                    Toast.makeText(applicationContext, "City name is incorrect", Toast.LENGTH_SHORT)
                            .show()

                }
        )

        // add network request to volley queue
        VolleySingleton.getInstance(applicationContext).addToRequestQueue(request)*/
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

    override fun onLocationChanged(loc: Location) {

        location = loc

        if(reload){
            weatherTask().execute()
        }
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(applicationContext, "Gps Disabled", Toast.LENGTH_SHORT)
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
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

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            return try{
                URL("https://api.openweathermap.org/data/2.5/weather?lat=${location?.latitude}&lon=${location?.longitude}&units=metric&appid=${Constants.API_KEY}").readText(
                        Charsets.UTF_8
                )
            }catch (e: Exception){
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min Temp: " + main.getString("temp_min")+"°C"
                val tempMax = "Max Temp: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

                reload = false

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }

        }
    }
}