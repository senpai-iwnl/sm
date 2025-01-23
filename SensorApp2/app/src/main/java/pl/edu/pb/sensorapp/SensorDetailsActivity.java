package pl.edu.pb.sensorapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SensorDetailsActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorLight;
    private TextView sensorLightTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_details_activity);

        sensorLightTextView = findViewById(R.id.sensor_light_label);

        // Inicjalizacja SensorManager i pobranie sensora
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (sensorLight == null) {
            sensorLightTextView.setText(getString(R.string.missing_sensor));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Rejestracja nasłuchiwania sensora w onStart()
        if (sensorLight != null) {
            sensorManager.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Wyrejestrowanie nasłuchiwania sensora w onStop()
        if (sensorLight != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];

        // Wyświetlanie wartości czujnika na podstawie jego typu
        switch (sensorType) {
            case Sensor.TYPE_LIGHT:
                sensorLightTextView.setText(getResources().getString(
                        R.string.light_sensor_label, currentValue));
                break;

            default:
                sensorLightTextView.setText(getString(R.string.unknown_sensor));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Opcjonalna obsługa zmian dokładności sensora
    }
}


