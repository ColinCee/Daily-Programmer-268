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
import java.util.Scanner;
import java.io.*;

public class Client {

	private Socket socket;
	private ObjectOutputStream objectOutput;
	private ObjectInputStream objectInput;

	public Client(String ip, int port) throws IOException {
		socket = new Socket(ip, port);
		objectOutput = new ObjectOutputStream(socket.getOutputStream());
		objectInput = new ObjectInputStream(socket.getInputStream());
	}
	
	public void sendMessage(String message) throws IOException {
		
		objectOutput.writeObject(message);
	}

	public String getServerResponse() throws IOException, ClassNotFoundException {

		return objectInput.readObject().toString();
	}

	public Socket getSocket() {
		return socket;
	}
	
	public void closeConnection() throws IOException{
		
		objectOutput.close();
		objectInput.close();
		socket.close();
	}

	public static void main(String[] args) {

		try {
			String message = "";
			Scanner scan = new Scanner(System.in);
			Client client = new Client("86.149.131.88", 10);
			Thread clientThread = new Thread(new ClientThread(client));
			
			System.out.println("Please enter your username:");
			message = scan.nextLine();
			client.sendMessage(message);
			String response = client.getServerResponse();
			System.out.println(response);
			
			while(response.contains("taken")){
				System.out.println("Please enter another username: ");
				message = scan.nextLine();
				client.sendMessage(message);
				response = client.getServerResponse();
				System.out.println(response);
			}
			
			clientThread.start();
			do {
				while (message.isEmpty()) {
					System.out.println("Input is empty please try again:");
					message = scan.nextLine();
				}

				client.sendMessage(message);
				message = scan.nextLine();

			} while (!message.contains("/close"));

			System.out.println("Exiting...");
			client.sendMessage("/close");
			scan.close();
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
