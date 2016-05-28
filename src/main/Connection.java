package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connection {
	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	
	public Connection(Socket socket) throws IOException{
		this.socket = socket;
		ois = new ObjectInputStream(socket.getInputStream());
		oos = new ObjectOutputStream(socket.getOutputStream());
		
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectInputStream getOis() {
		return ois;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public void close() throws IOException {
		ois.close();
		oos.close();
		socket.close();
	}
	
	
}
