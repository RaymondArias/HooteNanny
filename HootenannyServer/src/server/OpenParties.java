package server;

/**
 * Handles all the parties running on the app
 */
import java.util.HashMap;

public class OpenParties {
	private HashMap <Integer, Party>currentParties;
	
	
	public OpenParties()
	{
		currentParties = new HashMap();
	}
	
	/**
	 * Adds a new party to the hashmap, that keeps track of the parties
	 * Every party has a partyID, which is its key, a partyName, and the
	 * hostName
	 * @param partyID
	 * @param partyName
	 * @param hostName
	 * @param latitude
	 * @param longitude
	 */
	public void addParty(int partyID, String partyName, String hostName, double latitude, double longitude)
	{
		currentParties.put(partyID, new Party(partyID, partyName, hostName, latitude, longitude));
	}
	/**
	 * Removes a party from the party hashTable, if the party was
	 * not found an error message is displayed
	 * @param partyID
	 * @param partyName
	 */
	public void removeParty(int partyID, String partyName)
	{
		Party removedParty = currentParties.get(partyID);
		
		if(removedParty.getPartyName().equals(partyName))
		{
			currentParties.remove(partyID);
		}
		else
		{
			System.out.println("Party not found");
		}
	}
	/**
	 * Returns the party mapped to partyID
	 * @param partyID
	 * @return
	 */
	public Party getParty(int partyID)
	{
		return currentParties.get(partyID);
	}
	/**
	 * Add a song to the list of a party specified by partyID
	 * @param partyID
	 * @param songID
	 * @param songName
	 * @param thumbnailURL
	 */
	public void addSongToParty(int partyID, String songID, 
			String songName, String thumbnailURL, int songIDNum)
	{
		Party party = currentParties.get(partyID);
		
		if (party != null)
		{
			party.addSong(songID, songName, thumbnailURL, songIDNum);
		}
		else
		{
			System.out.println("Party not found");
		}
	}
	public HashMap getPartyTable()
	{
		return currentParties;
	}
	

}
