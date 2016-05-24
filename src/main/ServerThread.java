package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerThread implements Runnable {
	
	private Socket socket;
	private Server server;
	private Connection conn;
	
	
	public ServerThread(Socket clientSocket, Server server) throws IOException{
		this.server = server;
		socket = clientSocket;
		conn = new Connection(socket);
	}
	
	@Override
	public void run() {
		try {
			System.out.println(server.getTimeStamp() + " Client connection established to: " + socket.getRemoteSocketAddress());
			
			
			String username = getUsername();
			String message = conn.getOis().readObject().toString();
			
			while(!message.contains("/close")){
				parseCommands(message, username);
				message =  conn.getOis().readObject().toString();
			}	
			
			server.deleteUser(username);
			conn.close();
			
		}
		catch (IOException | ClassNotFoundException e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}	
	
	private void parseCommands(String message, String username) throws IOException {
		StringTokenizer st = new StringTokenizer(message);
		String command = st.nextToken(" ");
		
		switch(command){
			case "/broadcast":	
				String broadcastMsg = "Server wide brodcast: ";
				broadcastMsg += message.substring(command.length());
				cmdBroadcast(broadcastMsg);
				break;
			case "/online":
				cmdOnline();
				break;
			case "/msg":
				String recipient = st.nextToken(" ");
				String msg = "User " + username + " says: ";
				msg += message.substring(command.length() + recipient.length() + 2, message.length()); 
				
				cmdMessage(recipient, msg);
				break;
			case "/ping":
				String sendTime = st.nextToken();
				sendMessage("pingback " + sendTime, conn);
				break;
			case "/close":
				sendMessage("Closing connection", conn);
				break;
			default: 
				sendMessage("Message successfully received, but no action taken", conn);
				break;
		}
	}
	
	//Send the broadcasted message to each user
	private void cmdBroadcast(String broadcastMsg) throws IOException {
		System.out.println(server.getTimeStamp() + " Broadcast requested");
		
		for(String user : server.getClientMap().keySet())
			sendMessage(broadcastMsg, server.getClientMap().get(user));
		
	}
	
	//Returns all users currently online
	private void cmdOnline() throws IOException {
		String usersOnline = "Users online: ";
		for(String user : server.getClientMap().keySet())
			usersOnline += user + ", ";
		
		//Get rid of last comma
		usersOnline = usersOnline.substring(0, usersOnline.length()-2);
		sendMessage(usersOnline, conn);
		
	}
	
	//Relays a message to a user
	private void cmdMessage(String recipient, String msg) throws IOException {
		
		boolean userFound = false;
		for(String user : server.getClientMap().keySet())
			if(user.equals(recipient)){
				sendMessage(msg, server.getClientMap().get(user));
				userFound = true;
				break;
			}
		
		if(!userFound)
			sendMessage("User " + recipient + " was not found", conn);
		
	}
	
	//Lets the user identify themselves
	private String getUsername() throws ClassNotFoundException, IOException {
		String username = conn.getOis().readObject().toString();
		
		while(!server.addUser(username, conn)){
			 sendMessage("Username: " + username + " taken", conn);
			 username = conn.getOis().readObject().toString();
		}
		
		sendMessage("Welcome, " + username + " \n" + "Commands are: /broadcast, /online, /close, /ping, /msg", conn);
		
		return username;
	}
	
	private void sendMessage(String message, Connection conn) throws IOException{		
		conn.getOos().writeObject(message);
	}

}
