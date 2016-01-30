package vmax.hsedorms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest.permission;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;
import vmax.hsedorms.api.Interactor;
import vmax.hsedorms.api.PlaceAdapter;
import vmax.hsedorms.api.Places;
import vmax.hsedorms.api.Route;
import vmax.hsedorms.api.WhenAdapter;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    SegmentedGroup.OnCheckedChangeListener,
                    View.OnClickListener,
                    AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener
{

    Location lastKnownLocation;

    Places.Place pDeparture;
    Places.Place pArrival;
    Interactor.When when;

    PlaceAdapter arrivalAdapter;
    ArrayList<Places.Place> arrivalList;

    PlaceAdapter dialogAdapter;

    WhenAdapter whenAdapter;
    ArrayList<Interactor.When> whenList;

    TextView tDeparture;
    SegmentedGroup dateSelector;

    RadioButton dateToday;
    RadioButton dateNow;
    RadioButton dateTomorrow;

    TextView timeSelectorTitle;
    Spinner timeSelector;

    Spinner arrivalSelector;

    FloatingActionButton fabGo;
    Button departureIsIncorrect;

    GoogleApiClient googleApiClient;


    /**
     * Handles the clicks on two buttons in the MainActivity
     * @param v the button (fabGo || departureIsIncorrect)
     */
    @Override public void onClick(View v) {
        if (fabGo == v)
        {
            Log.d("hsedorms", "Params:");
            Log.d("hsedorms", "pDeparture: " + pDeparture.apiName);
            Log.d("hsedorms", "pArrival: " + pArrival.apiName);
            // TODO: start RouteActivity
            if (!isOnline())
            {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Проблема")
                        .setMessage("Судя по всему, интернет-соединение недоступно")
                        .setCancelable(true)
                        .create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
            else
            {

                // start another activity
                Intent routeIntent = new Intent(this, RouteActivity.class);
                routeIntent.putExtra("_from",pDeparture.apiName);
                routeIntent.putExtra("_to", pArrival.apiName);
                routeIntent.putExtra("when", when.when);
                routeIntent.putExtra("when_param", when.when_param);
                routeIntent.putExtra("device_id", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                startActivity(routeIntent);
            }

        }
        else if (departureIsIncorrect == v)
        {
            showDepartureChooserDialog();
        }
    }


    @Override public void onClick(DialogInterface dialog, int which) {
        Log.d("hsedorms", "onClick on Dialog");
        Places.Place oldDeparture = pDeparture;
        pDeparture = dialogAdapter.getItem(which);

        updateDepartureView();

        if (!Places.placesAreInSameGroup(oldDeparture, pDeparture)) {
            // we replace contents in arrival selector only if we changed the group of selection
            handleArrival();
        }

    }

    @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO: update When in MainActivity
        ArrayList<Interactor.When> resultWhenList;
        switch (checkedId)
        {
            case R.id.dateToday:
                timeSelectorTitle.setVisibility(View.VISIBLE);
                timeSelector.setVisibility(View.VISIBLE);

                resultWhenList = Interactor.getTimes(true);
                whenList.clear();
                whenList.addAll(resultWhenList);

                whenAdapter.notifyDataSetChanged();

                break;
            case R.id.dateNow:
                timeSelectorTitle.setVisibility(View.GONE);
                timeSelector.setVisibility(View.GONE);
                break;
            case R.id.dateTomorrow:
                timeSelectorTitle.setVisibility(View.VISIBLE);
                timeSelector.setVisibility(View.VISIBLE);

                resultWhenList = Interactor.getTimes(false);
                whenList.clear();
                whenList.addAll(resultWhenList);

                whenAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * Check if performing internet requests is reasonable
     * @return if device is connecting or connecting
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter() == arrivalAdapter)
        {
            pArrival = (Places.Place)parent.getAdapter().getItem(position);
            Toast.makeText(this, pArrival.toString(), Toast.LENGTH_LONG).show();
        }
        else if (parent.getAdapter() == whenAdapter)
        {
            when = (Interactor.When)parent.getAdapter().getItem(position);
            Toast.makeText(this, when.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onNothingSelected(AdapterView<?> parent) {
        ; // TODO: implement?
    }

    @Override public void onConnectionSuspended(int i) {

    }

    /**
     * Reacts to changes in pDeparture
     */
    public void updateDepartureView () {
        if (pDeparture != null) {
            tDeparture.setText(pDeparture.toString());
        }
    }


    // Couldn't connect to Google Location API
    // Probably, there are no Google Play Services on the device
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        handleDeparture();
        handleArrival();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PackageManager.PERMISSION_GRANTED == grantResults[0])
        {
            try {
                lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            } catch (SecurityException ex)
            {
                ; // shouldn't happen, though
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        int isPermitted = ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED == isPermitted) {
            lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } else
        {
            ActivityCompat.requestPermissions(this, new String[]{permission.ACCESS_FINE_LOCATION}, 0);
        }

        handleDeparture();
        handleArrival();

    }

    public void showDepartureChooserDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Где Вы?");
        builder.setAdapter(dialogAdapter, this);
        builder.setOnItemSelectedListener(this);
        builder.setCancelable(false);
        builder.create().show();
    }

    public void handleDeparture()
    {
        // do we have location?
        if (lastKnownLocation == null)
        {
            // no
            Log.d("hsedorms", "no last location");
            showDepartureChooserDialog();

        }
        else
        {
            // TODO: or get from SavedPreferences?
            pDeparture = Places.getNearestPlace(lastKnownLocation);
            updateDepartureView();
        }

       // Toast.makeText(this, pDeparture.toString(), Toast.LENGTH_LONG);
        arrivalSelector.performItemClick(arrivalSelector.getChildAt(0), 0, arrivalAdapter.getItemId(0));

    }

    public void handleArrival()
    {
        if (Arrays.asList(Places.Edus).contains(pDeparture))
        {
            arrivalList.clear();
            arrivalList.addAll(Arrays.asList(Places.Dorms));

        }
        else
        {
            arrivalList.clear();
            arrivalList.addAll(Arrays.asList(Places.Edus));
        }

        arrivalAdapter.notifyDataSetChanged();
        arrivalSelector.performItemClick(arrivalSelector.getChildAt(0), 0, arrivalAdapter.getItemId(0));
        arrivalSelector.setSelection(0);
        pArrival = arrivalAdapter.getItem(0);
        Log.d("hsedorms", "new arrival is " + pArrival.apiName);
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

    // TODO: implement
    @Override public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    // TODO: rtfm & implement
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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

        when = new Interactor.When("now", "now");

        tDeparture = (TextView) findViewById(R.id.departure);
        dateSelector = (SegmentedGroup)findViewById(R.id.dateSelector);
        dateSelector.setOnCheckedChangeListener(this);

        dateToday = (RadioButton) findViewById(R.id.dateToday);
        dateNow = (RadioButton) findViewById(R.id.dateNow);
        dateTomorrow = (RadioButton) findViewById(R.id.dateTomorrow);

        timeSelectorTitle = (TextView) findViewById(R.id.timeSelectorTitle);
        timeSelector = (Spinner) findViewById(R.id.timeSelector);

        fabGo = (FloatingActionButton)findViewById(R.id.fabGo);
        fabGo.setOnClickListener(this);

        departureIsIncorrect = (Button) findViewById(R.id.departureIsIncorrect);
        departureIsIncorrect.setOnClickListener(this);

        arrivalSelector = (Spinner) findViewById(R.id.arrivalSelector);
        arrivalList = new ArrayList<Places.Place>();

        arrivalAdapter = new PlaceAdapter(this, R.layout.spinner_element, arrivalList);
        arrivalAdapter.setDropDownViewResource(R.layout.spinner_element);

        arrivalSelector.setAdapter(arrivalAdapter);
        arrivalSelector.setOnItemSelectedListener(this);

        dialogAdapter = new PlaceAdapter(this, R.layout.spinner_element, Arrays.asList(Places.AllPlaces));
        dialogAdapter.setDropDownViewResource(R.layout.spinner_element);

        // check if 'today' option should be displayed
        ArrayList<Interactor.When> now = Interactor.getTimes(true);

        if (now.isEmpty())
        {
            dateToday.setVisibility(View.GONE);
        }


        whenList = new ArrayList<Interactor.When>();
        whenAdapter = new WhenAdapter(this, R.layout.spinner_element, whenList);
        whenAdapter.setDropDownViewResource(R.layout.spinner_element);
        timeSelector.setAdapter(whenAdapter);
        timeSelector.setOnItemSelectedListener(this);

    }
}
