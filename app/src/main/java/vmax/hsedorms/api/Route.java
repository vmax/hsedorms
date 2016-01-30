package vmax.hsedorms.api;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class Route {

    // TODO: don't forget to check the taxi

    public DateTime departure;
    public DateTime arrival;
    public long fullRouteTime; // in seconds
    public String departure_place;

    public Bus bus;
    public Train train;
    public Subway subway;
    public Onfoot onfoot;

    public String _from;
    public String _to;

    public class Subway
    {
        public DateTime arrival;
        public DateTime departure;
        public String from;
        public String to;

        public Subway(JSONObject subwayObject)
        {
            try
            {
                this.from = subwayObject.getString("from");
                this.to = subwayObject.getString("to");
                this.arrival = pyDateTimeToJoda(subwayObject.getJSONObject("arrival"));
                this.departure = pyDateTimeToJoda(subwayObject.getJSONObject("departure"));
            } catch (JSONException ex)
            {

            }
        }
    }

    public class Bus {
        public DateTime arrival;
        public DateTime departure;
        public String from;
        public String to;

        public Bus(JSONObject busObject)
        {
            try
            {
                this.from = busObject.getString("from");
                this.to = busObject.getString("to");
                this.arrival = pyDateTimeToJoda(busObject.getJSONObject("arrival"));
                this.departure = pyDateTimeToJoda(busObject.getJSONObject("departure"));
            } catch (JSONException ex)
            {
                Log.d("hsedorms", ":JSONException/Bus " + ex.getMessage());
            }
        }
    }
    public class Train {
        public DateTime arrival;
        public DateTime departure;
        public String stops;
        public String title;
        public String to; // station to exit on

        public Train(JSONObject trainObject)
        {
            try{
                this.arrival = pyDateTimeToJoda(trainObject.getJSONObject("arrival"));
                this.departure = pyDateTimeToJoda(trainObject.getJSONObject("departure"));
                this.stops = trainObject.getString("stops");
                this.title = trainObject.getString("title");
                this.to = trainObject.getString("to");
            } catch (JSONException ex)
            {

            }
        }
    }
    public class Onfoot {
        public DateTime arrival;
        public DateTime departure;
        public String mapsrc;
        public int time; // in seconds

        public Onfoot(JSONObject onfootObject)
        {
            try
            {
                this.arrival = pyDateTimeToJoda(onfootObject.getJSONObject("arrival"));
                this.departure = pyDateTimeToJoda(onfootObject.getJSONObject("departure"));
                this.mapsrc = onfootObject.getString("mapsrc");
                this.time = onfootObject.getJSONObject("time").getInt("seconds");
            } catch (JSONException ex)
            {

            }
        }
    }

    public static DateTime pyDateTimeToJoda (JSONObject pyDateTime) {
        int day, month, year, minute, hour;
        try {
            day = pyDateTime.getInt("day");
            month = pyDateTime.getInt("month");
            year = pyDateTime.getInt("year");

            hour = pyDateTime.getInt("hour");
            minute = pyDateTime.getInt("minute");

            return new DateTime().withDate(year, month, day).withTime(hour, minute, 0, 0);

        } catch (JSONException ex)
        {
            ;
        }
        return null;
    }

    public Route (JSONObject jsonObject)
    {
        try {
            this._from = jsonObject.getString("_from");
            this._to = jsonObject.getString("_to");

            this.arrival = pyDateTimeToJoda(jsonObject.getJSONObject("arrival"));
            this.departure = pyDateTimeToJoda(jsonObject.getJSONObject("departure"));

            this.departure_place = jsonObject.getString("departure_place");
            this.fullRouteTime = jsonObject.getJSONObject("full_route_time").getInt("seconds");

            this.bus = new Bus(jsonObject.getJSONObject("bus"));
            this.train = new Train(jsonObject.optJSONObject("train"));
            this.subway = new Subway(jsonObject.getJSONObject("subway"));
            this.onfoot = new Onfoot(jsonObject.getJSONObject("onfoot"));

        } catch (JSONException ex)
        {
            Log.d("hsedorms", ":JSONException/Route " + ex.getMessage());
        }
    }

}
