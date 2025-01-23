package pl.edu.pb.sensorapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorHolder> {

    private final List<Sensor> sensorList;

    public SensorAdapter(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }

    @NonNull
    @Override
    public SensorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sensor_list_item, parent, false);
        return new SensorHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorHolder holder, int position) {
        Sensor sensor = sensorList.get(position);
        holder.bind(sensor);

        holder.itemView.setOnClickListener(view -> {
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // Uruchomienie LocationActivity dla magnetometru
                Intent intent = new Intent(view.getContext(), LocationActivity.class);
                view.getContext().startActivity(intent);
            } else {
                // Dla innych czujników możemy dodać inną akcję
                Toast.makeText(view.getContext(), "Czujnik: " + sensor.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    static class SensorHolder extends RecyclerView.ViewHolder {
        private final TextView sensorNameTextView;

        public SensorHolder(@NonNull View itemView) {
            super(itemView);
            sensorNameTextView = itemView.findViewById(R.id.sensor_name);
        }

        public void bind(Sensor sensor) {
            sensorNameTextView.setText(sensor.getName());
        }
    }

}

