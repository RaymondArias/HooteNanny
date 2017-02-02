package com.inasweaterpoorlyknit.hackpoly2016;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

/**
*   PlaylistFragment displays a playlist of songTitles and songThumbnails
*   This is used for the playlistFragment AND historyFragment of the ServerLobby
**/

public class PlaylistFragment extends Fragment {

    private ListView playlistListView; // list view to display the playlist
    private PlaylistAdapter playlistAdapter;
    private ArrayList<SongData> songNames;
    private AWSServerCommand awsServerCommand;
    private ServerLobby serverLobby;
    private ClientMainActivity client;
    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // accomplish most tasks only when the view is created in onCreateView
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        this.playlistListView = (ListView) rootView.findViewById(R.id.playlist_fragment_list_view);  // access the listview from xml
        this.playlistListView.setAdapter(this.playlistAdapter);
        /*
        Angel this new position works to put the onClickListener and it does not
        break if we press history. The one thing youll need to do extra is give
        this class a reference to the arraylist from the server lobby. This
        can be done with some setter methods and giving the class a few extra
        data members
         */
        this.playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getContext();
                if(position == 0)
                {
                    CharSequence text = "Now Playing!";
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    return;
                }
                //Get the song title from the playlistSongTitles using the index = postion
                //position is the entry in the list clicked
                Object ob = playlistListView.getAdapter().getItem(position);
                String chosenSong = playlistAdapter.getItem(position).toString();
                CharSequence text = "Chosen song: " + chosenSong;
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                createVotingDialog(position);
            }
        });

        return rootView;    // Inflate the layout for this fragment
    }


    public void setPlaylistAdapter(Activity context, ArrayList<String> songTitles, ArrayList<Bitmap> thumbnails){
        this.playlistAdapter = new PlaylistAdapter(context, songTitles, thumbnails);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateListView(){
        this.playlistAdapter.notifyDataSetChanged();
    }

    public ListView getPlaylistListView() {
        return playlistListView;
    }
    public void setSongNames(ArrayList<SongData> songNames)
    {
        this.songNames = songNames;
    }
    /**
     * Create a voting dialog box when user presses a
     * entry in the song list
     */
    public void createVotingDialog(final int position) {
        AlertDialog.Builder votingDialog = new AlertDialog.Builder(getContext());

        votingDialog.setTitle("Vote for song");
        votingDialog.setPositiveButton("Give A Hoot!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Increment the vote counter for the song clicked on
                //songNames.get(position).incVoteCount();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(serverLobby != null)
                        {
                            awsServerCommand.voteCountChanged(songNames.get(position), 1, awsServerCommand.getPartyID());
                            awsServerCommand.getPlaylistHost(awsServerCommand.getPartyID(), serverLobby.getSongPlaylist());
                            serverLobby.updateLists();

                        }
                        else
                        {
                            awsServerCommand.voteCountChanged(songNames.get(position), 1, awsServerCommand.getPartyID());
                            awsServerCommand.getPlaylistClient(awsServerCommand.getPartyID(), client.getPlaylist());
                            client.updateLists();

                        }

                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        votingDialog.setNegativeButton("Don't Give A Hoot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Decrement the vote counter for the song clicked on
                //songNames.get(position).decVoteCount();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(serverLobby != null)
                        {
                            awsServerCommand.voteCountChanged(songNames.get(position), -1, awsServerCommand.getPartyID());
                            awsServerCommand.getPlaylistHost(awsServerCommand.getPartyID(), serverLobby.getSongPlaylist());
                            serverLobby.updateLists();

                        }
                        else
                        {
                            awsServerCommand.voteCountChanged(songNames.get(position), -1, awsServerCommand.getPartyID());
                            awsServerCommand.getPlaylistClient(awsServerCommand.getPartyID(), client.getPlaylist());
                            client.updateLists();

                        }

                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
        
        AlertDialog dialog = votingDialog.create();
        dialog.show();
    }
    public void setServerLobby(ServerLobby serverLobby)
    {
        this.serverLobby = serverLobby;
    }


    public void setAwsServerCommand(AWSServerCommand awsServerCommand){
        this.awsServerCommand = awsServerCommand;

    }
    public void setClient(ClientMainActivity client)
    {
        this.client = client;
    }
}
