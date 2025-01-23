package pl.edu.pb.sensorapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LocationActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private Location lastLocation;
    private TextView locationTextView;
    private TextView addressTextView;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        locationTextView = findViewById(R.id.location_text);
        addressTextView = findViewById(R.id.textview_address);
        Button fetchLocationButton = findViewById(R.id.fetch_location_button);
        Button getAddressButton = findViewById(R.id.fetch_address_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Pobierz lokalizację
        fetchLocationButton.setOnClickListener(view -> getLocation());

        // Pobierz adres
        getAddressButton.setOnClickListener(view -> executeGeocoding());
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            lastLocation = location;
                            locationTextView.setText(
                                    getString(
                                            R.string.location_text,
                                            location.getLatitude(),
                                            location.getLongitude(),
                                            java.text.DateFormat.getTimeInstance().format(location.getTime())
                                    )
                            );
                        } else {
                            locationTextView.setText("Nie udało się pobrać lokalizacji.");
                        }
                    });
        }
    }

    private String locationToGeocoding(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        String resultMessage = "";

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1 // Maksymalnie 1 wynik
            );
            if (addresses == null || addresses.isEmpty()) {
                resultMessage = context.getString(R.string.no_address_found);
                Log.e("LocationActivity", resultMessage);
            } else {
                Address address = addresses.get(0);
                List<String> addressParts = new ArrayList<>();

                // Zbieranie wszystkich linii adresu
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressParts.add(address.getAddressLine(i));
                }

                // Łączenie wyników w jedną wiadomość
                resultMessage = TextUtils.join("\n", addressParts);
            }
        } catch (IOException e) {
            resultMessage = context.getString(R.string.service_not_available);
            Log.e("LocationActivity", resultMessage, e);
        }

        return resultMessage;
    }

    private void executeGeocoding() {
        if (lastLocation != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> returnedAddress = executor.submit(() -> locationToGeocoding(getApplicationContext(), lastLocation));

            try {
                // Pobranie wyniku z wątku
                String result = returnedAddress.get();
                addressTextView.setText(getString(R.string.address_text, result, java.text.DateFormat.getTimeInstance().format(lastLocation.getTime())));
            } catch (ExecutionException | InterruptedException e) {
                Log.e("LocationActivity", "Geocoding failed", e);
                Thread.currentThread().interrupt();
            }
        } else {
            addressTextView.setText("Brak dostępnej lokalizacji. Pobierz lokalizację najpierw.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                locationTextView.setText("Brak uprawnień do lokalizacji.");
            }
        }
    }
}


