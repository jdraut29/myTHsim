package texasSim;

public class Player {

	private Card card1;
	private Card card2;
	private Double bankRoll;
	private Double currentBet;
	private Boolean folded;
	
	
	public Card getCard1() {
		return card1;
	}
	public Card getCard2() {
		return card2;
	}
	public Double getBankRoll() {
		return bankRoll;
	}
	public Double getBet() {
		return currentBet;
	}
	public void resetBet() {
		currentBet = 0.;
	}
	public void foldHand() {
		folded = true;
	}
	public void clearHand() {
		folded = false;
	}
	public Boolean hasFolded() {
		return folded;
	}
	public void setCard1(Card c) {
		card1 = new Card(c);
	}
	public void setCard2(Card c) {
		card2 = new Card(c);
	}
	public void addPotToBankRoll(Double pot) {
		bankRoll = bankRoll + pot;
	}
	public void placeBet(Double bet) {
		currentBet = currentBet + bet;
		bankRoll = bankRoll - bet;
	}
	public void setBankRoll(Double b) {
		bankRoll = b;
	}
	public Player(){
		card1 = new Card();
		card2 = new Card();
		bankRoll = 12000.;
		currentBet = 0.;
		folded = false;
	}
}
