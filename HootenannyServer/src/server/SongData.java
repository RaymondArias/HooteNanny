package server;

public class SongData {
	private String songID;
	private String songName;
	private String thumbnailURL;
	private int voteCount;
	private int sondIDNum;
	
	/**
	 * Default Constructor
	 */
	public SongData()
	{
		songID = null;
		songName = null;
		thumbnailURL = null;
		voteCount = 0;
		sondIDNum = 0;
	}
	/**
	 * Constructor that handles new song
	 * @param songID
	 * @param songName
	 * @param thumbnailURL
	 * @param voteCount
	 */
	public SongData(String songID, String songName, String thumbnailURL, int voteCount, int sondIDNum)
	{
		this.songID = songID;
		this.songName = songName;
		this.thumbnailURL = thumbnailURL;
		this.voteCount = voteCount;
		this.sondIDNum = sondIDNum;
	}
	public String getSongID() {
		return songID;
	}
	public void setSongID(String songID) {
		this.songID = songID;
	}
	public String getSongName() {
		return songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}
	public String getThumbnailURL() {
		return thumbnailURL;
	}
	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}
	public int getVoteCount() {
		return voteCount;
	}
	public void setVoteCount(int voteCount) {
		this.voteCount = voteCount;
	}
	public int getSongIDNum(){
		return sondIDNum;
	}
	public void setSongIDNum(int sondIDNum)
	{
		this.sondIDNum = sondIDNum;
	}

}
