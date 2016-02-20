package vmax.hsedorms.api;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.JodaTimePermission;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vmax.hsedorms.R;
import vmax.hsedorms.RouteActivity;

public class Interactor extends AsyncTask<Interactor.Params, Void, Route> {

    RouteActivity context;
    ProgressBar downloadProgress;
    JSONObject resultJSON;

    private static final When lesson1;
    private static final When lesson2;
    private static final When lesson3;
    private static final When lesson4;
    private static final When lesson5;
    private static final When lesson6;
    private static final When lesson7;
    private static final When lesson8;

    static
    {
        lesson1 = new When("today", "9:00");
        lesson2 = new When("today", "10:30");
        lesson3 = new When("today", "12:10");
        lesson4 = new When("today", "13:40");
        lesson5 = new When("today", "15:10");
        lesson6 = new When("today", "16:40");
        lesson7 = new When("today", "18:10");
        lesson8 = new When("today", "19:40");
    }



    // FIXME: return only in reasonable time (+1:30)
    // FIXME: onlyAfterCurrentTime ->> tomorrow
    public static ArrayList<When> getTimes(boolean onlyAfterCurrentTime)
    {
        ArrayList<When> result = new ArrayList<When>();
        if (onlyAfterCurrentTime)
        {
            if (lesson1.joda_when_param.isAfterNow())
            {
                result.add(lesson1);
            }
            if (lesson2.joda_when_param.isAfterNow())
            {
                result.add(lesson2);
            }
            if (lesson3.joda_when_param.isAfterNow())
            {
                result.add(lesson3);
            }
            if (lesson4.joda_when_param.isAfterNow())
            {
                result.add(lesson4);
            }
            if (lesson5.joda_when_param.isAfterNow())
            {
                result.add(lesson5);
            }
            if (lesson6.joda_when_param.isAfterNow())
            {
                result.add(lesson6);
            }
            if (lesson7.joda_when_param.isAfterNow())
            {
                result.add(lesson7);
            }
            if (lesson8.joda_when_param.isAfterNow())
            {
                result.add(lesson8);
            }
        }
        else
        {
            result.add(lesson1);
            result.add(lesson2);
            result.add(lesson3);
            result.add(lesson4);
            result.add(lesson5);
            result.add(lesson6);
            result.add(lesson7);
            result.add(lesson8);

            for (When w : result)
            {
                w.when_param = "tomorrow";
            }
        }
        return result;
    }

    public static class When
    {
        public String when;
        public String when_param;
        public DateTime joda_when_param;

        @Override
        public String toString() {
            return when_param;
        }

        public When(String when, String when_param)
        {
            String times[] = when_param.split(":");
            this.when = when;
            this.when_param = when_param;


            if (times.length == 2)
            {
                this.joda_when_param = new DateTime().withTime(Integer.parseInt(times[0]), Integer.parseInt(times[1]), 0, 0);
                if (when.compareTo("tomorrow") == 0)
                {
                    this.joda_when_param = this.joda_when_param.plusDays(1);
                }
            }
            else
            {
                this.joda_when_param = new DateTime();
            }

        }

    }

    public static class Params{
        public Places.Place _from;
        public Places.Place _to;
        public When when;
        public String device_id;

        @Override
        public String toString() {
            String format = "_from=%s&_to=%s&when=%s&when_param=%s&device_id=%s";
            return String.format(format, _from.apiName, _to.apiName, when.when, when.when_param, device_id);
        }

    }
    public Interactor(Activity context)
    {
        this.context = (RouteActivity)context;
        this.downloadProgress = (ProgressBar) context.findViewById(R.id.downloadProgress);
    }

    @Override
    protected void onPreExecute() {
        downloadProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected Route doInBackground(Params... params) {
        String oneLine;
        StringBuilder stringBuilder;

        InputStream input;
        OutputStream output;

        BufferedWriter writer;
        BufferedReader reader;

        HttpURLConnection connection;
        URL url;

        Route response = null;
        try {

            url = new URL("http://dubki.pythonanywhere.com/route_mobile");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDefaultUseCaches(false); // don't cache the response
            connection.setDoInput(true);
            connection.setDoOutput(true);

            output = connection.getOutputStream();

            writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
            writer.write(params[0].toString());
            writer.close();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            stringBuilder = new StringBuilder();


            String line;

            if (200 == connection.getResponseCode()) {

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

            }

            resultJSON = new JSONObject(stringBuilder.toString());
            response = new Route(resultJSON);
            return response;
        }
        catch (MalformedURLException ex)
        {
            Log.d("hsedorms/Interactor", ":MalformedURLException " + ex.getMessage());
            // shouldn't happen as we have fixed known URL
        } catch (IOException ex)
        {
            Log.d("hsedorms/Interactor", ":IOException " + ex.getMessage());
            // may happen
        } catch (JSONException ex)
        {
            Log.d("hsedorms/Interactor", ":JSONException " + ex.getMessage());

            // should not happen but who knows
        }
        finally{
            return response;
        }

    }

    @Override
    protected void onPostExecute(Route route) {
        downloadProgress.setVisibility(View.GONE);
        if (route == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle("Проблема");
            builder.setPositiveButton("Связаться с разработчиком", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:vmax0770+dubki@gmail.com"));
                    try {
                        context.startActivity(Intent.createChooser(i, "Отправить:"));
                    } catch (ActivityNotFoundException ex)
                    {
                        ; // don't you fucking have an email client?!
                    }
                }
            });
            builder.setMessage("Большая проблема!");
            builder.create().show();

        }
        else
        {
            context.displayRoute(route);
        }
    }
}
