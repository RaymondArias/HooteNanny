package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

/**
 * Simulation and debugging of server
 * from the host's perspective
 * @author Raymond Arias
 *
 */
public class Host {
	public static void createParty(Scanner userInput)
	{
		
		userInput.nextLine();
		
		String partyName = userInput.nextLine();
		System.out.println("Enter Host Name");
		String hostName = userInput.nextLine();
		System.out.println("Party Name: " + partyName);
		System.out.println("Host Name: " + hostName);
		int partyId = new Random().nextInt();
		System.out.println("Party ID: " + partyId);
		
		try {
			Socket socket = new Socket("ec2-52-11-142-84.us-west-2.compute.amazonaws.com", 7659);
			OutputStream os = socket.getOutputStream();
			os.write(AWSServer.ADD_PARTY);
			os.write(partyId);
			
			PrintStream out = new PrintStream(os);
			out.println(partyName);
			out.println(hostName);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	public static void removeParty(int partyId, String partyName)
	{
		try {
			Socket socket = new Socket("ec2-52-11-142-84.us-west-2.compute.amazonaws.com", 7659);
			OutputStream os = socket.getOutputStream();
			os.write(AWSServer.REMOVE_PARTY);
			os.write(partyId);
			
			PrintStream out = new PrintStream(os);
			out.println(partyName);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	public static void main(String []args)
	{
		System.out.println("Enter a command");
		System.out.println("1: Create Party");
		System.out.println("2: End Party");
		System.out.println("3: Add Song");
		System.out.println("4: Create Party");
		Scanner userInput = new Scanner(System.in);
		while(true)
		{
			System.out.println("Enter a command");
			int command = userInput.nextInt();
			if(command == 1)
			{
				createParty(userInput);
			}
			else if (command == 2)
			{
				userInput.nextLine();
				System.out.println("Enter Party ID:");
				int partyId = userInput.nextInt();
				userInput.nextLine();
				System.out.println("Enter Party Name:");
				String partyName = userInput.nextLine();
				removeParty(partyId, partyName);


			}
				
			
		}
	}
			
}
