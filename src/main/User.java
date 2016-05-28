package main;

import java.util.ArrayList;
import java.util.List;

import blackjack.Card;

public class User implements Comparable<User> {
	
	private String username;
	private boolean start;
	private long startTime;
	private List<Card> currentHand;
	private boolean pass;
	private List<String> commandHistory;
	
	public User(String username){
		this.username = username;
		currentHand = new ArrayList<Card>();
		commandHistory = new ArrayList<String>();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
		if(start)
			setStartTime(System.currentTimeMillis());
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public List<Card> getCurrentHand() {
		return currentHand;
	}

	public void addCardToHand(Card card) {
		currentHand.add(card);
	}
	
	public boolean isPass() {
		return pass;
	}

	public void setPass(boolean pass) {
		this.pass = pass;
	}

	public int getHandValue(){
		int value = 0;
		boolean ace = false;
		for(Card card : currentHand){
			value += card.getValue();
			if(card.getRank().equals("A"))
				ace = true;
		}
		//Change value of ace to 1 when total value > 21
		if(value > 21 && ace)
			value -= 10;
		//When bust, force pass next move
		else if(value > 21)
			pass = true;
		
		return value;
		
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		
		return true;
	}

	@Override
	public int compareTo(User user) {
		if(this.getStartTime() < user.getStartTime())
			return -1;
		else
			return 1;
	}
}
