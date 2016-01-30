package vmax.hsedorms;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import vmax.hsedorms.api.Interactor;
import vmax.hsedorms.api.Places;
import vmax.hsedorms.api.Route;

public class RouteActivity extends AppCompatActivity {

    Interactor.Params params;

    public void displayRoute(@NonNull Route route)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Intent intent = getIntent();

        String _from = intent.getStringExtra("_from");
        String _to = intent.getStringExtra("_to");
        String when = intent.getStringExtra("when");
        String when_param = intent.getStringExtra("when_param");
        String device_id = intent.getStringExtra("device_id");

        params = new Interactor.Params();
        params._from = Places.findPlaceByApiName(_from);
        params._to = Places.findPlaceByApiName(_to);
        params.when = new Interactor.When(when, when_param);
        params.device_id = device_id;


        new Interactor(this).execute(params);


    }


}
