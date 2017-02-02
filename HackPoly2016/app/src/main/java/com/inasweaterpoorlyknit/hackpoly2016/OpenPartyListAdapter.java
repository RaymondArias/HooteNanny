package com.inasweaterpoorlyknit.hackpoly2016;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by raymond on 6/15/16.
 */

public class OpenPartyListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> partyNames;
    private final ArrayList<String> hostNames;

    public OpenPartyListAdapter(Activity context, ArrayList<String> partyNames, ArrayList<String> hostNames)
    {
        super(context, R.layout.open_party_row, partyNames);
        this.context = context;
        this.partyNames = partyNames;
        this.hostNames = hostNames;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.open_party_row, null, true);
        if (partyNames.size() > position) {
            TextView partyTitle = (TextView) rowView.findViewById(R.id.party_name_row);
            partyTitle.setText(partyNames.get(position));
            TextView hostTitle = (TextView) rowView.findViewById(R.id.host_name_row);
            hostTitle.setText(hostNames.get(position));

        }
        return rowView; // return the rowView that was created
    }
}
