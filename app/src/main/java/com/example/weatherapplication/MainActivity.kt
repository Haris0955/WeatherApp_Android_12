package com.example.weatherapplication


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

import com.google.android.material.textfield.TextInputEditText

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

import android.widget.Toast
import com.bumptech.glide.Glide
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private lateinit var homeRL: RelativeLayout
private lateinit var loadingPB: ProgressBar
private lateinit var cityNameTV: TextView
private lateinit var temperatureTV: TextView
private lateinit var conditionTV: TextView
private lateinit var cityEdit: TextInputEditText
private lateinit var backIv: ImageView
private lateinit var iconIv: ImageView
private lateinit var searchIv: ImageView
private lateinit var todaysDateTV: TextView
private lateinit var windSpeedTV: TextView
private lateinit var humidityTV: TextView



class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the activity fullscreen using the FLAG_LAYOUT_NO_LIMITS flag
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_main)

        homeRL = findViewById(R.id.idRLHome)

        cityNameTV = findViewById(R.id.idTVCityName)
        temperatureTV = findViewById(R.id.idTVTemperature)
        conditionTV = findViewById(R.id.idTVCondition)
        backIv = findViewById(R.id.idIVBack)
        cityEdit = findViewById(R.id.idTIEditCity)
        iconIv = findViewById(R.id.idIVIcon)
        searchIv = findViewById(R.id.idIVSearch)

        todaysDateTV = findViewById(R.id.idTodaysDate)
        windSpeedTV = findViewById(R.id.idTVWindSpeed)
        humidityTV = findViewById(R.id.idTVHumidity)

        sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)

        val lastCity = sharedPreferences.getString("lastCity", "")
        cityEdit.setText(lastCity)


        searchIv.setOnClickListener {
            val city = cityEdit.text.toString()

            if (city.isNotEmpty()) {
                // Save the city name to SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("lastCity", city)
                editor.apply()

                // Call the weather API
                getWeatherInfo(city)
            } else {
                Toast.makeText(this, "Please enter city name", Toast.LENGTH_SHORT).show()
            }

        }

    }



//https://api.openweathermap.org/data/2.5/weather?q=london&appid=a47201e4489960ec1b46a9eccc9920cf&units=metric

    private fun getWeatherInfo(cityName: String) {
        val apiKey = "e9230b77c54ba7eb2f21aea9012de095"
        val BASE_URL = "https://api.openweathermap.org/"

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)

        val call = weatherService.getWeather(cityName, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                try {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse != null) {
                            // Update UI components using weatherResponse data
                            cityNameTV.text = weatherResponse.name
                            val roundedTemperature = weatherResponse.main.temp.toInt()
                            temperatureTV.text = "$roundedTemperature °C"
                            conditionTV.text = "Condition: "+ weatherResponse.weather[0].description

                            val weatherIconResource = when (weatherResponse.weather[0].description.lowercase()) {
                                "clear sky" -> R.drawable.clear_sky
                                "few clouds" -> R.drawable.few_clouds
                                "scattered clouds" -> R.drawable.scattered_clouds
                                "broken clouds" -> R.drawable.broken_clouds
                                "fog" -> R.drawable.fog
                                "haze" -> R.drawable.haze
                                "heavy rain" -> R.drawable.heavy_rain
                                "heavy snow" -> R.drawable.heavy_snow
                                "light rain" -> R.drawable.light_rain
                                "light snow" -> R.drawable.light_snow
                                "mist" -> R.drawable.mist
                                "moderate rain" -> R.drawable.moderate_rain
                                "moderate snow" -> R.drawable.moderate_snow
                                "overcast clouds" -> R.drawable.overcast_clouds
                                "thunderstorm" -> R.drawable.thunderstorm

                                // Add more cases for other weather descriptions
                                else -> R.drawable.default_icon
                            }

                            Glide.with(this@MainActivity)
                                .load(weatherIconResource)
                                .into(iconIv)

                            // Format and set today's date
                            val timestamp = weatherResponse.dt * 1000L // Convert to milliseconds
                            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                            val formattedDate = dateFormat.format(Date(timestamp))
                            todaysDateTV.text = "$formattedDate - Today's Weather Forecast"

                            // Set wind speed and humidity
                            windSpeedTV.text = "Wind Speed: ${weatherResponse.wind.speed} m/s"
                            humidityTV.text = "Humidity: ${weatherResponse.main.humidity}%"
                        }
                    } else {
                        // Handle unsuccessful response
                        cityNameTV.text = response.message()
                    }
                } catch (e: Exception) {
                    // Handle any exceptions that occur during processing
                    cityNameTV.text = "Exception occurred: ${e.message}"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Handle network failure
                cityNameTV.text = "Network failure: ${t.message}"
            }
        })
    }


}



