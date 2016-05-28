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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import blackjack.Deck;

public class Server {

	private Map<User, Connection> clientMap;
    private SimpleDateFormat sdf;
    private Deck deck;
    private GameThread gameThread;
    
	public Server() throws IOException {

		clientMap = new HashMap<User, Connection>();
		sdf = new SimpleDateFormat("'['dd.MM HH:mm:ss']'");
		deck = new Deck();
		gameThread = new GameThread(this);
		
	}

	public synchronized Map<User, Connection> getClientMap() {
		return clientMap;
	}

	public Deck getDeck() {
		return deck;
	}

	public GameThread getGameThread() {
		return gameThread;
	}

	public synchronized boolean addUser(User user, Connection conn) throws IOException, ClassNotFoundException {
		
		if (!clientMap.containsKey(user)) {
			System.out.println(getTimeStamp() + " User: " + user.getUsername() + " connected");
			clientMap.put(user, conn);
			return true;
		} else
			return false;

	}

	public synchronized void deleteUser(User user) {
		System.out.println(getTimeStamp() + " User: " + user.getUsername() + " disconnected");
		clientMap.remove(user);
	}
	
	public synchronized boolean checkAllReady(){
		boolean ready = true;
		int count = 0;
		for(User user : clientMap.keySet()){
			if(!user.isStart()){
				ready = false;
				break;
			}
			count++;
		}
		
		if(count > 1)
			return ready;
		else 
			return false;
		
	}

	public void startGame() {
		
		Thread thread = new Thread(gameThread);
		thread.start();
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
				Connection conn = new Connection(cSock);
				executor.execute(new ServerThread(conn, server));
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