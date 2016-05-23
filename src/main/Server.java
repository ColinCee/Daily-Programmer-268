/** 
 * This server class creates a server socket on port 6100
 * and waits for a client to connect. Once connected it
 * retrieves the message from the socket input stream
 * and uses the MessageImpl class to get the count of
 * characters and digits. Then finally it sends the
 * MessageImpl class back using the socket's output
 * stream.
 * 
 * Note that this implementation uses a cached thread
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
		//Create new cached thread pool
		ExecutorService executor = Executors.newFixedThreadPool(16);
		try {
			//Create new server socket at port 6100
			serverSock = new ServerSocket(port);
			System.out.println("Waiting for Client on Port: " + serverSock.getLocalPort());
			//While true loop allows the class to accept any clients on port 6100
			while(true){
				
				//Accept the client and then use executor to assign a new thread to perform...
				//the server response.
				Socket cSock = serverSock.accept();
				executor.execute(new ClientThread(cSock, server));
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	}
}