/** 
 * This server class creates a server socket on port 10
 * and waits for a client to connect. Once connected it
 * retrieves sends and receives messages to and from the
 * client until an exit command is received.
 * 
 * Note that this implementation uses a fixed thread
 * pool.
 * 
 * @author Colin Cheung
 */
package main;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread implements Runnable {
	
	private Map<String, Socket> clientMap;

	public Server() throws IOException{
		
		clientMap = new HashMap<String, Socket>();
	}

	
	public synchronized Map<String, Socket> getClientMap() {
		return clientMap;
	}


	public synchronized boolean addUser(String username, Socket socket) {
		
		if(!clientMap.containsKey(username)){
			System.out.println("User: " + username + " added");
			clientMap.put(username, socket);
			return true;
		}
		else
			return false;
		
	}
	public synchronized void deleteUser(String username) {
		clientMap.remove(username);
	}
	

	public static void main(String[] args) throws IOException{
		Server server = new Server();
		int port = 10;
		
		ServerSocket serverSock = null;
		//Create new fixed thread pool
		ExecutorService executor = Executors.newFixedThreadPool(16);
		try {
			//Create new server socket at port 10
			serverSock = new ServerSocket(port);
			System.out.println("Waiting for Client on Port: " + serverSock.getLocalPort());
			//While true loop allows the class to accept any clients on port 10
			while(true){
				
				//Accept the client and then use executor to assign a new thread to perform...
				//the server response.
				Socket cSock = serverSock.accept();
				executor.execute(new ServerThread(cSock, server));
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	}
}