package pl.edu.pb.sensorapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val TAG = "LocationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        // Inicjalizacja widoków
        locationTextView = findViewById(R.id.location_text_view)
        val getLocationButton: Button = findViewById(R.id.get_location_button)

        // Inicjalizacja FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obsługa kliknięcia przycisku
        getLocationButton.setOnClickListener {
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Jeśli brak uprawnień, poproś o nie
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Jeśli uprawnienia są nadane, pobierz lokalizację
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    locationTextView.text = "Lat: $latitude, Lng: $longitude"
                    Log.d(TAG, "Location: Lat: $latitude, Lng: $longitude")
                } else {
                    locationTextView.text = getString(R.string.no_location)
                    Log.d(TAG, "Location not available")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation() // Jeśli uprawnienia zostały przyznane, pobierz lokalizację
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
