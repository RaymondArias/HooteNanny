package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class PartyListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button searchPartiesBtn;
    private AWSServerCommand serverCommand;
    private ArrayList<String> partyNames;
    private ArrayList<String> hostNames;
    private OpenPartyListAdapter listAdapter;
    private ArrayList<Party> matchedParties;
    private AutoCompleteTextView textInput;
    private ListView matchedPartyList;
    private HashMap<Marker, Party> markerPartyMap;
    private GoogleMap mMap;
    private GPSHelper gpsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        partyNames = new ArrayList<>();
        hostNames = new ArrayList<>();
        matchedParties = new ArrayList<>();
        textInput = (AutoCompleteTextView)findViewById(R.id.party_name_search);
        serverCommand = new AWSServerCommand();
        matchedPartyList = (ListView)findViewById(R.id.matched_party_list);
        listAdapter = new OpenPartyListAdapter(this, partyNames, hostNames);
        matchedPartyList.setAdapter(listAdapter);
        gpsHelper = new GPSHelper(this, this);
        searchPartiesBtn = (Button)findViewById(R.id.search_party_btn);
        searchPartiesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get string from text input
                partyNames.clear();
                hostNames.clear();
                matchedParties.clear();
                Runnable searchPartiesProcess = new Runnable() {
                    @Override
                    public void run() {
                        String inputStr = textInput.getText().toString();
                        serverCommand.getOpenParties(inputStr, partyNames, hostNames, matchedParties);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAdapter.notifyDataSetChanged();
                            }
                        });


                    }
                };
                Thread searchPartiesThread = new Thread(searchPartiesProcess);
                searchPartiesThread.start();



            }
        });

        matchedPartyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Party myParty = matchedParties.get(position);
                Intent intent = new Intent(view.getContext(), ClientMainActivity.class);
                intent.putExtra("myParty", myParty);
                startActivity(intent);

            }
        });

        //gpsHelper = new GPSHelper(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.party_list_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng currentLocation = gpsHelper.getLatLng();
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        // the higher the value of the float, the closer-in the zoom will be
        float zoomFloat = 20.0f;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(zoomFloat).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
        Runnable searchPartiesProcess = new Runnable() {
            @Override
            public void run() {
                String inputStr = textInput.getText().toString();
                serverCommand.getOpenParties(inputStr, partyNames, hostNames, matchedParties);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        markerPartyMap = new HashMap<Marker, Party>();
                        for(int i = 0; i < matchedParties.size(); i++)
                        {
                            Party aParty = matchedParties.get(i);
                            LatLng location = new LatLng(aParty.getLatitude(), aParty.getLongitude());
                            Marker marker =
                            mMap.addMarker(new MarkerOptions().position(location).title(aParty.getPartyName()));
                            markerPartyMap.put(marker, aParty);
                        }
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                marker.showInfoWindow();
                                if(markerPartyMap != null)
                                {
                                    Party selectedParty = markerPartyMap.get(marker);
                                    if(selectedParty != null) {
                                        Log.d("Party Name", selectedParty.getPartyName());
                                        Log.d("Party id", selectedParty.getPartyID() + "");
                                    }
                                }
                                return false;
                            }
                        });
                        listAdapter.notifyDataSetChanged();
                    }
                });


            }
        };
        Thread searchPartiesThread = new Thread(searchPartiesProcess);
        searchPartiesThread.start();

    }
}
