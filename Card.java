package texasSim;

public class Card {
	private Integer suit;
	private Integer rank;
	
	public Integer getSuit() {
		return suit;
	}
	public Integer getRank() {
		return rank;
	}
	public Boolean setSuit(Integer s) {
		if (s > 0 && s < 5) {
			suit = s;
			return true;
		}
		return false;
	}
	public Boolean setRank(Integer r) {
		if (r > 0 && r < 14) {
			rank = r;
			return true;
		}
		return false;
	}
	public void printCard() {
		String suitStr = new String();
		switch(this.getSuit()) {
			case 1: suitStr = "clubs";
				break;
			case 2: suitStr = "diamonds";
				break;
			case 3: suitStr = "hearts";
				break;
			case 4: suitStr = "spades";
				break;
			default: suitStr = "invalid suit";
				break;
		}
		System.out.println(this.getRank() + " " + suitStr);
	}
	public Card(Card c) {
		super();
		this.suit = c.getSuit();
		this.rank = c.getRank();
	}
	// generic initialization: create a card with no suit or rank for future use.
	public Card() {
		super();
		this.suit = 0;
		this.rank = 0;
	}
	
}
