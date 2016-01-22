package vmax.hsedorms;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest.permission;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener{


    Location lastKnownLocation;

    GoogleApiClient googleApiClient;

    @Override
    public void onConnectionSuspended(int i) {

    }

    // Couldn't connect to Google Location API
    // Probably, there are no Google Play Services on the device
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        int isPermitted = ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED == isPermitted) {
            lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } else
        {
            // we are not permitted to get location
            // TODO: ask for it or ask the current location
        }
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        googleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (googleApiClient == null)
        {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }
}
