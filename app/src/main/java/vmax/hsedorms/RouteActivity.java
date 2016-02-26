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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

public class RouteActivity extends AppCompatActivity implements View.OnClickListener {

    Interactor.Params params;
    LinearLayout cardsContainer;
    ArrayList<View> cards;
    Route route;
    LayoutInflater inflater;

    CardView onfoot$card, train$card;

    public void onClick (View v)
    {
        if (onfoot$card == v)
        {
            AlertDialog ad = new AlertDialog.Builder(RouteActivity.this)
                    .setTitle(R.string.map)
                    .create();



            View dialogView = inflater.inflate(R.layout.map_dialog, null);
            ImageView mapView = (ImageView)dialogView.findViewById(R.id.map);

            Picasso.with(RouteActivity.this)
                    .load(route.onfoot.mapsrc)
                    .into(mapView);

            ad.setView(dialogView);
            ad.setCanceledOnTouchOutside(true);
            ad.show();
        }
        else if (train$card == v)
        {
            final AlertDialog ad = new AlertDialog.Builder(RouteActivity.this)
                    .setTitle(route.train.title)
                    .setMessage(String.format(getString(R.string.train_dialog_message), route.train.stops))
                    .create();

            ad.setCanceledOnTouchOutside(true);
            ad.show();
        }
    }

    public void displayRoute(@NonNull final Route _route)
    {
        route = _route;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cards = new ArrayList<>();

        /* firstly we add view in a natural order (bus? => train? => subway => onfoot)
            then we check route.departure_place and reverse the list with views if needed (if departure_place  == 'edu')
          */

        if (route.bus != null)
        {
            CardView bus$card = (CardView)inflater.inflate(R.layout.card_bus, cardsContainer, false);
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
            train$card = (CardView)inflater.inflate(R.layout.card_train, cardsContainer, false);
            TextView train$to = (TextView)train$card.findViewById(R.id.train$to);

            TextView train$departure = (TextView)train$card.findViewById(R.id.train$departure);
            TextView train$arrival = (TextView)train$card.findViewById(R.id.train$arrival);

            train$to.setText(String.format(getString(R.string.train_to), route.train.to));

            train$departure.setText(route.train.departure.toString("HH:mm"));
            train$arrival.setText(route.train.arrival.toString("HH:mm"));

            train$card.setOnClickListener(this);

            cards.add(train$card);
        }

        if (route.subway != null)
        {
            CardView subway$card = (CardView)inflater.inflate(R.layout.card_subway, cardsContainer, false);
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
            onfoot$card = (CardView)inflater.inflate(R.layout.card_onfoot, cardsContainer, false);
            TextView onfoot$time = (TextView)onfoot$card.findViewById(R.id.onfoot$time);
            TextView onfoot$arrival = (TextView)onfoot$card.findViewById(R.id.onfoot$arrival);
            TextView onfoot$departure = (TextView)onfoot$card.findViewById(R.id.onfoot$departure);

            onfoot$time.setText(String.format(getString(R.string.onfoot_time), route.onfoot.time / 60));
            onfoot$departure.setText(route.onfoot.departure.toString("HH:mm"));
            onfoot$arrival.setText(route.onfoot.arrival.toString("HH:mm"));

            onfoot$card.setOnClickListener(this);

            cards.add(onfoot$card);
        }


        if (route.departure_place.compareTo("edu") == 0)
        {
            Collections.reverse(cards);
        }

        // Route title
        View title = inflater.inflate(R.layout.spinner_element, cardsContainer, false);
        ((TextView)title.findViewById(R.id.text))
                .setText(String.format(getString(R.string.route_title),
                        Places.findPlaceByApiName(route._from).humanReadableName,
                        Places.findPlaceByApiName(route._to).humanReadableName));
        cards.add(0, title);

        for (View v : cards)
        {
            cardsContainer.addView(v);
        }
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
