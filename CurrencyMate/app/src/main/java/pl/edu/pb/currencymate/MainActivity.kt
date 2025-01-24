package pl.edu.pb.currencymate

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.edu.pb.currencymate.api.CurrencyApi
import pl.edu.pb.currencymate.databinding.ActivityMainBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicjalizacja akcelerometru
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Obsługa przycisku "Convert"
        binding.btnConvert.setOnClickListener {
            val baseCurrency = binding.spnBaseCurrency.selectedItem.toString()
            val targetCurrency = binding.spnTargetCurrency.selectedItem.toString()
            val amountText = binding.edtAmount.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toDouble()
                fetchExchangeRate(baseCurrency, targetCurrency, amount)
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
                if (base == "PLN") {
                    // Pobierz kurs dla waluty docelowej względem PLN
                    val response = api.getCurrencyRate(target)
                    val rate = response.rates.first().mid
                    val conversion = amount / rate
                    binding.txtResult.text = "$amount $base = ${"%.2f".format(conversion)} $target"
                } else if (target == "PLN") {
                    // Pobierz kurs dla waluty bazowej względem PLN
                    val response = api.getCurrencyRate(base)
                    val rate = response.rates.first().mid
                    val conversion = amount * rate
                    binding.txtResult.text = "$amount $base = ${"%.2f".format(conversion)} $target"
                } else {
                    // Pobierz oba kursy i przelicz
                    val baseResponse = api.getCurrencyRate(base)
                    val targetResponse = api.getCurrencyRate(target)
                    val baseRate = baseResponse.rates.first().mid
                    val targetRate = targetResponse.rates.first().mid
                    val conversion = amount * (baseRate / targetRate)
                    binding.txtResult.text = "$amount $base = ${"%.2f".format(conversion)} $target"
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 1000) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())
                if (acceleration > 12) { // Próg potrząśnięcia
                    lastShakeTime = now
                    resetFields()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun resetFields() {
        binding.spnBaseCurrency.setSelection(0)
        binding.spnTargetCurrency.setSelection(0)
        binding.edtAmount.text.clear()
        binding.txtResult.text = ""
        Toast.makeText(this, "Fields reset!", Toast.LENGTH_SHORT).show()
    }
}