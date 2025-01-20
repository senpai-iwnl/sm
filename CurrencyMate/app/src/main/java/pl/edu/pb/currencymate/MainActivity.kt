package pl.edu.pb.currencymate

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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ustawienia przycisku przeliczania waluty
        binding.btnConvert.setOnClickListener {
            val baseCurrency = binding.spnBaseCurrency.selectedItem.toString()
            val targetCurrency = binding.spnTargetCurrency.selectedItem.toString()
            val amountText = binding.editAmount.text.toString()

            if (amountText.isBlank()) {
                Toast.makeText(this, "Proszę podać kwotę", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Proszę podać prawidłową kwotę", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchExchangeRate(baseCurrency, targetCurrency, amount)
        }
    }

    private fun fetchExchangeRate(base: String, target: String, amount: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.nbp.pl/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CurrencyApi::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Obsługa specjalnego przypadku dla PLN
                val baseRate = if (base == "PLN") 1.0 else null
                val targetRate = if (target == "PLN") 1.0 else null

                val response = api.getExchangeRates()
                val rates = response.first().rates

                val resolvedBaseRate = baseRate ?: rates.find { it.code == base }?.mid
                val resolvedTargetRate = targetRate ?: rates.find { it.code == target }?.mid

                if (resolvedBaseRate != null && resolvedTargetRate != null) {
                    // Oblicz kurs wymiany poprawnie
                    val conversionRate = resolvedBaseRate / resolvedTargetRate
                    val result = amount * conversionRate

                    // Wyświetl wynik
                    binding.txtResult.text = "%.2f $base = %.2f $target".format(amount, result)
                } else {
                    Toast.makeText(this@MainActivity, "Invalid currency selection", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
