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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private Map<String, Connection> clientMap;

    private SimpleDateFormat sdf;
	public Server() throws IOException {

		clientMap = new HashMap<String, Connection>();
		
		sdf = new SimpleDateFormat("'['dd.MM HH:mm:ss']'");

	}

	public synchronized Map<String, Connection> getClientMap() {
		return clientMap;
	}

	public synchronized boolean addUser(String username, Connection conn) throws IOException, ClassNotFoundException {

		if (!clientMap.containsKey(username)) {
			System.out.println(getTimeStamp() + " User: " + username + " added");
			clientMap.put(username, conn);
			return true;
		} else
			return false;

	}

	public synchronized void deleteUser(String username) {
		System.out.println(getTimeStamp() + " User: " + username + " deleted");
		clientMap.remove(username);
	}
	
	public String getTimeStamp(){
		Date date = new Date();
		return(sdf.format(date));
	}
	
	public static void main(String[] args) {
		int port = 10;
		ServerSocket serverSock = null;
		// Create new fixed thread pool
		ExecutorService executor = Executors.newFixedThreadPool(16);

		try {
			Server server = new Server();
			// Create new server socket at port 10
			serverSock = new ServerSocket(port);
			System.out.println(server.getTimeStamp() + " Waiting for Client on Port: " + serverSock.getLocalPort());
			// While true loop allows the class to accept any clients on port 10
			while (true) {
				Socket cSock = serverSock.accept();
				executor.execute(new ServerThread(cSock, server));
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		} finally {
			try {
				serverSock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}