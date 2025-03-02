package com.example.runtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView locationTextView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    // Store the previous location to calculate speed manually.
    private Location lastLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a LocationListener to get location updates
        locationListener = new LocationListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Current coordinates
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double speedMph = 0.0;  // default speed

                if (lastLocation != null) {
                    // Calculate the time difference in seconds
                    double timeDelta = (location.getTime() - lastLocation.getTime()) / 1000.0;
                    if (timeDelta > 0) {
                        // Calculate distance between the two points using the Haversine formula
                        double distanceMeters = haversine(
                                lastLocation.getLatitude(),
                                lastLocation.getLongitude(),
                                latitude,
                                longitude
                        );
//                        System.out.println(lastLocation.getLatitude());
//                        System.out.println(lastLocation.getLongitude());
//                        System.out.println(latitude);
//                        System.out.println(longitude);
//                        System.out.println("Distance: " + distanceMeters);
                        // Speed in meters per second = distance / time
                        double speedMps = distanceMeters / timeDelta;
                        // Convert speed to miles per hour (1 m/s ≈ 2.23694 mph)
                        speedMph = speedMps * 2.23694;
                    }
                }

                // Update lastLocation for the next update
                lastLocation = location;

                // Update the TextView with latitude, longitude, and calculated speed
                locationTextView.setText(
                        "Lat: " + String.format("%.4f", latitude) +
                        "\nLng: " + String.format("%.4f", longitude) +
                        "\nSpeed: " + String.format("%.2f", speedMph) + " mph");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        // Check if the location permission is granted
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
    }

    // Callback for the result from requesting permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // If permission is granted, start location updates
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
            // Request location updates every 5000 milliseconds or every 10 meters change
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
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

    /**
     * Calculates the distance between two GPS coordinates using the Haversine formula.
     *
     * @param lat1 Latitude of the first point in degrees.
     * @param lon1 Longitude of the first point in degrees.
     * @param lat2 Latitude of the second point in degrees.
     * @param lon2 Longitude of the second point in degrees.
     * @return Distance between the two points in meters.
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth’s radius in meters
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
