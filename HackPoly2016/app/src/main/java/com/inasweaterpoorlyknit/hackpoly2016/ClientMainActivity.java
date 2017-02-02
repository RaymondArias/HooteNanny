package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ClientMainActivity extends AppCompatActivity {
    
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.songrequest";
    public String songRequest;
    Button sendRequest;
    Button connectToHost;

    //private ListView PLAYLISTTITLES;
    //private ArrayList<String> songList;
    //private ArrayAdapter<String> listAdapter;

    private CardView nowPlayingCard;
    private ImageView nowPlayingThumbnailView;
    private TextView nowPlayingText;
    private Bitmap nowPlayingThumbnail;
    private String nowPlayingThumbnailURL;

    private ArrayList<String> playlistTitles;
    private ArrayList<Bitmap> playlistThumbnails;
    private ArrayList<String> thumbnailURLs;
    private ArrayList<String> historyTitles;
    private ArrayList<Bitmap> historyThumbnails;
    private HashMap<String, Bitmap> thumbnailsDownloaded;

    private ViewPager viewPager;    // view pager will link our three fragments
    private TabLayout tabLayout;    // the tabs that initiate the change between fragments

    private HistoryFragment historyFragment;       // fragment to display playlist history
    private PlaylistFragment playlistFragment;      // fragment to display the current playlist
    private SearchFragment searchFragment;          // fragment to allow searching and adding new songs
    private DiscoverPartyFragment discoverPartyFragment;

    SharedPreferences prefs;
    private String returnedVideoID;
    private String returnedVideoTitle;
    private String returnVideoThumbnail;

    public String ipStr;
    TextView hostDisplay;
    private String hostAddress;
    private IntentFilter  intentFilter;
    private Party myParty;
    private AWSServerCommand serverCommand;
    private ArrayList<SongData> songPlaylist;
    private boolean isActivityRunning;

    public static final int GET_PLAYLIST = 1;
    //private static final int SEARCH_CODE = 2;
    public static final int ADD_NEW_SONG = 3;
    public static final int VOTE_SONG = 4;
    public static final int GET_NOW_PLAYING = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);
        isActivityRunning = true;
        playlistTitles = new ArrayList<>();
        playlistThumbnails = new ArrayList<>();
        thumbnailURLs = new ArrayList<>();
        historyTitles = new ArrayList<>();
        historyThumbnails = new ArrayList<>();
        thumbnailsDownloaded = new HashMap<>();
        songPlaylist = new ArrayList<>();

        // initialize playlist fragment with current tracks
        playlistFragment = new PlaylistFragment();  // intialize playlist fragment
        playlistFragment.setPlaylistAdapter(this, playlistTitles, playlistThumbnails);

        // initialize search fragment
        searchFragment = new SearchFragment();

        // initialize history fragment
        historyFragment = new HistoryFragment();  // intialize history fragment
        historyFragment.setPlaylistAdapter(this, historyTitles, historyThumbnails);

        // initialize discovery party fragment
        discoverPartyFragment = new DiscoverPartyFragment();

        // initialize the viewPager to link to the three fragments(Playlist, Search, History)
        viewPager = (ViewPager) findViewById(R.id.client_viewpager);
        setupViewPager(viewPager);

        // initialize the tablayout with the info from viewPager
        tabLayout = (TabLayout) findViewById(R.id.client_tabs);
        tabLayout.setupWithViewPager(viewPager);

        debugCardView();    // displaying the card view with hard coded data

        ipStr ="";
        hostAddress = null;
        myParty = (Party)getIntent().getSerializableExtra("myParty");
        serverCommand = new AWSServerCommand();
        serverCommand.setPartyID(myParty.getPartyID());
        Log.d("AWS SERVER", "Joined: " +myParty.getPartyName());
        Log.d("AWS SERVER", "Party ID: " +myParty.getPartyID());

        nowPlayingCard = (CardView)findViewById(R.id.client_card);
        nowPlayingThumbnailView = (ImageView)nowPlayingCard.findViewById(R.id.cardThumbail);
        nowPlayingText = (TextView)nowPlayingCard.findViewById(R.id.now_playing_song_title);
        getNowPlaying();
        Runnable getNowPlayingClient = new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        if (!isActivityRunning)
                        {
                            return;
                        }
                        serverCommand.getPlaylistClient(myParty.getPartyID(), songPlaylist);
                        updateLists();
                        getNowPlaying();
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread getNowPlayingThread = new Thread(getNowPlayingClient);
        getNowPlayingThread.start();
        playlistFragment.setAwsServerCommand(serverCommand);
        playlistFragment.setSongNames(songPlaylist);
        playlistFragment.setClient(this);

    }
    @Override
    protected void onResume()
    {
        isActivityRunning = true;
        super.onResume();
    }
    @Override
    protected void onDestroy()
    {
        isActivityRunning = false;
        super.onDestroy();
    }

    //   setupViewPager is a layout manager that allows us to flip left and right through fragments
    //   we initialize it with our PlaylistFragment, SearchFragment, and HistoryFragment
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager()); // initialize a viewPagerAdapter
        adapter.addFragment(playlistFragment, getResources().getString(R.string.title_playlist));   // Add the playlist,
        adapter.addFragment(searchFragment, getResources().getString(R.string.title_search));       // search, and
        adapter.addFragment(historyFragment, getResources().getString(R.string.title_history));     // history fragments
        adapter.addFragment(discoverPartyFragment, getResources().getString(R.string.title_discover_party));
        viewPager.setAdapter(adapter);  // set the adapter to our viewPager
    }
    /**
     *
     * @param songId
     * @param songName
     * @param songThumbnail
     * @param songThumbnailURL
     */
    public void addSong(String songId, String songName, Bitmap songThumbnail, String songThumbnailURL) {
        addSongAWS(songId, songName, songThumbnailURL);
        songPlaylist.clear();
        serverCommand.getPlaylistClient(myParty.getPartyID(), songPlaylist);// got the new playlist from server
        playlistTitles.clear();
        thumbnailURLs.clear();

        for(int i = 0; i < songPlaylist.size(); i++)
        {
            SongData song = songPlaylist.get(i);
            playlistTitles.add(song.getSongName());
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
     * Notify server that client want now playing song for this party
     */
    public void getNowPlaying()
    {
        Runnable nowPlayingAction = new Runnable() {
            @Override
            public void run() {
                SongData nowPlayingSong = serverCommand.getNowPlaying(myParty.getPartyID());
                final String nowPlayingSongTitle = nowPlayingSong.getSongName();
                String thumbnailURL = nowPlayingSong.getThumbnailURL();
                if (!nowPlayingThumbnailURL.equals(thumbnailURL)) {
                    historyTitles.add(0, nowPlayingText.getText().toString()); // remove the top song title and place it in front of historySongTitles
                    historyThumbnails.add(0, nowPlayingThumbnail); // remove the top song thumbnail and place it in front of historyThumbnails
                    nowPlayingThumbnailURL = thumbnailURL;

                    final Bitmap nowPlayingThumb = getImage(thumbnailURL);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateNowPlaying(nowPlayingThumb, nowPlayingSongTitle);
                            historyFragment.updateListView();   // update history's list view
                        }
                    });
                }
            }
        };
        Thread nowPlayingThread = new Thread(nowPlayingAction);
        nowPlayingThread.start();

    }

    /**
     * Sets the thumbnail and song title for now playing card
     * @param thumbnail
     * @param songTitle
     */
    public void updateNowPlaying(Bitmap thumbnail, String songTitle)
    {
        nowPlayingThumbnail = thumbnail;
        nowPlayingThumbnailView.setImageBitmap(nowPlayingThumbnail);
        nowPlayingText.setText(songTitle);
    }

    public void debugCardView()
    {
        nowPlayingThumbnailURL = "https://i.ytimg.com/vi/S-Xm7s9eGxU/default.jpg";

        //debug cardView
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final CardView cardView =(CardView)findViewById(R.id.client_card);

                final ImageView cardImage = (ImageView)cardView.findViewById(R.id.cardThumbail);
                final Bitmap testThumb = getImage("https://i.ytimg.com/vi/S-Xm7s9eGxU/default.jpg");
                nowPlayingThumbnail = testThumb;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cardImage.setImageBitmap(testThumb);
                    }
                });

            }
        };
        Thread debugThread = new Thread(runnable);
        debugThread.start();
    }
    public Bitmap getImage(String thumbnailUrl) {
        Bitmap thumbnail = null;
        try {
            InputStream in = new java.net.URL(thumbnailUrl).openStream();
            thumbnail = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnail;
    }

    /**
     * Add song to party mapped to partyID
     * @param songId
     * @param songName
     * @param thumbnailURL
     */
    public void addSongAWS(String songId, String songName, String thumbnailURL)
    {
        serverCommand.addSongToParty(myParty.getPartyID(), songId, songName, thumbnailURL);
        updateLists();
    }
    public void updateLists()
    {

        playlistTitles.clear();
        thumbnailURLs.clear();
        for(int i = 0; i < songPlaylist.size(); i++)
        {
            SongData song = songPlaylist.get(i);
            playlistTitles.add(song.getSongName());
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
    public ArrayList<SongData> getPlaylist()
    {
        return songPlaylist;
    }

}
