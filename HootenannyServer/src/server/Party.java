package server;

import java.util.ArrayList;

public class Party {
	private ArrayList<SongData> songList;
	private int partyID;
	private String partyName;
	private String hostName;
	private boolean hasPlayListChanged;
	private double latitude;
    private double longitude;

	public Party()
	{
		songList = null;
		partyID = 0;
		partyName = null;
		hostName = null;
		hasPlayListChanged = false;
	}
	public Party(int partyID, String partyName, String hostName, double latitude, double longitude)
	{
		this.partyID = partyID;
		this.partyName = partyName;
		this.hostName = hostName;
		this.latitude = latitude;
		this.longitude = longitude;
		songList = new ArrayList();
	}
	public ArrayList<SongData> getSongs() {
		return songList;
	}
	public void setSongs(ArrayList<SongData> songs) {
		this.songList = songs;
	}
	public int getPartyID() {
		return partyID;
	}
	public void setPartyID(int partyID) {
		this.partyID = partyID;
	}
	public String getPartyName() {
		return partyName;
	}
	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public void setHasPlayListChanged(boolean value)
	{
		hasPlayListChanged = value;
	}
	public boolean getHasPlayListChanged()
	{
		return hasPlayListChanged;
	}
	/**
	 * Add song to this party's song list
	 * @param songID
	 * @param songName
	 * @param thumbnailURL
	 */
	public void addSong(String songID, String songName, String thumbnailURL, int songIDNum)
	{
		songList.add(new SongData(songID, songName, thumbnailURL, 0, songIDNum));
		hasPlayListChanged = true;
	}
	/**
	 * Get the ith song from list
	 * @param index
	 * @return
	 */
	public SongData getSong(int index)
	{
		return songList.get(index);

	}
	public void removeSong(String songID, String songName)
	{
		for(int i = 0; i < songList.size(); i++)
		{
			SongData song = songList.get(i);
			if(song.getSongID().equals(songID) && song.getSongName().equals(songName))
			{
				songList.remove(i);
				System.out.println("\n Song: " + song.getSongName() + " removed from party");
				hasPlayListChanged = true;
				break;
			}
		}
	}

	public void changeVote(int songID, int voteNum){
		for(int i = 0; i < songList.size(); i++){
			if(songID == songList.get(i).getSongIDNum()){
				int currentVote = songList.get(i).getVoteCount();
				int newVote = currentVote + voteNum;
				songList.get(i).setVoteCount(newVote);
				break;
			}
		}
		hasPlayListChanged = true;
	}
	public void sortSongList(int songIDNum){
		if(songList.size() <= 1){
			return;
		}
		System.out.println(songIDNum);
		SongData song = null;
		int j = 0;
		for(int i = 0; i < songList.size(); i++){
			if(songList.get(i).getSongIDNum() == songIDNum){
				song = songList.remove(i);
				System.out.println(song.getSongName());
				break;
			}
		}
		while(songList.get(songList.size()-j-1).getVoteCount() < song.getVoteCount()){
			System.out.println(songList.get(j).getSongName());
			j++;
			if(j + 1 == songList.size()){
				break;
			}
		}

		songList.add(songList.size()-j, song);
		hasPlayListChanged = true;
	}
}
