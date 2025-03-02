package com.example.runtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView locationTextView;
    private TextView speedTextView;
    private Button pauseButton;
    private boolean isPaused = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the TextViews and Button from the layout
        locationTextView = findViewById(R.id.locationTextView);
        speedTextView = findViewById(R.id.speedTextView);
        pauseButton = findViewById(R.id.pauseButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a LocationListener to get location updates
        locationListener = new LocationListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Get current coordinates
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double speedMph = location.getSpeed() * 2.23694;

                // Update the location TextView with latitude and longitude
                locationTextView.setText(
                        "Lat: " + String.format("%.8f", latitude) + "°" +
                        "\nLng: " + String.format("%.8f", longitude) + "°");

                // Update the speed TextView with speed
                speedTextView.setText("Speed: " + String.format("%.3f", speedMph) + " mph");

                // Change the color of the speed TextView based on the speed value
                if (speedMph < 33) {
                    speedTextView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_dark));
                } else if (speedMph < 66) {
                    speedTextView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_orange_dark));
                } else {
                    speedTextView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(@NonNull String provider) { }

            @Override
            public void onProviderDisabled(@NonNull String provider) { }
        };

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, start receiving location updates
            startLocationUpdates();
        }

        // Set up the pause button to toggle location updates
        pauseButton.setOnClickListener(v -> {
            if (!isPaused) {
                // Pause location updates
                locationManager.removeUpdates(locationListener);
                pauseButton.setText("Resume");
                isPaused = true;
            } else {
                // Resume location updates
                startLocationUpdates();
                pauseButton.setText("Pause");
                isPaused = false;
            }
        });
    }

    // Callback for the result from requesting permissions
    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                locationTextView.setText("Location permission denied.");
            }
        }
    }

    // Method to start location updates
    private void startLocationUpdates() {
        try {
            // Request location updates every 1 second or every 1 meter change
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Remove location updates when the activity is paused to save battery
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
