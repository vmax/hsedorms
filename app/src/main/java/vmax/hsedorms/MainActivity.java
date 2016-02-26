package vmax.hsedorms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.shape.Shape;
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
                    DialogInterface.OnClickListener,
                    Handler.Callback
{

    final int LOCATION_IS_SET = 1;
    final int LOCATION_IS_NOT_SET = 2;

    Handler geoMessageQueue;

    Location lastKnownLocation;

    Places.Place pDeparture;
    Places.Place pArrival;
    Interactor.When when;

    PlaceAdapter arrivalAdapter;
    ArrayList<Places.Place> arrivalList;

    PlaceAdapter dialogAdapter;

    WhenAdapter whenAdapter;
    ArrayList<Interactor.When> whenList;

    CoordinatorLayout rootCordinatorLayout;

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
    Button swapDestinations;

    GoogleApiClient googleApiClient;

    // handles message that location is set by geolocation
    @Override public boolean handleMessage(Message msg) {
        switch (msg.what)
        {
            case LOCATION_IS_SET:
                handleDeparture();
                handleArrival();
                break;
            case LOCATION_IS_NOT_SET:
                showDepartureChooserDialog(false);
                handleDeparture();
                handleArrival();
                break;
        }
        return true;
    }

    /**
     * Handles the clicks on three buttons in the MainActivity
     * @param v the button (fabGo || departureIsIncorrect || swapDestinations)
     */
    @Override public void onClick(View v) {
        if (fabGo == v)
        {
            if (!isOnline())
            {
                Snackbar.make(rootCordinatorLayout,R.string.no_internet,Snackbar.LENGTH_LONG)
                        .setAction(R.string.no_internet_settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                try {
                                    startActivity(intent);
                                } catch (Exception e)
                                {
                                    ;
                                }
                            }
                        }).show();
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
        else if (departureIsIncorrect == v || tDeparture == v)
        {
            showDepartureChooserDialog(true);
        }
        else if (swapDestinations == v)
        {
            Places.Place oldDeparture = pDeparture;
            pDeparture = pArrival;
            updateDepartureView();
            handleArrival(oldDeparture);
        }
    }


    @Override public void onClick(DialogInterface dialog, int which) {
        Places.Place oldDeparture = pDeparture;
        pDeparture = dialogAdapter.getItem(which);

        updateDepartureView();

        if (!Places.placesAreInSameGroup(oldDeparture, pDeparture)) {
            // we replace contents in arrival selector only if we changed the group of selection
            handleArrival();
        }

    }

    /**
     * User changed 'When' parameter in UI
     * @param group redunant; we have only one radio group
     * @param checkedId id of the option that has been selected
     */
    @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
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

                when = whenAdapter.getItem(0);
                timeSelector.performItemClick(timeSelector.getChildAt(0), 0, whenAdapter.getItemId(0));
                timeSelector.setSelection(0);

                break;
            case R.id.dateNow:
                timeSelectorTitle.setVisibility(View.GONE);
                timeSelector.setVisibility(View.GONE);

                when = new Interactor.When("now", "now");

                break;
            case R.id.dateTomorrow:
                timeSelectorTitle.setVisibility(View.VISIBLE);
                timeSelector.setVisibility(View.VISIBLE);

                resultWhenList = Interactor.getTimes(false);
                whenList.clear();
                whenList.addAll(resultWhenList);

                whenAdapter.notifyDataSetChanged();

                when = whenAdapter.getItem(0);
                timeSelector.performItemClick(timeSelector.getChildAt(0), 0, whenAdapter.getItemId(0));
                timeSelector.setSelection(0);

                break;
        }
    }

    /**
     * Check if performing internet requests is reasonable
     * @return if device is connected or connecting
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
        }
        else if (parent.getAdapter() == whenAdapter)
        {
            when = (Interactor.When)parent.getAdapter().getItem(position);
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


    /**
     * Couldn't connect to Google Location API
     * Probably, there are no Google Play Services on the device
     * @param connectionResult not used
     */
    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        geoMessageQueue.sendEmptyMessage(LOCATION_IS_NOT_SET);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PackageManager.PERMISSION_GRANTED == grantResults[0])
        {
            try {
                lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            } catch (SecurityException ex)
            {
                ; // shouldn't happen, though
            }
        }

        if (lastKnownLocation == null)
        {
            geoMessageQueue.sendEmptyMessage(LOCATION_IS_NOT_SET);
        }
        else {
            geoMessageQueue.sendEmptyMessage(LOCATION_IS_SET);
        }
    }

    @Override public void onConnected(Bundle bundle) {
        int isPermitted = ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED == isPermitted) {
            lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastKnownLocation == null)
            {
                geoMessageQueue.sendEmptyMessage(LOCATION_IS_NOT_SET);
            } else
            {
                geoMessageQueue.sendEmptyMessage(LOCATION_IS_SET);
            }
        } else
        {
            ActivityCompat.requestPermissions(this, new String[]{permission.ACCESS_FINE_LOCATION}, 0);
        }

    }

    public void showDepartureChooserDialog(boolean cancelable)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Где Вы?");
        builder.setAdapter(dialogAdapter, this);
        builder.setOnItemSelectedListener(this);
        builder.setCancelable(cancelable);
        builder.create().show();
    }

    public void handleDeparture()
    {
        if (lastKnownLocation != null)
        {
            // TODO: or get from SavedPreferences?
            pDeparture = Places.getNearestPlace(lastKnownLocation);
            updateDepartureView();
        }

        arrivalSelector.performItemClick(arrivalSelector.getChildAt(0), 0, arrivalAdapter.getItemId(0));
        arrivalSelector.setSelection(0);

    }

    public void handleArrival(Places.Place... placeToSet)
    {
        if (Arrays.asList(Places.Edus).contains(pDeparture))
        {
            arrivalList.clear();
            arrivalList.addAll(Arrays.asList(Places.Dorms));
            // departing from an edu, we don't support reverse routing, so hide unneeded buttons
            dateNow.setChecked(true);
            dateToday.setVisibility(View.GONE);
            dateTomorrow.setVisibility(View.GONE);
        }
        else
        {
            arrivalList.clear();
            arrivalList.addAll(Arrays.asList(Places.Edus));
            // departing from a dorm, we do support reverse routing, so show unneeded buttons
            dateToday.setVisibility(View.VISIBLE);
            dateTomorrow.setVisibility(View.VISIBLE);
        }

        arrivalAdapter.notifyDataSetChanged();
        if (placeToSet.length == 0) {
            arrivalSelector.performItemClick(arrivalSelector.getChildAt(0), 0, arrivalAdapter.getItemId(0));
            arrivalSelector.setSelection(0);
            pArrival = arrivalAdapter.getItem(0);
        }
        else
        {
            int placePosition = arrivalAdapter.getPosition(placeToSet[0]);
            arrivalSelector.performItemClick(arrivalSelector.getChildAt(placePosition), 0, arrivalAdapter.getItemId(placePosition));
            arrivalSelector.setSelection(placePosition);
            pArrival = placeToSet[0];
        }


    }

    @Override protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override protected void onDestroy() {
        googleApiClient.disconnect();
        super.onDestroy();
    }

    // TODO: implement
    @Override public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    // TODO: rtfm & implement
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
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

        rootCordinatorLayout = (CoordinatorLayout)findViewById(R.id.rootCoordinatorLayout);

        tDeparture = (TextView) findViewById(R.id.departure);
        tDeparture.setOnClickListener(this);
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

        swapDestinations = (Button) findViewById(R.id.swapDestinations);
        swapDestinations.setOnClickListener(this);

        ArrayList<Interactor.When> now = Interactor.getTimes(true);

        if (now.isEmpty())
        {
            dateToday.setVisibility(View.GONE);
        }


        DateTime tomorrow = DateTime.now().plusDays(1);
        if (tomorrow.getDayOfWeek() == 7) // handling weekend problem
        {
            tomorrow = tomorrow.plusDays(1);
        }
        dateTomorrow.setText(tomorrow.dayOfWeek().getAsText());


        whenList = new ArrayList<Interactor.When>();
        whenAdapter = new WhenAdapter(this, R.layout.spinner_element, whenList);
        whenAdapter.setDropDownViewResource(R.layout.spinner_element);
        timeSelector.setAdapter(whenAdapter);
        timeSelector.setOnItemSelectedListener(this);

        usageShowcase();
        geoMessageQueue = new Handler(this);



    }

    /**
     * Shows the onboarding usage showcase
     */
    protected void usageShowcase()
    {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "Sequence");
        sequence.setConfig(config);

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.spacer))
                        .setContentText(R.string.sc_main)
                        .setDismissOnTouch(true)
                        .build());

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(tDeparture)
                        .setContentText(R.string.sc_departure)
                        .setDismissOnTouch(true)
                        .build());
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(arrivalSelector)
                        .setContentText(R.string.sc_arrival)
                        .setDismissOnTouch(true)
                        .build());

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(dateSelector)
                        .setContentText(R.string.sc_date)
                        .setDismissOnTouch(true)
                        .build());

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(fabGo)
                        .setContentText(R.string.sc_go)
                        .setDismissOnTouch(true)
                        .build());
        sequence.start();
    }
}
