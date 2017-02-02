package com.inasweaterpoorlyknit.hackpoly2016;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import android.support.design.widget.Snackbar;

/**
 *  Screen to create parties
 *  @author Raymond Arias
 */
public class RegisterPartyActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {


    private AWSServerCommand serverCommand;

    // public string holding the name of the app's user preferences
    public static final String PLAYLIST_PREFS = "PlaylistPrefs";
    // public string holding playlist string set name
    public static final String PLAYLIST_STRING_SET = "PlaylistStringSet";

    // UI references.
    private AutoCompleteTextView partyName;
    private EditText partyHost;
    private View mProgressView;
    private View mLoginFormView;
    private ListView mPlaylistListView;
    private OpenPartyListAdapter playlistAdapter;
    private Activity context;
    private ArrayList<String> partyNames;
    private ArrayList<String> hostNames;
    private ArrayList<Party> partyArrayList;
    private GPSHelper gpsHelper;
    private GoogleApiClient mGoogleApiClient;
    private double latitude;
    private double longitude;
    private Location location;
    Set<String> playlistIDs;
    int newPartyID;
    private final int GET_LOCATION_PERMISSION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_party);
        // Set up the login form.
        partyName = (AutoCompleteTextView) findViewById(R.id.party_name);
        mPlaylistListView = (ListView) findViewById(R.id.playlist_list_view);
        context = this;
        serverCommand = new AWSServerCommand();
        partyNames = new ArrayList<>();
        hostNames = new ArrayList<>();
        //gpsHelper = new GPSHelper(this);
        playlistAdapter = new OpenPartyListAdapter(context, partyNames, hostNames);
        mPlaylistListView.setAdapter(playlistAdapter);
        mPlaylistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Party aParty = partyArrayList.get(position);
                if (aParty != null) {
                    serverCommand.setPartyID(aParty.getPartyID());
                    Intent intent = new Intent(context, ServerLobby.class);
                    intent.putExtra("AWSServerCommand", serverCommand);
                    startActivity(intent);

                }
            }
        });

        partyHost = (EditText) findViewById(R.id.host_name);
        partyHost.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {

                    return true;
                }
                return false;
            }
        });
        Button registerPartyBtn = (Button) findViewById(R.id.register_party);
        registerPartyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptLogin();
                final String partyNameStr = partyName.getText().toString(); //Get the party name
                final String hostNameStr = partyHost.getText().toString();  //Get the host's name
                final Intent intent = new Intent(view.getContext(), ServerLobby.class);
                final GPSHelper gpsHelper = new GPSHelper(context, context);
                Runnable addParty = new Runnable() {
                    @Override
                    public void run() {
                        //double latitude = gpsHelper.getUpdatedLatitude();
                        //double longitude = gpsHelper.getUpdatedLongitude()
                        Log.d("Register Party", "Latitude: " + latitude);
                        Log.d("Register Party", "Longitude: " + longitude);
                        longitude = gpsHelper.getLongitude();
                        latitude = gpsHelper.getLatitude();
                        newPartyID = serverCommand.createParty(partyNameStr, hostNameStr, latitude, longitude); //Contact server to register party
                        intent.putExtra("AWSServerCommand", serverCommand);
                        playlistIDs.add(Integer.toString(newPartyID));// get shared preference for our app and add the song the user chose and commit the changes
                        SharedPreferences sharedPreferences = getSharedPreferences(PLAYLIST_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Set<String> stringSet = sharedPreferences.getStringSet(PLAYLIST_STRING_SET, new HashSet<String>());
                        stringSet.add(Integer.toString(newPartyID));
                        editor.clear();
                        editor.putStringSet(PLAYLIST_STRING_SET, playlistIDs);
                        editor.commit();
                        playlistIDs = sharedPreferences.getStringSet(PLAYLIST_STRING_SET, null);

                        for (int i = 0; i < playlistIDs.size(); i++) {
                            Log.d("Register Party", playlistIDs.toString());
                        }
                        startActivity(intent);
                    }
                };
                Thread createPartyThread = new Thread(addParty);
                createPartyThread.start();


            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPlaylistListView = (ListView) findViewById(R.id.playlist_list_view);

        // retrieve the preferences of the app for the user
        SharedPreferences prefs = getSharedPreferences(PLAYLIST_PREFS, MODE_PRIVATE);
        // if the preferences does not find a preset song, it will play Heart Gongs by Software Blonde
        playlistIDs = prefs.getStringSet(PLAYLIST_STRING_SET, null);
        if (playlistIDs == null) {
            playlistIDs = new HashSet<String>();
        } else {
            final ArrayList<String> playlistIDsArrayList = new ArrayList<String>(playlistIDs);
            Runnable getMyParty = new Runnable() {
                @Override
                public void run() {
                    // send IDs to the server
                    try {
                        partyArrayList = serverCommand.getCreatedParties(playlistIDsArrayList);
                        partyNames.clear();
                        hostNames.clear();
                        for (int i = 0; i < partyArrayList.size(); i++) {
                            Party iParty = partyArrayList.get(i);
                            partyNames.add(iParty.getPartyName());
                            hostNames.add(iParty.getHostName());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playlistAdapter.notifyDataSetChanged();

                            }
                        });

                    } catch (IOException e) {
                        Log.d("Register", "catch");
                        e.printStackTrace();
                    }

                }
            };
            Thread thread = new Thread(getMyParty);
            thread.start();

        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        // retrieve the preferences of the app for the user
        SharedPreferences prefs = getSharedPreferences(PLAYLIST_PREFS, MODE_PRIVATE);
        // if the preferences does not find a preset song, it will play Heart Gongs by Software Blonde
        playlistIDs = prefs.getStringSet(PLAYLIST_STRING_SET, null);
        if (playlistIDs == null) {
            playlistIDs = new HashSet<String>();
        } else {
            final ArrayList<String> playlistIDsArrayList = new ArrayList<String>(playlistIDs);
            Runnable getMyParty = new Runnable() {
                @Override
                public void run() {
                    Log.d("Register", "test");
                    // send IDs to the server
                    try {
                        Log.d("Register", "Try");
                        partyArrayList = serverCommand.getCreatedParties(playlistIDsArrayList);
                        for (int i = 0; i < partyArrayList.size(); i++) {
                            Log.d("Register", playlistIDsArrayList.get(i));
                        }
                        partyNames.clear();
                        hostNames.clear();
                        for (int i = 0; i < partyArrayList.size(); i++) {
                            Log.d("Register", "Loop2");
                            Party iParty = partyArrayList.get(i);
                            partyNames.add(iParty.getPartyName());
                            hostNames.add(iParty.getHostName());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("Register", "Update Adapter");
                                playlistAdapter.notifyDataSetChanged();

                            }
                        });

                    } catch (IOException e) {
                        Log.d("Register", "catch");
                        e.printStackTrace();
                    }

                }
            };
            Thread thread = new Thread(getMyParty);
            thread.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }







    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == GET_LOCATION_PERMISSION)
        {
            Log.d("Register","It Works");
        }
    }




    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GET_LOCATION_PERMISSION);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GET_LOCATION_PERMISSION);

        }
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GET_LOCATION_PERMISSION);

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("Latitude" , ""+latitude);
        Log.d("Longitude", ""+longitude);
    }
    public double []getLocation()
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        double []location = {latitude, longitude};
        return location;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


}
