package main;

import java.io.IOException;
import java.util.StringTokenizer;

public class ClientThread implements Runnable {
	
	private Client client;
	
	public ClientThread(Client client){
		this.client = client;
	}
	
	@Override
	public void run() {

		try {
			while(!Thread.currentThread().isInterrupted()){
				String response = client.getServerResponse();
				long receiveTime = System.currentTimeMillis();
				if(response.contains("Closing"))
					client.closeConnection();
				else if(response.contains("ping")){
					StringTokenizer st = new StringTokenizer(response);
					st.nextToken();
					long sendTime = Long.parseLong(st.nextToken());
					System.out.println("Response time: " + (receiveTime - sendTime) + " ms");
				}
				else
					System.out.println(response);
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
