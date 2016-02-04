package vmax.hsedorms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import vmax.hsedorms.api.Interactor;
import vmax.hsedorms.api.Places;
import vmax.hsedorms.api.Route;

public class RouteActivity extends AppCompatActivity {

    Interactor.Params params;
    LinearLayout cardsContainer;

    public void displayRoute(final @NonNull Route route)
    {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        ArrayList<View> cards = new ArrayList<>();

        /* firstly we add view in a natural order (bus? => train? => subway => onfoot)
            then we check route.departure_place and reverse the list with views if needed (if departure_place  == 'edu')
          */



        if (route.bus != null)
        {
            CardView bus$card = (CardView)inflater.inflate(R.layout.card_bus, null);
            TextView bus$_from = (TextView)bus$card.findViewById(R.id.bus$_from);
            TextView bus$_to = (TextView)bus$card.findViewById(R.id.bus$_to);
            TextView bus$arrival = (TextView)bus$card.findViewById(R.id.bus$arrival);
            TextView bus$departure = (TextView)bus$card.findViewById(R.id.bus$departure);

            bus$_from.setText(route.bus.from);
            bus$_to.setText(route.bus.to);
            bus$departure.setText(route.bus.departure.toString("HH:mm"));
            bus$arrival.setText(route.bus.arrival.toString("HH:mm"));

            cards.add(bus$card);
        }

        if (route.train != null)
        {
            CardView train$card = (CardView)inflater.inflate(R.layout.card_train, null);
            TextView train$title = (TextView)train$card.findViewById(R.id.train$title);
            TextView train$to = (TextView)train$card.findViewById(R.id.train$to);
            TextView train$stops = (TextView)train$card.findViewById(R.id.train$stops);
            TextView train$departure = (TextView)train$card.findViewById(R.id.train$departure);
            TextView train$arrival = (TextView)train$card.findViewById(R.id.train$arrival);

            train$title.setText(route.train.title);
            train$to.setText("выходите на станции: " + route.train.to);
            train$stops.setText("остановки: " + route.train.stops);
            train$departure.setText(route.train.departure.toString("HH:mm"));
            train$arrival.setText(route.train.arrival.toString("HH:mm"));

            cards.add(train$card);
        }

        if (route.subway != null)
        {
            CardView subway$card = (CardView)inflater.inflate(R.layout.card_subway, null);
            TextView subway$_from = (TextView)subway$card.findViewById(R.id.subway$_from);
            TextView subway$_to = (TextView)subway$card.findViewById(R.id.subway$_to);
            TextView subway$arrival = (TextView)subway$card.findViewById(R.id.subway$arrival);
            TextView subway$departure = (TextView)subway$card.findViewById(R.id.subway$departure);

            subway$_from.setText(route.subway.from);
            subway$_to.setText(route.subway.to);
            subway$departure.setText(route.subway.departure.toString("HH:mm"));
            subway$arrival.setText(route.subway.arrival.toString("HH:mm"));

            cards.add(subway$card);
        }

        if(route.onfoot != null)
        {
            CardView onfoot$card = (CardView)inflater.inflate(R.layout.card_onfoot, null);
            TextView onfoot$time = (TextView)onfoot$card.findViewById(R.id.onfoot$time);
            TextView onfoot$arrival = (TextView)onfoot$card.findViewById(R.id.onfoot$arrival);
            TextView onfoot$departure = (TextView)onfoot$card.findViewById(R.id.onfoot$departure);

            onfoot$time.setText("Пешком: " + String.valueOf(route.onfoot.time / 60) + " минут");
            onfoot$departure.setText(route.onfoot.departure.toString("HH:mm"));
            onfoot$arrival.setText(route.onfoot.arrival.toString("HH:mm"));

            onfoot$card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog ad = new AlertDialog.Builder(RouteActivity.this)
                            .setTitle("Карта")
                            .create();

                    View dialogView = inflater.inflate(R.layout.map_dialog, null);
                    ImageView mapView = (ImageView)dialogView.findViewById(R.id.map);

                    Picasso.with(RouteActivity.this)
                            .load(route.onfoot.mapsrc)
                            .into(mapView);

                    ad.setView(dialogView);
                    ad.show();

                }
            });

            cards.add(onfoot$card);
        }


        if (route.departure_place.compareTo("edu") == 0)
        {
            Collections.reverse(cards);
        }

        for (View v : cards)
        {
            cardsContainer.addView(v);
        }



        /*
        TextView tv = (TextView) findViewById(R.id.smth);
        tv.setText(route.toString());*/
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        cardsContainer = (LinearLayout)findViewById(R.id.cardsContainer);

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
