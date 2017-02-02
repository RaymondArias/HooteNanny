package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Backend server for Hootenanny
 *
 * @author Raymond Arias
 *
 */

public class AWSServer {
	private OpenParties openParties;
	public static int ADD_PARTY = 1;
	public static int ADD_SONG_TO_PARTY = 2;
	public static int REMOVE_SONG = 3;
	public static int REMOVE_PARTY = 4;
	public static int SHOW_PARTIES = 5;
	public static int GET_SONGS_FROM_PARTY = 6;
	public static int VOTE_SONG = 7;
	public static int GET_NOW_PLAYING = 8;
	public static int GET_SONGS_CLIENT = 9;
	public static int GET_MY_PARTIES = 10;
	private static String endConnect = "END THE SOCKET NOW!!!!";

	public AWSServer() {
		openParties = new OpenParties();
	}

	public OpenParties getOpenParties() {
		return openParties;
	}

	public static void main(String[] args) {
		final AWSServer awsServer = new AWSServer();
		try {
			ServerSocket server = new ServerSocket(7659);
			while (true) {

				final Socket socket = server.accept();
				Runnable newConnection = new Runnable() {
					@Override
					public void run() {
						try {
							System.out.println(socket.getRemoteSocketAddress().toString() + " connected");
							InputStream in = socket.getInputStream();
							OutputStream os = socket.getOutputStream();

							int requestType = in.read();

							if (requestType == ADD_PARTY) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								// Get the party ID
								int partyID = Integer.parseInt(br.readLine());
								// get the party name
								String partyName = br.readLine();
								//get the host name
								String hostName = br.readLine();
								//get longititude and latitude
								double latitude = Double.parseDouble(br.readLine());
								double longitude = Double.parseDouble(br.readLine());
								// Add party to hash table of open parties
								awsServer.getOpenParties().addParty(partyID, partyName,
										hostName, latitude, longitude);
								System.out.println("Party " + partyName + " added");
								System.out.println("Party ID: " + partyID);
								System.out.println("Latitude " + latitude);
								System.out.println("Longitude: " + longitude);

							} else if (requestType == REMOVE_PARTY) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								int partyID = Integer.parseInt(br.readLine());
								String partyName = br.readLine();
								awsServer.getOpenParties().removeParty(partyID, partyName);
								System.out.println("Party " + partyName + " removed");
							} else if (requestType == ADD_SONG_TO_PARTY) {

								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								int partyID = Integer.parseInt(br.readLine());
								String songID = br.readLine();
								String songName = br.readLine();
								String songThumbnail = br.readLine();
								int songIDNum = Integer.parseInt(br.readLine());
								awsServer.getOpenParties().addSongToParty(partyID, songID,
										songName, songThumbnail, songIDNum);
								System.out.println("Song " + songName +
										" Added to party id: " + partyID);
								Party aParty = awsServer.getOpenParties().getParty(partyID);
								aParty.sortSongList(songIDNum);
							} else if (requestType == SHOW_PARTIES) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								// Party name being searched for
								String partyQuery = br.readLine(); 
								PrintStream printStream = new PrintStream(os);

								HashMap<Integer, Party> allParties = awsServer.
										getOpenParties().getPartyTable();
								for (Map.Entry<Integer, Party> entry : allParties.entrySet()) {
									Party aParty = entry.getValue();
									String partyName = aParty.getPartyName();
									String hostName = aParty.getHostName();
									double latitude = aParty.getLatitude();
									double longitude = aParty.getLongitude();
									// send party id
									printStream.println(aParty.getPartyID()); 
									printStream.println(partyName);
									printStream.println(hostName);
									printStream.println(latitude);
									printStream.println(longitude);

									System.out.println("\nID: " + aParty.getPartyID());
									System.out.println("Party Name: " + aParty.getPartyName());
									System.out.println("Host Name: " + aParty.getHostName());
								}
								printStream.println(endConnect);
							} else if (requestType == GET_SONGS_FROM_PARTY) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								// Get the party id
								int partyID = Integer.parseInt(br.readLine()); 
								PrintStream printStream = new PrintStream(os);
								// Get the party mapped to partyID
								Party aParty = awsServer.openParties.getParty(partyID); 

								if (aParty.getHasPlayListChanged()) {
									// Get the arraylist of song data for this party
									ArrayList<SongData> songData = aParty.getSongs();
									// Send the size of list to client
									printStream.println(songData.size()); 
									for (int i = 0; i < songData.size(); i++) {
										// Send songID
										String songID = songData.get(i).getSongID(); 
										// Send songName
										String songName = songData.get(i).getSongName(); 
										// Send song thumbnail
										String thumbnailUrl = songData
												.get(i).getThumbnailURL(); 
										int songIDNum = songData.get(i).getSongIDNum();
										printStream.println(songID);
										printStream.println(songName);
										printStream.println(thumbnailUrl);
										printStream.println(songIDNum);
										System.out.println("Song Name: " + songName);
										System.out.println("Song ID :" + songID);
									}
									aParty.setHasPlayListChanged(false);

								} else {
									printStream.println("NO CHANGE");
								}

							} else if (requestType == GET_SONGS_CLIENT) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								// Get the party id
								int partyID = Integer.parseInt(br.readLine()); 
								PrintStream printStream = new PrintStream(os);
								// Get the party mapped to partyID
								Party aParty = awsServer.openParties.getParty(partyID);
								// Get the arraylist of song data for this party
								ArrayList<SongData> songData = aParty.getSongs(); 
								printStream.println(songData.size()); 
								for (int i = 0; i < songData.size(); i++) {
									String songID = songData.get(i).getSongID(); 
									String songName = songData.get(i).getSongName(); 
									String thumbnailUrl = songData.get(i).getThumbnailURL(); 
									int songIDNum = songData.get(i).getSongIDNum();
									printStream.println(songID);
									printStream.println(songName);
									printStream.println(thumbnailUrl);
									printStream.println(songIDNum);
									System.out.println("Song Name: " + songName);
									System.out.println("Song ID :" + songID);
								}

							} else if (requestType == REMOVE_SONG) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								int partyID = Integer.parseInt(br.readLine());

								String songID = br.readLine();
								String songName = br.readLine();
								Party aParty = awsServer.getOpenParties().getParty(partyID);
								System.out.println("Party Name: " + aParty.getPartyName());
								aParty.removeSong(songID, songName);
							} else if (requestType == VOTE_SONG) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								int partyID = Integer.parseInt(br.readLine());

								System.out.println(partyID);

								int songIDNum = Integer.parseInt(br.readLine());
								int voteNum = Integer.parseInt(br.readLine());
								Party aParty = awsServer.getOpenParties().getParty(partyID);
								// System.out.println("Party Name: " +
								// aParty.getPartyName());
								aParty.changeVote(songIDNum, voteNum);
								aParty.sortSongList(songIDNum);

							} else if (requestType == GET_NOW_PLAYING) {
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								PrintStream printStream = new PrintStream(os);
								int partyID = Integer.parseInt(br.readLine());
								Party aParty = awsServer.getOpenParties().getParty(partyID);
								SongData nowPlayingSong = aParty.getSong(0);
								String nowPlayingSongTitle = nowPlayingSong.getSongName();
								String nowPlayingSongThumbnail = nowPlayingSong.getThumbnailURL();
								printStream.println(nowPlayingSongTitle);
								printStream.println(nowPlayingSongThumbnail);
								System.out.println("Party Name: " + aParty.getPartyName());
							} else if (requestType == GET_MY_PARTIES) {
								System.out.println("Getting parties for user");
								InputStreamReader read = new InputStreamReader(in, "UTF-8");
								BufferedReader br = new BufferedReader(read);
								PrintStream printStream = new PrintStream(os);
								int numOfKeys = Integer.parseInt(br.readLine());

								for (int i = 0; i < numOfKeys; i++) {

									int partyID = Integer.parseInt(br.readLine());
									System.out.println(partyID);
									Party aParty = awsServer.openParties.getParty(partyID);
									System.out.println(aParty);

									if (aParty != null) {
										System.out.println(aParty.getHostName());
										System.out.println(aParty.getPartyName());

										printStream.println(aParty.getPartyName());
										printStream.println(aParty.getHostName());

									} else {
										printStream.println("Skip");
									}
								}
								printStream.println(endConnect);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				};
				Thread connectionThread = new Thread(newConnection);
				connectionThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Connection error");
		}
	}

}
