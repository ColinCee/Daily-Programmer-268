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
	private Scanner socketIn;
	private ObjectOutputStream objectOutput;
	private ObjectInputStream objectInput;

	public Client(String ip, int port) throws IOException {
		socket = new Socket(ip, port);
		socketIn = new Scanner(socket.getInputStream());
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

	public static void main(String[] args) {

		try {
			String message = "";
			String response = "";
			Scanner scan = new Scanner(System.in);
			Client client = new Client("86.149.131.88", 10);
			
			System.out.println("Please enter your username:");
			message = scan.nextLine();
			
			do {
				while (message.isEmpty()) {
					System.out.println("Input is empty please try again:");
					message = scan.nextLine();
				}

				client.sendMessage(message);
				response = client.getServerResponse();
				
				if(response.contains("Closing"))
					break;
				else
					System.out.println(response);
				
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
