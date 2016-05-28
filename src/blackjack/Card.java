package blackjack;

public class Card{

	private String name;
	private String suit;
	private String rank;
	private int value;
	
	public Card(String suit, String rank, int value){
		
		this.suit = suit;
		this.rank = rank;
		this.value = value;
		name = rank + " of " + suit;
	}

	public String getName() {
		return name;
	}

	public String getSuit() {
		return suit;
	}

	public String getRank() {
		return rank;
	}

	public int getValue() {
		return value;
	}
}