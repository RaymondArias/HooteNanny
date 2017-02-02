package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class ServerLobby extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {


    private ArrayList<String> playlistSongIDs;      // current playlist's song IDs
    private ArrayList<String> playlistSongTitles;   // current playlist's song titles
    private ArrayList<String> thumbnailURLs;        //Save the thumbnail strings so can send back to client
    private ArrayList<Bitmap> playlistThumbnails;   // current playlist's song thumbnails
    private ArrayList<SongData> songPlaylist;       //list of video data
    private HashMap<String, Bitmap> thumbnailsDownloaded;

    private ArrayList<String> historySongTitles;    // previous playlist song titles
    private ArrayList<Bitmap> historyThumbnails;    // previous playlist song thumbnails

    private YouTubePlayer player;                   // the YouTube player fragment

    private ViewPager viewPager;    // view pager will link our three fragments
    private TabLayout tabLayout;    // the tabs that initiate the change between fragments

    private HistoryFragment historyFragment;       // fragment to display playlist history
    private PlaylistFragment playlistFragment;      // fragment to display the current playlist
    private SearchFragment searchFragment;          // fragment to allow searching and adding new songs

    private WifiP2pManager manager;                 //Wifi p2p manager for communication to clients
    private WifiP2pManager.Channel channel;         //Wifi p2p needed for manager
    private IntentFilter intentFilter;              //Intent filter that listens for WIFI p2p events

    private String androidKey;                      // android developer key
    private Thread playlistUpdateAWS;
    private boolean isActivityActive;
    private ServerSocket serverSocket;              //Server socket that listens for clients
    private ListView serverListView;                //List for server

    private AWSServerCommand serverCommands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);

        // initialize our arrays to hold the song ids, titles, and thumbnails
        playlistSongIDs = new ArrayList<>();
        playlistSongTitles = new ArrayList<>();
        playlistThumbnails = new ArrayList<>();
        thumbnailURLs = new ArrayList<>();
        historySongTitles = new ArrayList<>();
        historyThumbnails = new ArrayList<>();
        songPlaylist = new ArrayList<>();
        thumbnailsDownloaded = new HashMap<>();

        // four hardcoded songs to assist with debugging
        /*
        COMMENTING OUT FOR AWS SERVER DEBUGGING
        this.addSong("S-Xm7s9eGxU", "Erik Satie - Gymnopédie No.1", "https://i.ytimg.com/vi/S-Xm7s9eGxU/default.jpg");
        this.addSong("HyozVHz9Ml4", "Laurence Equilbey - Cantique de Jean Racine - opus 11 (In Paradisum)", "https://i.ytimg.com/vi/HyozVHz9Ml4/default.jpg");
        this.addSong("iqb60rxl96I", "Eluvium - Radio Ballet", "https://i.ytimg.com/vi/iqb60rxl96I/default.jpg");
        this.addSong("KHlnKXBVFVg", "Wintercoats // Working on a Dream", "https://i.ytimg.com/vi/KHlnKXBVFVg/default.jpg");
        */
        // initialize playlist fragment with current tracks
        playlistFragment = new PlaylistFragment();  // intialize playlist fragment
        playlistFragment.setPlaylistAdapter(this, playlistSongTitles, playlistThumbnails);
        playlistFragment.setSongNames(songPlaylist);
        playlistFragment.setServerLobby(this);

        serverCommands = (AWSServerCommand) getIntent().getSerializableExtra("AWSServerCommand");
        playlistFragment.setAwsServerCommand(serverCommands);
        // initialize search fragment
        searchFragment = new SearchFragment();

        // initialize history fragment
        historyFragment = new HistoryFragment();  // intialize history fragment
        historyFragment.setPlaylistAdapter(this, historySongTitles, historyThumbnails);

        // initialize the viewPager to link to the three fragments(Playlist, Search, History)
        viewPager = (ViewPager) findViewById(R.id.server_viewpager);
        setupViewPager(viewPager);

        // initialize the tablayout with the info from viewPager
        tabLayout = (TabLayout) findViewById(R.id.server_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // initialize our YouTube player through a youTubePlayerFragment
        // all initialization is handled in the functions: OnInitializationSuccess and OnInitializationFailure
        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);

        // accessing our private developerKey.properties folder to hide our personal developer keys
        // this is so our dev keys will not be hosted on github
        // place a developerKey.properties file in your assets folder with a androidKey
        // value if you want this to work
        try {
            // getting our properties file in our asset folder
            AssetManager assetManager = getAssets();
            Properties prop = new Properties();
            String propFileName = "developerKey.properties";
            InputStream inputStream = assetManager.open(propFileName);
            // only set our android key string if we successfully opened the file
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
                androidKey = prop.getProperty("androidKey");
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "'not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        // only initialize our youTubePlayerFragment if our androidKey was obtained
        if (androidKey != null) {
            youTubePlayerFragment.initialize(androidKey, this);
        } else {
            Log.d("androidKey: ", "failed to initialize");
        }

        isActivityActive = true;
        Runnable getPlaylistAWS = new Runnable() {
            @Override
            public void run() {
                serverCommands.getPlaylistClient(serverCommands.getPartyID(), songPlaylist);
                updateLists();

            }
        };

        Thread getPlaylistAWSThread = new Thread(getPlaylistAWS);
        getPlaylistAWSThread.start();
        //Get updated playlist from server every 5 seconds
        Runnable getUpdatedPlaylistAWS = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //Every 5 seconds get updated playlist from server
                        Thread.sleep(2000);
                        if (!isActivityActive)
                            break;
                        if (serverCommands.getPlaylistHost(serverCommands.getPartyID(), songPlaylist)) {
                            updateLists();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        playlistUpdateAWS = new Thread(getUpdatedPlaylistAWS);
        playlistUpdateAWS.start();

    }

    //   setupViewPager is a layout manager that allows us to flip left and right through fragments
    //   we initialize it with our PlaylistFragment, SearchFragment, and HistoryFragment
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager()); // initialize a viewPagerAdapter
        adapter.addFragment(playlistFragment, getResources().getString(R.string.title_playlist));   // Add the playlist,
        adapter.addFragment(searchFragment, getResources().getString(R.string.title_search));       // search, and
        adapter.addFragment(historyFragment, getResources().getString(R.string.title_history));     // history fragments
        viewPager.setAdapter(adapter);  // set the adapter to our viewPager
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverCommands = (AWSServerCommand) getIntent().getSerializableExtra("AWSServerCommand");
        isActivityActive = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        isActivityActive = false;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {   // if the player is new and not just restored
            //youTubePlayer.loadVideo(playlistSongIDs.get(0)); // the first song on our debug list of songs
            this.player = youTubePlayer;
            player.setShowFullscreenButton(false);  // prev & next buttons currently disabled from our player
            serverListView = playlistFragment.getPlaylistListView();
            if(playlistSongIDs.size() > 0)
            {
                player.loadVideo(playlistSongIDs.get(0));
            }
            player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {

                @Override
                public void onLoading() {
                }

                @Override
                public void onLoaded(String s) {

                }

                @Override
                public void onAdStarted() {

                }

                @Override
                public void onVideoStarted() {

                }

                @Override
                public void onVideoEnded() {    // when video ends...
                    if (playlistSongIDs.size() > 0) {   // if there are still songs to remove
                        playlistSongIDs.remove(0);  // remove the top song id
                        historySongTitles.add(0, playlistSongTitles.remove(0)); // remove the top song title and place it in front of historySongTitles
                        historyThumbnails.add(0, playlistThumbnails.remove(0)); // remove the top song thumbnail and place it in front of historyThumbnails
                        thumbnailURLs.remove(0);
                        final SongData removedSong = songPlaylist.remove(0);
                        if (!playlistSongIDs.isEmpty()) { // if there are more videos to load
                            player.loadVideo(playlistSongIDs.get(0)); // load the first video on the list
                        }
                        playlistFragment.updateListView();
                        historyFragment.updateListView();
                        Runnable removeSongFromServer = new Runnable() {
                            @Override
                            public void run() {
                                serverCommands.removeSong(serverCommands.getPartyID(), removedSong.getSongID(), removedSong.getSongName());
                                //songPlaylist.clear();
                                if (serverCommands.getPlaylistHost(serverCommands.getPartyID(), songPlaylist)) {
                                    updateLists();
                                }


                            }
                        };
                        Thread removeSongThread = new Thread(removeSongFromServer);
                        removeSongThread.start();
                    } else {
                        Log.d("LIST IS EMPTY", "serverMSG"); // else, print that our list is empty to debug log
                    }
                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {
                }
            });
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        // do nothing
    }

    public void addSong(String songID, String songTitle, Bitmap songThumbnail, String thumbnailStr) {
        playlistSongIDs.add(songID);    // add the songID to playlist
        playlistSongTitles.add(songTitle); // add the song title to playlist
        playlistThumbnails.add(songThumbnail); // add the song thumbnail to playlist
        thumbnailURLs.add(thumbnailStr);        //add the url of thumbnail for now playing on client
        addSongAWS(songID, songTitle, thumbnailStr);
        //If the player is not playing and the playlist is less than 1
        //play the song just added to list
        if (!player.isPlaying() && playlistSongIDs.size() <= 1) {
            player.loadVideo(songID);
        }
        playlistFragment.updateListView(); // notify playlistFragment of the changes
    }

    // add song function for our search fragment
    private void addSong(String songID, String songTitle, String songThumbnailURL) {
        playlistSongIDs.add(songID);    // add the songID to playlist
        playlistSongTitles.add(songTitle); // add the song title to playlist
        thumbnailURLs.add(songThumbnailURL);

        // AsyncTask to download the thumbnail images
        class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            // downloads any number of URLs in the background
            protected Bitmap doInBackground(String... urls) {
                String urlDisplay = urls[0];    // save the first url
                Bitmap thumbnail = null;          // thumbnail, set to null
                try {
                    InputStream in = new java.net.URL(urlDisplay).openStream(); // get an input stream from specified url
                    thumbnail = BitmapFactory.decodeStream(in);   // decode the inputStream as a Bitmap
                } catch (Exception e) { // printe any errors
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return thumbnail;   // return the thumbnail
            }

            // called after bitmap is loaded and returned from doInBackground()
            protected void onPostExecute(Bitmap result) {
                playlistThumbnails.add(result); // add the song thumbnail to playlist
                playlistFragment.updateListView(); // notify playlistFragment of the changes
            }
        }
        new DownloadImageTask().execute(songThumbnailURL);  // download the thumbnail for that song and set as bitmap for the imageview
    }

    /**
     * Add song to the parties playlist
     *
     * @param songID
     * @param songTitle
     * @param thumbnailStr
     */
    public void addSongAWS(final String songID, final String songTitle, final String thumbnailStr) {
        Runnable addSong = new Runnable() {
            @Override
            public void run() {
                serverCommands.addSongToParty(serverCommands.getPartyID(), songID, songTitle, thumbnailStr);

                if (serverCommands.getPlaylistHost(serverCommands.getPartyID(), songPlaylist)) {
                    updateLists();
                }


            }
        };
        Thread addSongThread = new Thread(addSong);
        addSongThread.start();
    }

    /**
     * Update all the arraylists and hashmaps in the server lobby
     */
    public void updateLists() {
        playlistSongIDs.clear();
        playlistSongTitles.clear();
        thumbnailURLs.clear();
        for (int i = 0; i < songPlaylist.size(); i++) {
            SongData song = songPlaylist.get(i);
            playlistSongTitles.add(song.getSongName());
            playlistSongIDs.add(song.getSongID());
            String aSongThumbnailURL = song.getThumbnailURL();
            //Add to arraylist of URLS and check if the thumbnail has
            //been downloaded already
            thumbnailURLs.add(aSongThumbnailURL);

            if (!thumbnailsDownloaded.containsKey(aSongThumbnailURL)) {
                Bitmap thumbnail = getImage(aSongThumbnailURL);
                //Put the bitmap associated the thumbnail URL is the hashmap
                thumbnailsDownloaded.put(aSongThumbnailURL, thumbnail);
            }

        }
        playlistThumbnails.clear();
        //Go through every string value in the new thumbnailURL list
        //and add the Bitmaps associated with these URLS to the
        //playlistThumbnails arraylist
        for (int i = 0; i < thumbnailURLs.size(); i++) {
            Bitmap tempThumbnail = thumbnailsDownloaded.get(thumbnailURLs.get(i));
            playlistThumbnails.add(tempThumbnail);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playlistFragment.updateListView();
            }
        });

    }

    /**
     * Get the bitmap image from the url passed in
     *
     * @param thumbnailURL
     * @return
     */
    public Bitmap getImage(String thumbnailURL) {
        Bitmap thumbnail = null;

        try {
            InputStream in = new java.net.URL(thumbnailURL).openStream(); // get an input stream from specified url
            thumbnail = BitmapFactory.decodeStream(in);   // decode the inputStream as a Bitmap
        } catch (Exception e) { // printe any errors
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return thumbnail;
    }

    public ArrayList<SongData> getSongPlaylist() {
        return songPlaylist;
    }

}
