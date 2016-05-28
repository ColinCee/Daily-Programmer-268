package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import blackjack.Card;
import blackjack.Deck;

public class GameThread implements Runnable {

	private Server server;
	private Map<User, String> blockingMap;

	public GameThread(Server server) {
		this.server = server;
		blockingMap = new HashMap<User, String>();
	}

	@Override
	public synchronized void run() {
		try {

			List<User> users = new ArrayList<User>();
			users.addAll(server.getClientMap().keySet());
			// Sort clients based on their /start command sent time
			Collections.sort(users);

			Deck deck = new Deck();
			// Deal initial cards
			for (int i = 0; i < users.size(); i++) {
				Connection currentConn = server.getClientMap().get(users.get(i));
				Card card = deck.getNextCard();
				users.get(i).addCardToHand(card);
				sendMessage("Dealer deals: " + card.getName(), currentConn);
				blockingMap.put(users.get(i), "");
			}
			while (!isGameOver(users)) {
				// Ask if users want to take or pass
				for (int i = 0; i < users.size(); i++) {
					// Only ask players who have not passed their turn
					if (!users.get(i).isPass()) {
						User user = users.get(i);
						Connection currentConn = server.getClientMap().get(user);
						sendMessage("Server asks " + user.getUsername() + " TAKE or PASS", currentConn);
						//block thread execution until user input
						while (blockingMap.get(user).isEmpty()) {
							try {
								wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						parseCommands(blockingMap.get(user), user, currentConn);
					}
				}

			}
			cmdBroadcast("Game over!");
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void parseCommands(String message, User user, Connection conn) throws IOException, ClassNotFoundException {

		switch (message) {
		case "TAKE":
			Card card = server.getDeck().getNextCard();
			user.addCardToHand(card);
			if (user.getHandValue() <= 21)
				sendMessage("Dealer deals: " + card.getName() + ", current value is: " + user.getHandValue(), conn);
			else
				sendMessage("Dealer deals: " + card.getName() + ", current value is: " + user.getHandValue() + " BUST",
						conn);
			break;
		case "PASS": // pass
			user.setPass(true);
			sendMessage("Pass confirmed", conn);
			break;
		}

		// Update the map when finished
		blockingMap.put(user, "");
	}

	// Checks to see if any players still wish to take more cards
	private boolean isGameOver(List<User> users) {

		boolean gameOver = true;

		for (User user : users) {
			if (!user.isPass()) {
				gameOver = false;
				break;
			}
		}

		return gameOver;
	}

	// Send the broadcasted message to each user
	private void cmdBroadcast(String broadcastMsg) throws IOException {
		System.out.println(server.getTimeStamp() + " Broadcast requested");

		for (User user : server.getClientMap().keySet())
			sendMessage(broadcastMsg, server.getClientMap().get(user));

	}

	private void sendMessage(String message, Connection conn) throws IOException {
		conn.getOos().writeObject(message);
	}

	public synchronized void setResponse(User user, String response) {
		blockingMap.put(user, response);
		notifyAll();
	}

}
