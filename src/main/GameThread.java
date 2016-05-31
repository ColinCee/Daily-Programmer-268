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
	private Deck deck;
	private List<User> users;
	private Map<User, String> blockingMap;

	public GameThread(Server server) {
		this.server = server;
		deck = new Deck();
		users = new ArrayList<User>();
		blockingMap = new HashMap<User, String>();
	}

	@Override
	public synchronized void run() {
		try {

			
			users.addAll(server.getClientMap().keySet());
			// Sort clients based on their /start command sent time
			Collections.sort(users);
			
			// Deal initial cards
			for (int i = 0; i < users.size(); i++) {
				Connection currentConn = server.getClientMap().get(users.get(i));
				Card card = deck.getNextCard();
				User user = users.get(i);
				user.addCardToHand(card);
				server.sendMessage("Dealer deals: " + card.getName() + " to " + user.getUsername(), currentConn);
				server.broadcast(user, "Dealer deals: " + card.getName() + " to " + user.getUsername());
				blockingMap.put(users.get(i), "");
			}
			while (!isGameOver(users)) {
				// Ask if users want to take or pass
				for (int i = 0; i < users.size(); i++) {
					// Only ask players who have not passed their turn
					if (!users.get(i).isPass()) {
						User user = users.get(i);
						Connection currentConn = server.getClientMap().get(user);
						server.sendMessage("Server asks " + user.getUsername() + " TAKE or PASS", currentConn);
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
			endGame();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void endGame() throws IOException {
		server.broadcast(null, "Game over!");
		for(User user : users)
			user.resetGame();
		users.clear();
		blockingMap.clear();
		deck.resetDeck();
	}

	private void parseCommands(String message, User user, Connection conn) throws IOException, ClassNotFoundException {

		switch (message) {
		case "TAKE":
			Card card = server.getDeck().getNextCard();
			user.addCardToHand(card);
			String msg = "Dealer deals: " + card.getName() + ", current hand is: " + user.getHandValue();
			if (user.getHandValue() > 21)
				msg += " BUST";
			server.sendMessage(msg, conn);
			String broadcast = user.getUsername() + " TAKES a " + card.getName() + ", current hand is : " + user.getHandValue();
			server.broadcast(user, broadcast);
			break;
		case "PASS": // pass
			user.setPass(true);
			server.sendMessage("Pass confirmed", conn);
			server.broadcast(user, user.getUsername() + " has passed");
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

	public synchronized void setResponse(User user, String response) {
		blockingMap.put(user, response);
		notifyAll();
	}

}
