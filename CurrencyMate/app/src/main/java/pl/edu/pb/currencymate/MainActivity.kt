package pl.edu.pb.currencymate

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.edu.pb.currencymate.api.CurrencyApi
import pl.edu.pb.currencymate.databinding.ActivityMainBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

    // Mapa stolic walut (waluta -> współrzędne stolicy)
    private val capitalCoordinates = mapOf(
        "USD" to Pair(38.8951, -77.0364),  // Waszyngton, USA
        "EUR" to Pair(50.8503, 4.3517),   // Bruksela, Belgia
        "GBP" to Pair(51.5074, -0.1278), // Londyn, Wielka Brytania
        "JPY" to Pair(35.6895, 139.6917), // Tokio, Japonia
        "PLN" to Pair(52.2297, 21.0122),   // Warszawa, Polska
        "CHF" to Pair(46.9481, 7.4474)    // Berno, Szwajcaria
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Pobranie lokalizacji użytkownika
        fetchUserLocation()

        // Obsługa przycisku "Convert"
        binding.btnConvert.setOnClickListener {
            val baseCurrency = binding.spnBaseCurrency.selectedItem.toString()
            val targetCurrency = binding.spnTargetCurrency.selectedItem.toString()
            val amountText = binding.edtAmount.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toDouble()
                fetchExchangeRate(baseCurrency, targetCurrency, amount)

                // Obliczanie odległości
                val targetCapital = capitalCoordinates[targetCurrency]
                if (targetCapital != null && userLocation != null) {
                    val distance = calculateDistance(
                        userLocation!!.latitude,
                        userLocation!!.longitude,
                        targetCapital.first,
                        targetCapital.second
                    )
                    binding.txtDistance.text = "Distance to ${targetCurrency} capital: ${"%.2f".format(distance)} km"
                } else {
                    binding.txtDistance.text = "Unable to calculate distance"
                }

            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchExchangeRate(base: String, target: String, amount: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.nbp.pl/api/exchangerates/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CurrencyApi::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Konwersja waluty
                if (base == "PLN") {
                    val response = api.getCurrencyRate(target)
                    val rate = response.rates.first().mid
                    val conversion = amount / rate
                    binding.txtResult.text = "$amount $base = ${"%.2f".format(conversion)} $target"
                } else if (target == "PLN") {
                    val response = api.getCurrencyRate(base)
                    val rate = response.rates.first().mid
                    val conversion = amount * rate
                    binding.txtResult.text = "$amount $base = ${"%.2f".format(conversion)} $target"
                } else {
                    val baseResponse = api.getCurrencyRate(base)
                    val targetResponse = api.getCurrencyRate(target)
                    val baseRate = baseResponse.rates.first().mid
                    val targetRate = targetResponse.rates.first().mid
                    val conversion = amount * (baseRate / targetRate)
                    binding.txtResult.text = "$amount $base = ${"%.2f".format(conversion)} $target"
                }

                // Obliczanie dystansu
                val targetCapital = capitalCoordinates[target]
                if (targetCapital != null && userLocation != null) {
                    val distance = calculateDistance(
                        userLocation!!.latitude,
                        userLocation!!.longitude,
                        targetCapital.first,
                        targetCapital.second
                    )
                    binding.txtDistance.text = "Distance to ${target} capital: ${"%.2f".format(distance)} km"
                } else {
                    binding.txtDistance.text = "Unable to calculate distance"
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // Poproś o uprawnienia
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Pobierz ostatnią lokalizację
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = location
                Toast.makeText(this, "Location fetched successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun calculateDistance(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Promień Ziemi w km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Uprawnienia zostały przyznane
                fetchUserLocation()
            } else {
                // Uprawnienia zostały odrzucone
                Toast.makeText(this, "Location permission is required to calculate distance", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }


}

