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
			server.print(" Client connection established to: " + conn.getSocket().getRemoteSocketAddress());
			
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
			case "/command":
				String commands = "Commands are: /command, /broadcast, /online, /close, /ping, /msg, /start";
				server.sendMessage(commands, conn);
				break;
			case "/broadcast":	
				String broadcastMsg = "Server wide brodcast: ";
				broadcastMsg += message.substring(command.length());
				server.broadcast(user, broadcastMsg);
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
				server.sendMessage("pingback " + sendTime, conn);
				break;
			case "/close":
				server.sendMessage("Closing connection", conn);
				break;
			case "TAKE":
			case "PASS":
				server.getGameThread().setResponse(user, command);
				break;
			default: 
				server.sendMessage("Message successfully received, but no action taken", conn);
				break;
		}
		
		if(!message.contains("/close")){
			
		}
	}
	
	private void cmdCommand() {
		
		
	}

	private void cmdStart(User user) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		if(user.isStart()){
			server.sendMessage("You have now opted out of the blackjack game.", conn);
			user.setStart(false);
		}
		else{
			server.sendMessage("You have set your status to ready.", conn);
			user.setStart(true);
			
			if(!server.checkAllReady())
				server.sendMessage("Waiting for other players to ready.", conn);
			else{
				server.sendMessage("Game is starting.", conn);
				server.broadcast(user, "Game is starting.");
				server.startGame();
			}
			
		}
	}
	
	//Returns all users currently online
	private void cmdOnline() throws IOException {
		String usersOnline = "Users online: ";
		for(User user : server.getClientMap().keySet())
			usersOnline += user.getUsername() + ", ";
		
		//Get rid of last comma
		usersOnline = usersOnline.substring(0, usersOnline.length()-2);
		server.sendMessage(usersOnline, conn);
		
	}
	
	//Relays a message to a user
	private void cmdMessage(String recipient, String msg) throws IOException {
		
		boolean userFound = false;
		for(User user : server.getClientMap().keySet())
			if(user.equals(recipient)){
				server.sendMessage(msg, server.getClientMap().get(user));
				userFound = true;
				break;
			}
		
		if(!userFound)
			server.sendMessage("User " + recipient + " was not found", conn);
		
	}
	
	//Lets the user identify themselves
	private User addNewUser() throws ClassNotFoundException, IOException {
		
		String username = conn.getOis().readObject().toString();
		User user = new User(username);
		
		while(!server.addUser(user, conn)){
			 server.sendMessage("Username: " + username + " taken", conn);
			 username = conn.getOis().readObject().toString();
			 user = new User(username);
		}
		String message = "Welcome, " + username + " \n";
		message += "Type /command to view the list of commands \n";
		message += "Type /start to initiate a game of blackjack ";
		server.sendMessage(message, conn);
		
		return user;
	}

}
