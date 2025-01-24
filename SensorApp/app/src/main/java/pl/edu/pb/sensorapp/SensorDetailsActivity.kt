package pl.edu.pb.sensorapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SensorDetailsActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorLightTextView: TextView
    private lateinit var locationTextView: TextView
    private var sensorLight: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_details)

        // Inicjalizacja widoków
        sensorLightTextView = findViewById(R.id.sensor_light_label)
        locationTextView = findViewById(R.id.location_text_view)
        val getLocationButton: Button = findViewById(R.id.get_location_button)

        // Inicjalizacja SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (sensorLight == null) {
            sensorLightTextView.text = getString(R.string.missing_sensor)
        }

        // Inicjalizacja FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obsługa przycisku lokalizacji
        getLocationButton.setOnClickListener {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                locationTextView.text = getString(R.string.location_template, latitude, longitude)
            } else {
                locationTextView.text = getString(R.string.no_location)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sensorLight?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            sensorLightTextView.text = getString(R.string.sensor_value_template, lightValue)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nie wymaga implementacji
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}



