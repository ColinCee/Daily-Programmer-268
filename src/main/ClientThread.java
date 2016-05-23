package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class ClientThread implements Runnable {
	
	private Socket socket;
	private Server server;
	private ObjectOutputStream objectOutput;
	private ObjectInputStream objectInput;
	
	
	public ClientThread(Socket clientSocket, Server server) throws IOException{
		this.server = server;
		socket = clientSocket;
		objectOutput = new ObjectOutputStream(socket.getOutputStream());
		objectInput = new ObjectInputStream(socket.getInputStream());
	}
	@Override
	public void run() {
		try {
			System.out.println("Client connection established to: " + socket.getRemoteSocketAddress());
			
			String message = " ";
			String username = objectInput.readObject().toString();
			if(!server.addUser(username, socket))
				sendMessage("Username:" + username + " taken");
			else{
				sendMessage("Welcome, " + username + " \n" + "Commands are: /broadcast, /online");
			}
				
			do{
				message =  objectInput.readObject().toString();
				System.out.println("Client sends: " + message);
				parseCommands(message);
				
			}while(!message.contains("/close"));	
			
			System.out.println("Closing connection");
			server.deleteUser(username);
			objectOutput.close();
			objectInput.close();
			socket.close();
		}
		catch (IOException | ClassNotFoundException e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}	
	
	private void parseCommands(String message) throws IOException {
		StringTokenizer st = new StringTokenizer(message);
		String command = st.nextToken(" ");
		
		switch(command){
			case "/broadcast":	
				System.out.println("Broadcast requested");
				break;
			case "/online":
				String usersOnline = "";
				for(String user : server.getClientMap().keySet())
					usersOnline += user + ", ";
				sendMessage(usersOnline);
				break;
			case "/close":
				sendMessage("Closing connection");
				break;
			default: 
				sendMessage("Message successfully received, but no action taken");
				break;
		}
	}
	private void sendMessage(String message) throws IOException{
		//Create a new ObjectOutputStream and use the socket's output stream...
		//then get the character and digits counts using the MessageImpl class...
		//and finally writing the MessageImpl object to the socket's output stream.
		
		objectOutput.writeObject(message);
		
	}

}
