/** 
 * This client class takes input from the user and sends 
 *  it to the server then waits for the server to respond.
 *  This process repeats until the client sends a closing
 *  command.
 *
 * @author Colin Cheung
 * 
 */
package main;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.*;

public class Client {

	private Socket socket;
	private ObjectOutputStream objectOutput;
	private ObjectInputStream objectInput;
	private Scanner scan;
	private SimpleDateFormat sdf;
	
	public Client(String ip, int port) throws IOException, ClassNotFoundException{
		socket = new Socket(ip, port);
		objectOutput = new ObjectOutputStream(socket.getOutputStream());
		objectInput = new ObjectInputStream(socket.getInputStream());
		scan = new Scanner(System.in);
		sdf = new SimpleDateFormat("'['HH:mm:ss']'");
		setUsername();
	}
	
	public void setUsername() throws ClassNotFoundException, IOException{

		String message = "";
		print("Please enter your username:");
		sendMessage(scan.nextLine());
		String response = getServerResponse();
		print(response);
		
		while(response.contains("taken")){
			print("Please enter another username: ");
			message = scan.nextLine();
			sendMessage(message);
			response = getServerResponse();
			print(response);
		}
		
	}
	
	public void pingServer() throws IOException, ClassNotFoundException{
		long startTime = System.currentTimeMillis();
		print("Pinging...");
		sendMessage("/ping " + startTime);

	}
	public void sendMessage(String message) throws IOException {
		
		objectOutput.writeObject(message);
	}

	public String getServerResponse() throws IOException, ClassNotFoundException {

		return objectInput.readObject().toString();
	}
	
	public Scanner getScanner() {
		return scan;
	}

	public void closeConnection() throws IOException{
		
		objectOutput.close();
		objectInput.close();
		socket.close();
	}
	
	public void print(String message){
		System.out.println(getTimeStamp() + " " +  message);
	}
	

	private String getTimeStamp(){
		Date date = new Date();
		return(sdf.format(date));
	}
	
	public static void main(String[] args) {

		try {
			String message = "";
			StringTokenizer st = null;
			Client client = new Client("86.149.131.88", 10);
			Scanner scan = client.getScanner();
			
			Thread clientThread = new Thread(new ClientThread(client));
			clientThread.start();
			
			do {			
				message = scan.nextLine();
				st = new StringTokenizer(message);
				
				if(st.nextToken().equals("/ping"))
					client.pingServer();
				else{
					while (message.isEmpty()) {
						client.print("Input is empty please try again:");
						message = scan.nextLine();
					}

					client.sendMessage(message);
					
				}

			} while (!message.contains("/close"));

			client.print("Exiting...");
			client.sendMessage("/close");
			scan.close();
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
