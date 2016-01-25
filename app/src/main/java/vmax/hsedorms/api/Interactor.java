package vmax.hsedorms.api;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vmax.hsedorms.R;

public class Interactor extends AsyncTask<Interactor.Params, Void, Route> {

    Activity context;
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

    public class Params {
        String _from;
        String _to;
        When when;
        String device_id;

        @Override
        public String toString() {
            String format = "_from=%s&_to=%s&when=%s&when_param=%s&device_id=%s";
            return String.format(format, _from, _to, when.when, when.when_param, device_id);
        }
    }
    public Interactor(Activity context)
    {
        this.context = context;
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
        try
        {
            url = new URL("http://dubki.pythonanywhere.com/route_mobile");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDefaultUseCaches(false); // don't cache the response
            connection.setDoInput(true);
            connection.setDoOutput(true);
            input = connection.getInputStream();
            output = connection.getOutputStream();

            writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
            writer.write(params[0].toString());
            writer.close();

            reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            stringBuilder = new StringBuilder();

            do {
                oneLine = reader.readLine();
                if (oneLine != null)
                {
                    stringBuilder.append(oneLine);
                }
            } while (oneLine != null);

            resultJSON = new JSONObject(stringBuilder.toString());
            response = Route.fromJSON(resultJSON);
        } catch (MalformedURLException ex)
        {
            // shouldn't happen as we have fixed known URL
        } catch (IOException ex)
        {
            // may happen
        } catch (JSONException ex)
        {
            // should not happen but who knows
        }
        return response;
    }

    @Override
    protected void onPostExecute(Route route) {
        downloadProgress.setVisibility(View.GONE);
        if (route == null)
        {
            // TODO: display that we've got errors
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

        }
    }
}
