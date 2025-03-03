/**
 * REQUIRED STATEMENT: All javadoc-ing in this document was done by chatGPT.
 * PROMPT: javadoc this file
 */

package com.example.runtracker;

import android.Manifest;
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

/**
 * MainActivity handles location tracking for the run tracker application.
 * <p>
 * It requests location permissions, receives location updates, and displays the current coordinates
 * on the user interface.
 * </p>
 */
public class MainActivity extends AppCompatActivity {
    /** The LocationManager to access the system location services. */
    private LocationManager locationManager;

    /** Listener for receiving location updates. */
    private LocationListener locationListener;

    /** TextView for displaying the current latitude and longitude. */
    private TextView locationTextView;

    /** Request code used when asking for location permission. */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Initializes the activity.
     * <p>
     * Sets up the user interface, initializes location components, creates a LocationListener,
     * and checks for location permissions.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a LocationListener to receive location updates.
        locationListener = new LocationListener() {
            /**
             * Called when the location has changed.
             *
             * @param location The updated location.
             */
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Update the UI with the current location coordinates.
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locationTextView.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
            }

            /**
             * Called when the provider status changes.
             *
             * @param provider The name of the location provider.
             * @param status   The new status of the provider.
             * @param extras   Additional provider-specific information.
             */
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            /**
             * Called when the provider is enabled.
             *
             * @param provider The name of the location provider.
             */
            @Override
            public void onProviderEnabled(@NonNull String provider) { }

            /**
             * Called when the provider is disabled.
             *
             * @param provider The name of the location provider.
             */
            @Override
            public void onProviderDisabled(@NonNull String provider) { }
        };

        // Check if location permission is granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not already granted.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // If permission is granted, start receiving location updates.
            startLocationUpdates();
        }
    }

    /**
     * Callback method for the result from requesting permissions.
     * <p>
     * If the location permission is granted, the app begins to receive location updates.
     * Otherwise, it displays an appropriate message.
     * </p>
     *
     * @param requestCode  The integer request code originally supplied to requestPermissions().
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // If permission granted, start location updates.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                locationTextView.setText("Location permission denied.");
            }
        }
    }

    /**
     * Starts receiving location updates.
     * <p>
     * Requests location updates from the GPS provider with a minimum time interval of 5000 milliseconds
     * and a minimum distance change of 10 meters.
     * </p>
     */
    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the activity is about to go into the background.
     * <p>
     * Removes location updates to conserve battery life.
     * </p>
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
