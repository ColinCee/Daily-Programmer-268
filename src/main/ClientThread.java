package main;

import java.io.IOException;

public class ClientThread implements Runnable {
	
	private Client client;
	
	public ClientThread(Client client){
		this.client = client;
	}
	
	@Override
	public void run() {

		try {
			while(true){
				String response = client.getServerResponse();
				if(response.contains("Closing")){
					client.closeConnection();
					break;
				}
				System.out.println(response);
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}