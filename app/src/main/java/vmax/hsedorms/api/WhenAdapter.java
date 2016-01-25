package vmax.hsedorms.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vmax.hsedorms.R;


public class WhenAdapter extends ArrayAdapter<Interactor.When>
{
    public WhenAdapter(Context ctx, int resId, List<Interactor.When> places)
    {
        super(ctx, resId, places);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Interactor.When when = getItem(position);

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_element, parent, false);
        }

        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText(when.when_param);

        return convertView;
    }
}