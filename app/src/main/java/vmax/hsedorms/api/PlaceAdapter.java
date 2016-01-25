package vmax.hsedorms.api;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import vmax.hsedorms.R;

public class PlaceAdapter extends ArrayAdapter<Places.Place>
{
    public PlaceAdapter(Context ctx, int resId, List<Places.Place> places)
    {
        super(ctx, resId, places);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Places.Place place = getItem(position);

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_element, parent, false);
        }

        TextView placeName = (TextView) convertView.findViewById(R.id.text);
        placeName.setText(place.toString());

        return convertView;
    }
}
