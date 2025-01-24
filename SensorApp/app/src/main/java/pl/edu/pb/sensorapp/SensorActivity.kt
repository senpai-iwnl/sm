package pl.edu.pb.sensorapp

import android.app.AlertDialog
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SensorActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorList: List<Sensor>
    private var adapter: SensorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        // Inicjalizacja RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.sensor_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pobranie czujników
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)

        // Inicjalizacja adaptera z obsługą długiego kliknięcia
        adapter = SensorAdapter(sensorList) { sensor ->
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                // Przekierowanie do LocationActivity dla czujnika magnetycznego
                val intent = Intent(this, LocationActivity::class.java)
                startActivity(intent)
            } else {
                // Wyświetlenie szczegółów innych czujników
                showSensorDetailsDialog(sensor)
            }
        }
        recyclerView.adapter = adapter

        // Dodanie przycisku do ręcznego otwierania LocationActivity
        val openLocationButton: Button = findViewById(R.id.open_location_button)
        openLocationButton.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }
    }

    // Wyświetlanie szczegółów czujnika w AlertDialog
    private fun showSensorDetailsDialog(sensor: Sensor) {
        val details = """
            Name: ${sensor.name}
            Type: ${sensor.type}
            Vendor: ${sensor.vendor}
            Version: ${sensor.version}
            Resolution: ${sensor.resolution}
            Max Range: ${sensor.maximumRange}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Sensor Details")
            .setMessage(details)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
