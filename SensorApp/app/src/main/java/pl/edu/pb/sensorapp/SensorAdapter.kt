package pl.edu.pb.sensorapp

import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SensorAdapter(
    private val sensorList: List<Sensor>,
    private val onSensorClick: (Sensor) -> Unit
) : RecyclerView.Adapter<SensorAdapter.SensorHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sensor_list_item, parent, false)
        return SensorHolder(view)
    }

    override fun onBindViewHolder(holder: SensorHolder, position: Int) {
        val sensor = sensorList[position]
        holder.sensorName.text = sensor.name

        // Obsługa kliknięcia elementu listy
        holder.itemView.setOnClickListener {
            onSensorClick(sensor)
        }
    }

    override fun getItemCount(): Int = sensorList.size

    class SensorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sensorName: TextView = itemView.findViewById(R.id.sensor_name)
    }
}



