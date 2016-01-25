package vmax.hsedorms.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Route {

    public Date departure;
    public Date arrival;
    public long fullRouteTime; // in seconds

    public Bus bus;
    public Train train;
    public Subway subway;


    public class Subway
    {
        public Date arrival;
        public Date departure;
        public String from;
        public String to;
    }

    public class Bus {
        public Date arrival;
        public Date departure;
        public String from;
        public String to;
    }

    public class Train {
        public Date arrival;
        public Date departure;
        public String stops;
        public String title;
        public String to; // station to exit on
    }

    public static Route fromJSON (JSONObject jsonObject)
    {
        try {
            JSONObject bus = jsonObject.getJSONObject("bus");
        } catch (JSONException ex)
        {
            //
        }
        return null;
    }
}
