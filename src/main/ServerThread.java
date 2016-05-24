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
				parseCommands(message);
				message =  conn.getOis().readObject().toString();
			}	
			
			server.deleteUser(username);
			conn.close();
			
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
				System.out.println(server.getTimeStamp() + " Broadcast requested");
				String body = "Server wide brodcast: ";
				body += message.substring(command.length());
				for(String user : server.getClientMap().keySet())
					sendMessage(body, server.getClientMap().get(user));
				
				break;
			case "/online":
				String usersOnline = "Users online: ";
				for(String user : server.getClientMap().keySet())
					usersOnline += user + ", ";
				
				//Get rid of last comma
				usersOnline = usersOnline.substring(0, usersOnline.length()-2);
				sendMessage(usersOnline, conn);
				break;
			case "/ping":
				sendMessage("ping response", conn);
				break;
			case "/close":
				sendMessage("Closing connection", conn);
				break;
			default: 
				sendMessage("Message successfully received, but no action taken", conn);
				break;
		}
	}
	
	private String getUsername() throws ClassNotFoundException, IOException {
		String username = conn.getOis().readObject().toString();
		
		while(!server.addUser(username, conn)){
			 sendMessage("Username: " + username + " taken", conn);
			 username = conn.getOis().readObject().toString();
		}
		
		sendMessage("Welcome, " + username + " \n" + "Commands are: /broadcast, /online, /close, /ping", conn);
		
		return username;
	}
	
	private void sendMessage(String message, Connection conn) throws IOException{		
		conn.getOos().writeObject(message);
	}

}
