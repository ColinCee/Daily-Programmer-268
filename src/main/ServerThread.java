package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerThread implements Runnable {
	
	private Server server;
	private Connection conn;
	
	
	public ServerThread(Connection conn, Server server) throws IOException{
		this.server = server;
		this.conn = conn;
		
	}
	
	@Override
	public void run() {
		try {
			System.out.println(server.getTimeStamp() + " Client connection established to: " + conn.getSocket().getRemoteSocketAddress());
			
			
			User newUser = addNewUser();
			String message = "";
			while(!message.contains("/close")){
				message =  conn.getOis().readObject().toString();
				parseCommands(message, newUser);
			}	
			
			server.deleteUser(newUser);
			conn.close();
			
		}
		catch (IOException | ClassNotFoundException | InterruptedException e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}	
	
	private void parseCommands(String message, User user) throws IOException, InterruptedException {
		StringTokenizer st = new StringTokenizer(message);
		String command = st.nextToken(" ");
		
		switch(command){
			case "/start":
				//Start game here
				cmdStart(user);
				break;
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
				String msg = "User " + user.getUsername() + " says: ";
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
			case "TAKE":
			case "PASS":
				System.out.println("Sending input");
				server.getGameThread().setResponse(user, command);
				break;
			default: 
				sendMessage("Message successfully received, but no action taken", conn);
				break;
		}
		
		if(!message.contains("/close")){
			
		}
	}
	
	private void cmdStart(User user) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		if(user.isStart()){
			sendMessage("You have now opted out of the blackjack game.", conn);
			user.setStart(false);
		}
		else{
			sendMessage("You have set your status to ready.", conn);
			user.setStart(true);
			
			if(!server.checkAllReady())
				sendMessage("Waiting for other players to ready.", conn);
			else{
				cmdBroadcast("Game is starting.");
				server.startGame();
			}
			
		}
	}

	//Send the broadcasted message to each user
	private void cmdBroadcast(String broadcastMsg) throws IOException {
		System.out.println(server.getTimeStamp() + " Broadcast requested");
		
		for(User user : server.getClientMap().keySet())
			sendMessage(broadcastMsg, server.getClientMap().get(user));
		
	}
	
	//Returns all users currently online
	private void cmdOnline() throws IOException {
		String usersOnline = "Users online: ";
		for(User user : server.getClientMap().keySet())
			usersOnline += user.getUsername() + ", ";
		
		//Get rid of last comma
		usersOnline = usersOnline.substring(0, usersOnline.length()-2);
		sendMessage(usersOnline, conn);
		
	}
	
	//Relays a message to a user
	private void cmdMessage(String recipient, String msg) throws IOException {
		
		boolean userFound = false;
		for(User user : server.getClientMap().keySet())
			if(user.equals(recipient)){
				sendMessage(msg, server.getClientMap().get(user));
				userFound = true;
				break;
			}
		
		if(!userFound)
			sendMessage("User " + recipient + " was not found", conn);
		
	}
	
	//Lets the user identify themselves
	private User addNewUser() throws ClassNotFoundException, IOException {
		
		String username = conn.getOis().readObject().toString();
		User user = new User(username);
		
		while(!server.addUser(user, conn)){
			 sendMessage("Username: " + username + " taken", conn);
			 username = conn.getOis().readObject().toString();
			 user = new User(username);
		}
		String message = "Welcome, " + username + " \n";
		message += "Commands are: /broadcast, /online, /close, /ping, /msg \n";
		message += "Type /start to initiate a game of blackjack";
		sendMessage(message, conn);
		
		return user;
	}
	
	private void sendMessage(String message, Connection conn) throws IOException{		
		conn.getOos().writeObject(message);
	}

}
