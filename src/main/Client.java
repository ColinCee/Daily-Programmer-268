/** 
 * This client class takes a message from the user and sends 
 *  it to the server then waits for the server to respond with 
 *  how many characters and digits the message contains.
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

	/**
	 * @param message
	 *            The message to be sent to the server
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		// Create a new ObjectOutputStream and use the socket's output stream...
		// then write the message object (string) to it.
		
		objectOutput.writeObject(message);
	}

	public String getServerResponse() throws IOException, ClassNotFoundException {
		// Create a new ObjectInputStream and use the socket's input stream...
		// then read the MessageImpl object and print the amount of
		// characters...
		// and digits.

		return objectInput.readObject().toString();
	}

	/*
	 * This main method currently only uses a pre-set message to send to the
	 * server. If you would like to type into the console then uncomment
	 * everything in this method and then comment out the original 2 lines.
	 */
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
