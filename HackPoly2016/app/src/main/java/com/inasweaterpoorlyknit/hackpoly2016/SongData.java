package com.inasweaterpoorlyknit.hackpoly2016;

/**
 * Holds data about a youtube video
 * Created by raymond on 6/16/16.
 */

public class SongData {
    private String songID;
    private String songName;
    private String thumbnailURL;
    private int voteCount;
    private int songIDNum;

    /**
     * Default Constructor
     */
    public SongData()
    {
        songID = null;
        songName = null;
        thumbnailURL = null;
        voteCount = 0;
    }
    /**
     * Constructor that handles new song
     * @param songID
     * @param songName
     * @param thumbnailURL
     * @param voteCount
     */
    public SongData(String songID, String songName, String thumbnailURL, int voteCount, int songIDNum)
    {
        this.songID = songID;
        this.songName = songName;
        this.thumbnailURL = thumbnailURL;
        this.voteCount = voteCount;
        this.songIDNum = songIDNum;
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
    public int getSongIDNum()
    {
        return songIDNum;
    }
    public void setSongIDNum(int songIDNum){
        this.songIDNum = songIDNum;
    }
    public void incVoteCount(){
        voteCount++;
    }
    public void decVoteCount(){
        voteCount--;
    }
}
