package blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck {
	
	private Stack<Card> activeDeck = new Stack<Card>();
	
	public Deck(){
		
		initialiseDeck();
	}

	private void initialiseDeck() {
		List<Card> newDeck = new ArrayList<Card>();
		String[] ranks = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
		String[] suits = {"Diamonds", "Clubs", "Hearts", "Spades"};
		int[] values = {2,3,4,5,6,7,8,9,10,10,10,10,11};
		
		assert(ranks.length == values.length) : "Mismatch array lengths";
		
		for(int i=0; i<suits.length; i++){
			for(int j=0;j<ranks.length;j++){
				Card card = new Card(suits[i], ranks[j], values[j]);
				newDeck.add(card);
			}
		}
		
		Collections.shuffle(newDeck);
		activeDeck.addAll(newDeck);
	}
	
	public Card getNextCard(){
		return activeDeck.pop();
	}
	
	public void resetDeck(){
		initialiseDeck();
	}
}
