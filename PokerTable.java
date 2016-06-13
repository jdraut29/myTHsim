package texasSim;

import java.util.Arrays;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class PokerTable {

	private Player[] tablePlayers;
	private Card[] tableCards;
	private Double[] preFlopCallBBOdds = {0.07, 0.08, 0.08, 0.09, 0.09, 0.091, 0.091, 0.10, 0.12, 0.137};
	private Double[] preFlopCallARaiseOdds = {0.09, 0.09, 0.10, 0.10, 0.10, 0.10, 0.10, 0.11, 0.13, 0.14};
	private Double[] preFlopCallMultiRaiseOdds = {0.10, 0.11, 0.12, 0.12, 0.12, 0.12, 0.12, 0.12, 0.137, 0.15};
	private List<Integer> tight = new ArrayList<Integer>();
	private Double[] pocketHands;
	private Double[] winningPocketHands;
	private Double[] winPercent;
	private Double pot;
	//private Double rake = 0.1;  // use for percentage rake instead of dollar rake
	private Double totalPots = 0.0;
	private Double currBet = 3.0;
	private Integer button;
	private Integer lastRaise;
	private Integer raiseCount = 0;
	public Integer flopsPlayed = 0;
	public Integer flopsSeen = 0;
	public Integer flopPlayers = 0;
	public Integer turnPlayers = 0;
	public Integer riverPlayers = 0;
	public Integer playersLeft = 0;
	public Integer flopBets = 0;
	public Integer turnBets = 0;
	public Integer turnsSeen = 0;
	public Integer riverBets = 0;
	public Integer riversSeen = 0;
	public Integer showdowns = 0;
	public Double minBankRoll = 12000.;
	public Integer handsWon = 0;
	private Card[] deck;
	
	public PokerTable(){
		super();
		this.tablePlayers = new Player[10];
		this.tableCards = new Card[5];
		this.button = 0;
		this.deck = new Card[52];
	}
	public void setUp() throws FileNotFoundException {
		for (int i = 0; i < 52; i++) {
			deck[i] = new Card();
		}
		for (int i = 0; i < 5; i++) {
			tableCards[i] = new Card();
		}
		for (int i = 0; i < 10; i++) {
			tablePlayers[i] = new Player();
			if (preFlopCallBBOdds[i] >= 0.091) tight.add(i);
		}
		pocketHands = new Double[169];
		winningPocketHands = new Double[169];
		winPercent = new Double[169];
		for (int i = 0; i < pocketHands.length; i++) {
			pocketHands[i] = 0.0;
			winningPocketHands[i] = 0.0;
			winPercent[i] = 0.0;
		}
        Scanner scanner = new Scanner(new File("L:/Jason/workspace/HoldEmSimulator/src/texasSim/winpercent.csv"));
        while(scanner.hasNext()){
//            System.out.print(scanner.next()+"|");
            String nextLine = scanner.next();
            String[] vals = nextLine.split(",");
            Integer index = Integer.parseInt(vals[0]); 
            Double pct = Double.parseDouble(vals[1]);
            winPercent[index] = pct;
        }
        scanner.close();
		button = 0;
		pot = 0.;
		
		
	}
	public Integer getNumPlayers(){
		return tablePlayers.length;
	}
	public Double getTotalPots(){
		return totalPots;
	}
	public Double getPlayerBankRoll(int index){
		return tablePlayers[index].getBankRoll();
	}
	public Integer getRaiseCount(){
		return raiseCount;
	}
	public Double getPocketHands(int index){
		return pocketHands[index];
	}
	public Double getWinningPocketHands(int index){
		return winningPocketHands[index];
	}
	public void resetPlayerBets() {
		for (int i = 0; i < tablePlayers.length; i++) {
			tablePlayers[i].resetBet();
		}
	}
	public void betOrRaise(int i, Double maxBet, Double betSize) {  //call above maxBet
		if (currBet == 0 || (tablePlayers[i].getBet() < currBet && currBet < maxBet)) {
			currBet = currBet + betSize;
			lastRaise = i;
			pot += currBet - tablePlayers[i].getBet();
			tablePlayers[i].placeBet(currBet - tablePlayers[i].getBet());
		}
		else {
			pot += currBet - tablePlayers[i].getBet();
			tablePlayers[i].placeBet(currBet - tablePlayers[i].getBet());
		}
	}
	public void callBetOrCheck(int i, Double maxCall) {
		if (currBet <= maxCall) {
			pot += currBet - tablePlayers[i].getBet();
			tablePlayers[i].placeBet(currBet - tablePlayers[i].getBet());
		}
		else tablePlayers[i].foldHand();
	}
	public void checkOrFold(int i) {
		if (currBet > tablePlayers[i].getBet()) {
			tablePlayers[i].foldHand();
		}
	}
	public void printTableStatus() {
//		System.out.println("pot: " + pot);
		Integer playersLeft = 0;
		for (int i = 0; i < tablePlayers.length; i++) {
			if (!tablePlayers[i].hasFolded()) playersLeft++;
		}
//		System.out.println("current bet: " + currBet);
//		System.out.println("players left: " + playersLeft);
	}
	public void shuffle() {
		Double[] draw = new Double[52];
		Double[] ordered = new Double[52];
		for (int i = 0; i < 52; i++) {
			draw[i] = Math.random();
		}
		ordered = draw.clone();
		Arrays.sort(ordered);
		for (int i = 0; i < 52; i++) {
			for (int j = 0; j < 52; j++) {
				if (draw[i] == ordered[j]) {
					deck[i].setRank(j % 13 + 1);
					deck[i].setSuit((int)(j/13) + 1);
					break;
				}
			}
		}
		button = (button + 1) % 10;
		pot = 0.;
		for (int i = 0; i < tablePlayers.length; i++) {
			tablePlayers[i].clearHand();
		}
	}
	public Integer suitedCards(Card[] hand, Integer minNum){
		//returns the suit index of the suit that has more than minNum cards, if no suit is greater, returns zero
		//minNum must be greater than half the cards in the hand for this to work properly
		Integer[] suits = {0,0,0,0};
		Integer retVal = 1;
		for (int i = 0; i < hand.length; i++) {
			suits[hand[i].getSuit()-1]++;
		}
		Integer max = suits[0];
		for (int i = 1; i < 4; i++){
			max = Math.max(max, suits[i]);
			if (max == suits[i]) {
				retVal = i + 1;
			}
		}
		if (max >= minNum) {
			return retVal;
		}
		return 0;
	}
	public List<Integer> multiSameRank(Card[] hand, Integer minNum) {
		//returns rank values of all cards that have at least minNum occurrences of a rank
		Integer[] cardRanks = new Integer[hand.length];
		List<Integer> retVal = new ArrayList<Integer>();
		List<Integer> cardsToCompare = new ArrayList<Integer>();
		for (int i = 0; i < hand.length; i++) {
			if (hand[i].getRank() == 1) {
				cardRanks[i] = 14;
			}
			else {
				cardRanks[i] = hand[i].getRank();
			}
		}
		Arrays.sort(cardRanks);
		for (int i = 0; i <= cardRanks.length-minNum; i++) {
			cardsToCompare = Arrays.asList(Arrays.copyOfRange(cardRanks, i, i + minNum));
			if (Collections.max(cardsToCompare) == Collections.min(cardsToCompare)) {
				if (!retVal.contains(cardRanks[i])) {
					retVal.add(cardRanks[i]);
				}
			}
		}
		if(retVal.contains(14)) {
			retVal.set(retVal.size()-1, 1);
		}
		return retVal;
	}
	public Integer hasFullHouse(Card[] currHand) {
		List<Integer> currMults = new ArrayList<Integer>();
		List<Integer> kickers = new ArrayList<Integer>();
	
		currMults = multiSameRank(currHand, 3);
		if(currMults.size() > 0) {
			for(int j = 0; j < currHand.length; j++) {
				if (currHand[j].getRank() != currMults.get(currMults.size()-1)) {
					kickers.add(currHand[j].getRank());						
				}
			}
			Integer[] ranks = new Integer[kickers.size()];
			kickers.toArray(ranks);
			Arrays.sort(ranks);
			for (int j = ranks.length - 1; j > 0; j--) {
				if(ranks[j-1] == ranks[j]) {
					return currMults.get(currMults.size()-1);
				}
			}
		}
		return 0;
	}
	public Integer hasStraight(Integer[] cardRanks, Integer minNum) {
		//returns the top rank of a straight of length minNum or longer.  Ace high straight returns 14.
		Integer retVal = 0;
		Integer straightLength = 1;
		Arrays.sort(cardRanks);

		int dups = 0;
		for (int i = 1; i < cardRanks.length; i++) {
			if(cardRanks[i-1] == cardRanks[i] && straightLength > 1) {
				dups++;
				continue;
			}
			if(cardRanks[i-straightLength-dups]==cardRanks[i] - straightLength) {
				retVal = cardRanks[i];
				straightLength++;
			}
			else if (!(straightLength >= minNum)) {
				retVal = 0;
				straightLength = 1;
				dups = 0;
			}
			if (retVal == 13 && cardRanks[0] == 1) {
				retVal++;
				straightLength++;
			}
		}
		if (straightLength >= minNum) {
			return retVal;			
		}
		else return 0;
	}
	public Boolean insideStraightDraw(Integer[] cardRanks, Integer minNum) {
		// checks for inside straight draw.
		// a straight of length minNum will not return true, there must be one gap card  
		Boolean retVal = false;
		Arrays.sort(cardRanks);
		Set<Integer> uniqCards = new TreeSet<Integer>();
		uniqCards.addAll(Arrays.asList(cardRanks));
		if (uniqCards.contains(1)) uniqCards.add(14);  // put an ace at top and bottom of rank order
		Integer[] uniqRanks = uniqCards.toArray(new Integer[uniqCards.size()]);
		for (int i = minNum-1; i < uniqRanks.length; i++) {
			if(uniqRanks[i-minNum+1] == uniqRanks[i] - minNum) {
				retVal = true;
				continue;
			}
		}
		return retVal;
	}
	public void updateStats(Player currHand, int handType, int numWinners) {
		// handType == 0 means update pocketCards
		// handType == 1 means update winningPocketCards
		Integer topRank;
		Integer btmRank;
		Double handBet = currHand.getBet();
		if (currHand.getCard1().getRank() == 1 || currHand.getCard2().getRank() == 1) {
			topRank = 14;
			btmRank = Math.max(currHand.getCard1().getRank(), currHand.getCard2().getRank());
			if (btmRank == 1) {
				btmRank = 14;
			}
		}
		else {
			if (currHand.getCard1().getRank() <= currHand.getCard2().getRank()) {
				topRank = currHand.getCard2().getRank();
				btmRank = currHand.getCard1().getRank();
			}
			else {
				topRank = currHand.getCard1().getRank();
				btmRank = currHand.getCard2().getRank();
			}
		}
		if (topRank == btmRank) { //pocket pair
			if (topRank == 14) {
				if (handType == 0) {
					if (currBet <= handBet) {
						pocketHands[168]++;
					}
				}
				else winningPocketHands[168] += 1./numWinners;
				//System.out.println("index: " + 168);
			}
			else {
				if (handType == 0) {
					if (currBet <= handBet) {
						pocketHands[154 + topRank]++;
					}
				}
				else winningPocketHands[154 + topRank] += 1./numWinners;
				//System.out.println("index: " + (154 + topRank));
			}
		}
		else {
			int index = -1;
			for (int j = 1; j < topRank-2; j++) {
				index += j;
			}
			index += btmRank-1;
			index *= 2;
			if (currHand.getCard1().getSuit() == currHand.getCard2().getSuit()) {
				index++;
			}
			if (handType == 0) {
				if (currBet <= handBet) {
					pocketHands[index]++;
				}
			}
			else winningPocketHands[index] += 1./numWinners;
			//System.out.println("index: " + index);
		}
		//currHand.getCard1().printCard();
		//currHand.getCard2().printCard();

	}
	public Double findMaxScore(Double[] vals) {
		Double max = 0.0;
		for (int i = 0; i < vals.length; i++) {
			if (vals[i] > max) {
				max = vals[i];
			}
		}
		return max;
	}
	
	//////////////////// DEAL STARTS HERE ////////////////////
	public void deal(){
		Integer topRank;
		Integer btmRank;
		Double[] currPercent = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		lastRaise = -1;
		currBet = 3.0;
		for (int i = 0; i<tablePlayers.length*2+5; i++) {
			if (i<tablePlayers.length) {
				tablePlayers[i].setCard1(deck[i]); 
			}
			else if (i<tablePlayers.length*2) {
				tablePlayers[i%tablePlayers.length].setCard2(deck[i]); 
			}
			else {
				tableCards[i-tablePlayers.length*2].setRank(deck[i].getRank());
				tableCards[i-tablePlayers.length*2].setSuit(deck[i].getSuit());
			}
		}
		//determine each hand's winning percentage and go through first round of betting
		for (int j = 0; j < tablePlayers.length; j++) {
			int i = (button + 3 + j) % 10;
			//if (j==0) System.out.println("button = " + button + "; first = " + i);
			if (tablePlayers[i].getCard1().getRank() == 1 || tablePlayers[i].getCard2().getRank() == 1) {
				topRank = 14;
				btmRank = Math.max(tablePlayers[i].getCard1().getRank(), tablePlayers[i].getCard2().getRank());
				if (btmRank == 1) {
					btmRank = 14;
				}
			}
			else {
				if (tablePlayers[i].getCard1().getRank() <= tablePlayers[i].getCard2().getRank()) {
					topRank = tablePlayers[i].getCard2().getRank();
					btmRank = tablePlayers[i].getCard1().getRank();
				}
				else {
					topRank = tablePlayers[i].getCard1().getRank();
					btmRank = tablePlayers[i].getCard2().getRank();
				}
			}
			if (topRank == btmRank) { //pocket pair
				if (topRank == 14) {
					currPercent[i] = winPercent[168];
				}
				else {
					currPercent[i] = winPercent[154 + topRank];
				}
			} 
			else {
				int index = -1;
				for (int k = 1; k < topRank-2; k++) {
					index += k;
				}
				index += btmRank-1;
				index *= 2;
				if (tablePlayers[i].getCard1().getSuit() == tablePlayers[i].getCard2().getSuit()) {
					index++;
				}
				currPercent[i] = winPercent[index];
			}
			
			//place first round of pre-flop bets
			if (currPercent[i] > 0.20) { 
				betOrRaise(i,12.,3.);
			}			
			else if ((currPercent[i] > 0.166 && currBet <= 3.0) ) { 
				betOrRaise(i,6.,3.);
			}
			else if (currPercent[i] > preFlopCallBBOdds[i] && currBet <= 3.0) {
				callBetOrCheck(i,3.);
			}
			else if (currPercent[i] > preFlopCallARaiseOdds[i] && currBet <= 6.0) {
				callBetOrCheck(i,6.);
			}
			else if (currPercent[i] > preFlopCallMultiRaiseOdds[i] && currBet > 6.0) {
				callBetOrCheck(i,12.);
			}
			else if (i == ((button + 1) % 10)) {
				tablePlayers[i].placeBet(1.0);
				pot += 1.0;
				tablePlayers[i].foldHand();
			}
			else if (i == ((button + 2) % 10)) {
				tablePlayers[i].placeBet(3.0);
				pot += 3.0;
				if (currBet > 3.0) {
					if (currPercent[i] > preFlopCallBBOdds[i] && currBet <= 6.0) {
						callBetOrCheck(i,6.);
					}
					else tablePlayers[i].foldHand();
				}
			}
			else if (tablePlayers[i].getBet() < currBet) {
				tablePlayers[i].foldHand();
			}
		}
		if (lastRaise > -1) {
			raiseCount++;
		}
		// call raises and allow for re-raises to cap
		while (lastRaise > -1) {
			lastRaise = -1;
			for (int j = 0; j < tablePlayers.length; j++) {
				int i = (button + 3 + j) % 10;
				if (!tablePlayers[i].hasFolded()) {
					if (currPercent[i] > 0.20) {
						betOrRaise(i,12.,3.);
					}
					if (tablePlayers[i].getBet() >= currBet) {
						continue;  //check
					}
					else if (tablePlayers[i].getBet() == currBet - 3.0 && currPercent[i] > preFlopCallBBOdds[i]) {
						//call one raise if player would have called BB
						callBetOrCheck(i, currBet);
					}
					else if (tablePlayers[i].getBet() < currBet - 3.0 && currPercent[i] > preFlopCallMultiRaiseOdds[i]) {
						//call the multiRaise if would have called multiple raises on first action
						callBetOrCheck(i,12.);
					}
					else tablePlayers[i].foldHand();
				}
			}
//			printTableStatus();
		}
//		for (int i = 0; i < tablePlayers.length; i++) {
//			if (!tablePlayers[9].hasFolded()) flopsPlayed++;
//		}
		//Bet the flop
		resetPlayerBets();
		for (int i = 0; i < tablePlayers.length; i++) {
			if (!tablePlayers[i].hasFolded()) {
				playersLeft++;
				flopPlayers++;
			}
		}
		if (playersLeft > 1) flopsSeen++;
		playersLeft = 0;
		betFlop();
		//Bet the turn
		resetPlayerBets();
		for (int i = 0; i < tablePlayers.length; i++) {
			if (!tablePlayers[i].hasFolded()) {
				playersLeft++;
				turnPlayers++;
			}
		}
		if (playersLeft > 1) turnsSeen++;
		playersLeft = 0;
		betTurn();
		
		//Bet the river
		resetPlayerBets();
		for (int i = 0; i < tablePlayers.length; i++) {
			if (!tablePlayers[i].hasFolded()) {
				riverPlayers++;
				playersLeft++;
			}
		}
		if (playersLeft > 1) riversSeen++;
		playersLeft = 0;
		betRiver();
		for (int i = 0; i < tablePlayers.length; i++) {
			if (!tablePlayers[i].hasFolded()) {
				playersLeft++;
			}
		}
		if (playersLeft > 1) showdowns++;
		playersLeft = 0;
		
	}
	public void betFlop(){
		currBet = 0.;
		lastRaise = 0;
		if (pot > 5) pot -= 5;  //rake
		while (lastRaise > -1){
			lastRaise = -1;
			for (int j = 0; j < tablePlayers.length; j++) {
				int i = (button + 3 + j) % 10;
				if (!tablePlayers[i].hasFolded()) {
					Card[] currHand = new Card[5];
					Card[] flop = new Card[3];
					
					currHand[0] = tablePlayers[i].getCard1();
					currHand[1] = tablePlayers[i].getCard2();
					currHand[2] = tableCards[0];
					currHand[3] = tableCards[1];
					currHand[4] = tableCards[2];
					flop[0] = tableCards[0];
					flop[1] = tableCards[1];
					flop[2] = tableCards[2];
					Integer floppedFlush = suitedCards(currHand, 5);
					Integer[] ranks = new Integer[currHand.length];
					for (int k = 0; k < currHand.length; k++) {
						ranks[k] = currHand[k].getRank();
					}
					Integer[] flopRanks = new Integer[flop.length];
					for (int k = 0; k < flop.length; k++) {
						flopRanks[k] = flop[k].getRank();
					}
					Arrays.sort(flopRanks);
					Integer floppedStraight = hasStraight(ranks, 5); 
					if (floppedFlush > 0 || floppedStraight > 0) {
						betOrRaise(i,12.,3.);
						continue;
					}
					Integer fourCardFlush = suitedCards(currHand, 4);
					Boolean insideStraight = insideStraightDraw(ranks,4);
					Integer straightDraw = hasStraight(ranks, 4); 
					Integer fullHouse = hasFullHouse(currHand);
					List<Integer> hasQuads = multiSameRank(currHand, 4);
					if (fourCardFlush > 0 || straightDraw > 0 || fullHouse > 0 || hasQuads.size() > 0) {
						callBetOrCheck(i,12.);
						continue;
					}
					if (insideStraight && (!tight.contains(i) || pot > 7 * currBet)) {
						callBetOrCheck(i,12.);
						continue;
					}
					List<Integer> hasTrips = multiSameRank(currHand, 3);
					if (hasTrips.size() > 0) {
						betOrRaise(i,12.,3.);	
						continue;
					}
					List<Integer> boardPairs = multiSameRank(flop, 2);
					List<Integer> hasPairs = multiSameRank(currHand, 2);
					if (hasPairs.size() > 1 && boardPairs.size() == 0) {
						betOrRaise(i,9.,3.);  //re-raise once with two pair 
						continue;
					}
					// one pair
					else if (hasPairs.size() > 0) {
						if (hasPairs.contains(1) && (tablePlayers[i].getCard1().getRank() == 1 || tablePlayers[i].getCard2().getRank() == 1)) {
							// bet once with pair of aces including a pocket card, otherwise call
							if (tablePlayers[i].getBet() == 0 || tablePlayers[i].getBet() < currBet) {
								if (currBet < 3.) {
									betOrRaise(i,3.,3.);
								}
								else callBetOrCheck(i,12.);
							}
							continue;					
						} // top pair including a pocket card, bet once or call 
						else if ((hasPairs.get(0) == currHand[0].getRank() || hasPairs.get(0) == currHand[1].getRank()) ){
							if (hasPairs.get(0) >= flopRanks[2]) {
								// bet once with top pair or over pair, otherwise call
								if (tablePlayers[i].getBet() == 0 || tablePlayers[i].getBet() < currBet) {
									if (currBet < 3.) {
										betOrRaise(i,3.,3.);
									}
									else callBetOrCheck(i,9.);
								}
								continue;
							}
							else if (!tight.contains(i)) {  //looser players call with a pair
								if (tablePlayers[i].getBet() < currBet) {
									callBetOrCheck(i,6.);
								}
								continue;
							}
						}
					}
					if (boardPairs.size() == 0 && currHand[0].getRank() > flopRanks[2] && currHand[1].getRank() > flopRanks[2] 
							&& currBet <= tablePlayers[i].getBet() + 3) {
						// call one bet with two overcards			
						if (tablePlayers[i].getBet() < currBet) {
							callBetOrCheck(i, currBet);
						}
						continue;
					}
					Integer threeCardFlush = suitedCards(tableCards, 3);
					if (!tight.contains(i) && threeCardFlush > 0 && (currHand[0].getRank() > flopRanks[2] || currHand[1].getRank() > flopRanks[2] 
							&& currBet <= tablePlayers[i].getBet() + 3)) {
						// loose players call one bet with three cards to a flush and one overcard
						if (tablePlayers[i].getBet() < currBet) {
							callBetOrCheck(i, currBet);
						}
						continue;
					}
					if (tablePlayers[i].getBet() >= currBet) {
						continue;  //check
					}
					else tablePlayers[i].foldHand();
				}
			}
		}
		if (currBet > 0.0) flopBets++;
	}
	public void betTurn(){
		currBet = 0.;
		lastRaise = 0;
		while (lastRaise > -1){
			lastRaise = -1;
			for (int j = 0; j < tablePlayers.length; j++) {
				int i = (button + 3 + j) % 10;
				if (!tablePlayers[i].hasFolded()) {
					Card[] currHand = new Card[6];
					Card[] turnCards = new Card[4];
					
					currHand[0] = tablePlayers[i].getCard1();
					currHand[1] = tablePlayers[i].getCard2();
					currHand[2] = tableCards[0];
					currHand[3] = tableCards[1];
					currHand[4] = tableCards[2];
					currHand[5] = tableCards[3];
					turnCards[0] = tableCards[0];
					turnCards[1] = tableCards[1];
					turnCards[2] = tableCards[2];
					turnCards[3] = tableCards[3];
					Integer turnedFlush = suitedCards(currHand, 5);
					Integer[] ranks = new Integer[currHand.length];
					for (int k = 0; k < currHand.length; k++) {
						ranks[k] = currHand[k].getRank();
					}
					Integer[] turnRanks = new Integer[turnCards.length];
					for (int k = 0; k < turnCards.length; k++) {
						turnRanks[k] = turnCards[k].getRank();
					}
					Arrays.sort(turnRanks);
					Integer turnedStraight = hasStraight(ranks, 5); 
					List<Integer> boardPairs = multiSameRank(turnCards, 2);
					
					if ((turnedFlush > 0 || turnedStraight > 0) && boardPairs.size() == 0 && !tight.contains(i)) {
						betOrRaise(i,24.,6.);
						continue;
					}
					if ((turnedFlush > 0 || (turnedStraight > 0 && suitedCards(turnCards,3) == 0)) && boardPairs.size() == 0) {
						betOrRaise(i,24.,6.);
						continue;
					}
					
					Integer fourCardFlush = suitedCards(currHand, 4);
					Boolean insideStraight = insideStraightDraw(ranks,4);
					Integer straightDraw = hasStraight(ranks, 4); 
					Integer fullHouse = hasFullHouse(currHand);
					List<Integer> hasQuads = multiSameRank(currHand, 4);
					
					if (fullHouse > 0 || hasQuads.size() > 0) {
						betOrRaise(i,24.,6.);
						continue;
					}
					if (fourCardFlush > 0 || straightDraw > 0) {
						if (!tight.contains(i)) {
							callBetOrCheck(i,24.);
							continue;
						}
						else if (8 * (currBet - tablePlayers[i].getBet()) < pot) {
							callBetOrCheck(i,24.);
							continue;
						}
					}
					if (insideStraight && !tight.contains(i)) {
						callBetOrCheck(i,24.);
						continue;
					}
					List<Integer> hasTrips = multiSameRank(currHand, 3);
					if (hasTrips.size() > 0 && boardPairs.size() == 0 && suitedCards(turnCards,3) == 0) {
						betOrRaise(i,18.,6.);
						continue;
					}
					if (hasTrips.size() > 0) {
						betOrRaise(i,6.,6.);
						continue;					
					}
					List<Integer> hasPairs = multiSameRank(currHand, 2);
					if (hasPairs.size() > 1 && boardPairs.size() == 0) {
						betOrRaise(i,18.,6.); //re-raise once with two pair
						continue;
					}
					// one pair
					else if (hasPairs.size() > 0) {
						if (hasPairs.contains(1) && (tablePlayers[i].getCard1().getRank() == 1 || tablePlayers[i].getCard2().getRank() == 1)) {
							// bet once with pair of aces including a pocket card, otherwise call
							if (tablePlayers[i].getBet() < currBet) {
								if (currBet < 6.) {
									betOrRaise(i,6.,6.);
								}
								else callBetOrCheck(i,18.);
							}
							continue;					
						} // top pair including a pocket card, bet once or call 
						else if ((hasPairs.get(0) == currHand[0].getRank() || hasPairs.get(0) == currHand[1].getRank()) ){
							if (hasPairs.get(0) >= turnRanks[3]) {
								// bet once with top pair or over pair, otherwise call
								if (tablePlayers[i].getBet() < currBet) {
									if (currBet < 6.) {
										betOrRaise(i,6.,6.);
									}
									else callBetOrCheck(i,18.);
								}
								continue;
							}
							else if (!tight.contains(i)) {  //looser players call with a pair
								callBetOrCheck(i,12.);
								continue;
							}
						}
					}
					if (!tight.contains(i) && boardPairs.size() == 0 && currHand[0].getRank() > turnRanks[3] && currHand[1].getRank() > turnRanks[3] && currBet <= tablePlayers[i].getBet() + 6.) {
						// looser players call one bet with two overcards
						callBetOrCheck(i,6.);
						continue;
					}
					if (tablePlayers[i].getBet() >= currBet) {
						continue;  //check
					}
					else tablePlayers[i].foldHand();
				}
			}
		}
		if (currBet > 0.0) turnBets++;
	}
	public void betRiver(){
		currBet = 0.;
		lastRaise = 0;
		while (lastRaise > -1){
			lastRaise = -1;
			for (int j = 0; j < tablePlayers.length; j++) {
				int i = (button + 3 + j) % 10;
				if (!tablePlayers[i].hasFolded()) {
					Card[] currHand = new Card[7];
					
					currHand[0] = tablePlayers[i].getCard1();
					currHand[1] = tablePlayers[i].getCard2();
					currHand[2] = tableCards[0];
					currHand[3] = tableCards[1];
					currHand[4] = tableCards[2];
					currHand[5] = tableCards[3];
					currHand[6] = tableCards[4];
					Integer hasFlush = suitedCards(currHand, 5);
					Integer[] ranks = new Integer[currHand.length];
					for (int k = 0; k < currHand.length; k++) {
						ranks[k] = currHand[k].getRank();
					}
					Integer[] tableRanks = new Integer[tableCards.length];
					for (int k = 0; k < tableCards.length; k++) {
						tableRanks[k] = tableCards[k].getRank();
					}
					Arrays.sort(tableRanks);
					Integer hasStraight = hasStraight(ranks, 5); 
					List<Integer> boardPairs = multiSameRank(tableCards, 2);

					if ((hasFlush > 0 || (hasStraight > 0 && suitedCards(tableCards,3) == 0)) && boardPairs.size() == 0) {
						betOrRaise(i,24.,6.);
						continue;
					}
					Integer fullHouse = hasFullHouse(currHand);
					List<Integer> hasQuads = multiSameRank(currHand, 4);
					
					if (fullHouse > 0 || hasQuads.size() > 0) {
						betOrRaise(i,24.,6.);
						continue;
					}
					if (hasFlush > 0 || hasStraight > 0) {
						callBetOrCheck(i,12.);
						continue;
					}

					List<Integer> hasTrips = multiSameRank(currHand, 3);
					if (hasTrips.size() > 0 && boardPairs.size() == 0 && suitedCards(tableCards,3) == 0) {
						betOrRaise(i,18.,6.);
						continue;
					}
					if (hasTrips.size() > 0) {
						callBetOrCheck(i,12.);  // if no bets yet this checks and folds if currBet above 12
						if (currBet < 6.) {
							betOrRaise(i,6.,6.); //if no bets yet, this bets once
						}
						continue;					
					}
					List<Integer> hasPairs = multiSameRank(currHand, 2);
					if (hasPairs.size() > 1 && boardPairs.size() == 0 && suitedCards(tableCards,3) == 0) {
						betOrRaise(i,6.,6.); //re-raise once with two pair
						continue;
					}
					else if (hasPairs.size() > 1 && boardPairs.size() == 0) {
						callBetOrCheck(i,12.);  // call only two bets if possible flush out there
					}
					// one pair
					else if (hasPairs.size() > 0) {
						if (hasPairs.contains(1) && (tablePlayers[i].getCard1().getRank() == 1 || tablePlayers[i].getCard2().getRank() == 1)) {
							// bet once with pair of aces including a pocket card, otherwise call
							callBetOrCheck(i,12.);  // if no bets yet this checks and folds if currBet above 12
							if (currBet < 6.) {
								betOrRaise(i,6.,6.); //if no bets yet, this bets once
							}
							continue;					
						} // top pair including a pocket card, bet once or call 
						else if ((hasPairs.get(0) == currHand[0].getRank() || hasPairs.get(0) == currHand[1].getRank()) ){
							if (hasPairs.get(0) >= tableRanks[4]) {  //TODO: make aces dealt with correctly
								// bet once with top pair or over pair, otherwise call
								callBetOrCheck(i,12.);  // if no bets yet this checks and folds if currBet above 12
								if (currBet < 6.) {
									betOrRaise(i,6.,6.); //if no bets yet, this bets once
								}
								continue;
							}
							else if (!tight.contains(i)) {  //looser players call with a pair
								callBetOrCheck(i,12.);
								continue;
							}
						}
					}
					if (tablePlayers[i].getBet() >= currBet) {
						continue;  //check
					}
					else tablePlayers[i].foldHand();
				}
			}
		}
		if (currBet > 0.0) riverBets++;		
		if (!tablePlayers[9].hasFolded()  && currBet > 0.) riversSeen++;
	}
	public void findWinner(){
		//find best hand
		Card[] currHand = new Card[tableCards.length + 2];
		Integer flushPossible = suitedCards(tableCards, 3);  //returns suit of possible flush or zero if none
		Double[] scores = new Double[tablePlayers.length];
		List<Integer> pairsOnBoard = multiSameRank(tableCards, 2);
		List<Integer> flushCards = new ArrayList<Integer>();
		List<Integer> kickers = new ArrayList<Integer>();
		
		for (int j = 0; j < tableCards.length; j++) {
			currHand[j] = new Card(tableCards[j]);
		}
		for (int i = 0; i < tablePlayers.length; i++) {
			scores[i] = 0.0;
		}		
		if (flushPossible > 0) {
			for (int i = 0; i < tablePlayers.length; i++) {
				//determine if anyone has a flush
				if (tablePlayers[i].hasFolded()) continue;
				flushCards.clear();
				currHand[tableCards.length] = new Card(tablePlayers[i].getCard1());
				currHand[tableCards.length + 1] = new Card(tablePlayers[i].getCard2());
				if (suitedCards(currHand, 5) == flushPossible) {
					//System.out.println("Player " + i + " has flush in suit #" + flushPossible);
					for (int j = 0; j < currHand.length; j++) {
						if (currHand[j].getSuit() == flushPossible) {
							flushCards.add(currHand[j].getRank());
						}
					}
					Integer[] ranks = new Integer[flushCards.size()];
					flushCards.toArray(ranks);
					Arrays.sort(ranks);
					Integer highCard = hasStraight(ranks, 5); 
					if (highCard > 0) {
						//System.out.println("Player " + i + " has straight flush high card" + highCard);
						scores[i] = 160.0 + highCard;
					}
					else {
						//System.out.println("Player " + i + " has flush high card " + ranks[ranks.length - 1]);
						if (ranks[0] == 1) {
							scores[i] = 114.0 + ranks[ranks.length - 1]/100. + ranks[ranks.length - 2]/10000. + ranks[ranks.length - 3]/1000000. + ranks[ranks.length - 4]/100000000.;
						}
						else {
							scores[i] = 100.0 + ranks[ranks.length - 1] + ranks[ranks.length - 2]/100. + ranks[ranks.length - 3]/10000. + ranks[ranks.length - 4]/1000000. + ranks[ranks.length - 5]/100000000.;
						}
					}
				}
			}
		}
		if (findMaxScore(scores) > 160) {
			payWinner(scores);
			return;
		}
		//check for full house or quads
		if (pairsOnBoard.size() > 0) {
			List<Integer> currMults = new ArrayList<Integer>();
			for (int i = 0; i < tablePlayers.length; i++) {
				if (tablePlayers[i].hasFolded()) continue;
				currHand[tableCards.length] = new Card(tablePlayers[i].getCard1());
				currHand[tableCards.length + 1] = new Card(tablePlayers[i].getCard2());
				currMults = multiSameRank(currHand, 4);
				if(currMults.size() > 0) {
					//System.out.println("Player " + i + " has quad " + currMults.get(currMults.size()-1) + "s");
					for(int j = 0; j < currHand.length; j++) {
						if (currHand[j].getRank() != currMults.get(currMults.size()-1)) {
							kickers.add(currHand[j].getRank());						
						}
					}
					Integer[] ranks = new Integer[kickers.size()];
					kickers.toArray(ranks);
					Arrays.sort(ranks);
					if (currMults.get(currMults.size() - 1) == 1) {
						scores[i] = 154.0 + ranks[ranks.length - 1]/100.0;						
					}
					else {
						if (kickers.contains(1)) {
							scores[i] = 140.0 + currMults.get(currMults.size() - 1) + 0.14;
						}
						else {
							scores[i] = 140.0 + currMults.get(currMults.size() - 1) + ranks[ranks.length - 1]/100.0;
						}
					}
				}
				kickers.clear();
			}
			if (findMaxScore(scores) > 140) {
				payWinner(scores);
				return;
			}
			for (int i = 0; i < tablePlayers.length; i++) {
				if (tablePlayers[i].hasFolded()) continue;
				currHand[tableCards.length] = new Card(tablePlayers[i].getCard1());
				currHand[tableCards.length + 1] = new Card(tablePlayers[i].getCard2());
				
				currMults = multiSameRank(currHand, 3);
				if(currMults.size() > 0) {
					
					for(int j = 0; j < currHand.length; j++) {
						if (currHand[j].getRank() != currMults.get(currMults.size()-1)) {
							kickers.add(currHand[j].getRank());						
						}
					}
					Integer[] ranks = new Integer[kickers.size()];
					kickers.toArray(ranks);
					Arrays.sort(ranks);
					Integer kickerPair = 0;
					for (int j = ranks.length - 1; j > 0; j--) {
						if(ranks[j-1] == ranks[j]) {
							kickerPair = ranks[j];
							//System.out.println("Player " + i + " has " + currMults.get(currMults.size()-1) + "s full of " + kickerPair + "s");
							scores[i] = 120.0 + currMults.get(currMults.size()-1) + kickerPair/100.0;
							if (currMults.get(currMults.size()-1) == 1) scores[i] += 13.; // correct score for aces full
							j = 0;
						}
					}
					kickers.clear();
				}
			}
			
			//System.out.println("Board paired.  Highest pair is " + pairsOnBoard.get(pairsOnBoard.size()-1) + "s");
		}
		if (findMaxScore(scores) > 100) {
			payWinner(scores);
			return;
		}

		for (int i = 0; i < tablePlayers.length; i++) {
			//determine if anyone has a straight
			if (tablePlayers[i].hasFolded()) continue;
			Integer highCard = 0;
			Integer[] ranks = new Integer[currHand.length];
			currHand[tableCards.length] = new Card(tablePlayers[i].getCard1());
			currHand[tableCards.length + 1] = new Card(tablePlayers[i].getCard2());
			for (int j = 0; j < currHand.length; j++) {
				ranks[j] = currHand[j].getRank();
			}
			highCard = hasStraight(ranks, 5); 
			if (highCard > 0) {
				//System.out.println("Player " + i + " has " + highCard + " high straight");
				scores[i] = 80.0 + highCard;
			}
		}
		if (findMaxScore(scores) > 80) {
			payWinner(scores);
			return;
		}
		// if no one has a straight or better, look for trips, then two pair then one pair then high card.	
		List<Integer> currMults = new ArrayList<Integer>();
		for (int i = 0; i < tablePlayers.length; i++) {
			if (tablePlayers[i].hasFolded()) continue;
			currHand[tableCards.length] = new Card(tablePlayers[i].getCard1());
			currHand[tableCards.length + 1] = new Card(tablePlayers[i].getCard2());
			currMults = multiSameRank(currHand, 3);
			if(currMults.size() > 0) {
				//System.out.println("Player " + i + " has trip " + currMults.get(currMults.size()-1) + "s");
				for(int j = 0; j < currHand.length; j++) {
					if (currHand[j].getRank() != currMults.get(currMults.size()-1)) {
						kickers.add(currHand[j].getRank());						
					}
				}
				Integer[] ranks = new Integer[kickers.size()];
				kickers.toArray(ranks);
				Arrays.sort(ranks);
				if (currMults.contains(1)) {
					scores[i] = 74.0 + ranks[ranks.length - 1]/100. + ranks[ranks.length - 2]/10000.;
				}
				else {
					if (ranks[0] == 1) {
						scores[i] = 60.0 + currMults.get(currMults.size()-1) + 0.14 + ranks[ranks.length - 1]/10000.;
					}
					else {
						scores[i] = 60.0 + currMults.get(currMults.size()-1) + ranks[ranks.length - 1]/100. + ranks[ranks.length - 2]/10000.;
					}
				}
			}
		}
		if (findMaxScore(scores) > 60) {
			payWinner(scores);
			return;
		}
		//look for two pair, then one pair
		for (int i = 0; i < tablePlayers.length; i++) {
			if (tablePlayers[i].hasFolded()) continue;
			currHand[tableCards.length] = new Card(tablePlayers[i].getCard1());
			currHand[tableCards.length + 1] = new Card(tablePlayers[i].getCard2());
			currMults = multiSameRank(currHand, 2);
			kickers.clear();
			if (currMults.size() > 1) {
				for(int j = 0; j < currHand.length; j++) {
					if (currHand[j].getRank() != currMults.get(currMults.size()-1)  && currHand[j].getRank() != currMults.get(currMults.size()-2)) {
						kickers.add(currHand[j].getRank());						
					}
				}
				Integer[] ranks = new Integer[kickers.size()];
				kickers.toArray(ranks);
				Arrays.sort(ranks);
				if (currMults.contains(1)) {
					scores[i] = 54.0 + currMults.get(currMults.size()-2)/100. + ranks[ranks.length - 1]/10000.;
					//System.out.println("Player " + i + " has two pair 1s over " + currMults.get(currMults.size()-2) + "s");

				}
				else {
					//System.out.println("Player " + i + " has two pair " + currMults.get(currMults.size()-1) + "s over " + currMults.get(currMults.size()-2) + "s");
					if (ranks[0] == 1) { 
						scores[i] = 40.0 + currMults.get(currMults.size()-1) + currMults.get(currMults.size()-2)/100. + 14/10000.;	
					}
					else { 
						scores[i] = 40.0 + currMults.get(currMults.size()-1) + currMults.get(currMults.size()-2)/100. + ranks[ranks.length - 1]/10000.;	
					}
				}
			}
			else if (currMults.size() > 0) {
				for(int j = 0; j < currHand.length; j++) {
					if (currHand[j].getRank() != currMults.get(currMults.size()-1)) {
						kickers.add(currHand[j].getRank());						
					}
				}
				Integer[] ranks = new Integer[kickers.size()];
				kickers.toArray(ranks);
				Arrays.sort(ranks);
				if (currMults.contains(1)) {
					scores[i] = 34.0 + ranks[ranks.length - 1]/100. + ranks[ranks.length - 2]/10000. + ranks[ranks.length - 3]/1000000.;
					//System.out.println("Player " + i + " has one pair of 1s");
				}
				else {
					if (ranks[0] == 1) { 
						scores[i] = 20.0 + currMults.get(currMults.size()-1) + 14/100. + ranks[ranks.length - 1]/10000. + ranks[ranks.length - 2]/1000000.;
					}
					else { 
						scores[i] = 20.0 + currMults.get(currMults.size()-1) + ranks[ranks.length - 1]/100. + ranks[ranks.length - 2]/10000. + ranks[ranks.length - 3]/1000000.;	
					}
					//System.out.println("Player " + i + " has one pair of " + currMults.get(currMults.size()-1) + "s");
				}
			} 
		}
		if (findMaxScore(scores) > 20) {
			payWinner(scores);
			return;
		}
		//if we get this far, determine best high card hand.
		for (int i = 0; i < tablePlayers.length; i++) {
			if (tablePlayers[i].hasFolded()) continue;
			currHand[tableCards.length] = new Card(tablePlayers[i].getCard1());
			currHand[tableCards.length + 1] = new Card(tablePlayers[i].getCard2());
			kickers.clear();
			for(int j = 0; j < currHand.length; j++) {
				kickers.add(currHand[j].getRank());						
			}
			Integer[] ranks = new Integer[kickers.size()];
			kickers.toArray(ranks);
			Arrays.sort(ranks);
			if (ranks[0] == 1) { 
				scores[i] = 14.0;
			}
			else {
				scores[i] = ranks[ranks.length-1]/1.;
			}
			for (int j = 2; j < 6; j++) {
				scores[i] += 1.*ranks[ranks.length-j]/(10^(2*(j-1)));
			}
		}
		payWinner(scores);
		return;
	}
		
	public void payWinner(Double[] scores) {	
		Double maxScore = findMaxScore(scores);
		List<Integer> winningHand = new ArrayList<Integer>();
		for (int i = 0; i < tablePlayers.length; i++) {
			if (scores[i] >= maxScore) {
				//System.out.println("Player " + i + ": " + scores[i]);
				maxScore = scores[i];
				winningHand.add(i);
			}
		}
		Double bankrollTotal = 0.;
		for (int i = 0; i < tablePlayers.length; i++) {
			updateStats(tablePlayers[i], 0, 1);
			tablePlayers[i].resetBet();
			bankrollTotal += tablePlayers[i].getBankRoll();
		}
		//System.out.println("bankroll Total: " + bankrollTotal + " and currPot: " + pot);
		for (int i = 0; i < winningHand.size(); i++) {
			updateStats(tablePlayers[winningHand.get(i)], 1, winningHand.size());
			//System.out.println("winner's bankroll: " + tablePlayers[winningHand.get(i)].getBankRoll());
			tablePlayers[winningHand.get(i)].addPotToBankRoll(pot/winningHand.size());
			//System.out.println("winner's bankroll: " + tablePlayers[winningHand.get(i)].getBankRoll());
		}
		totalPots += pot;
		//System.out.println("paid pot to winners");
		// after winning stats updated, update all pocket card stats
		bankrollTotal = 0.;
		for (int i = 0; i < tablePlayers.length; i++) {
			updateStats(tablePlayers[i], 0, 1);
			tablePlayers[i].resetBet();
			bankrollTotal += tablePlayers[i].getBankRoll();
		}
		if (minBankRoll > tablePlayers[9].getBankRoll()) minBankRoll = tablePlayers[9].getBankRoll();
		if (winningHand.contains(9)) handsWon++;
		//System.out.println("bankroll Total: " + bankrollTotal + " and currPot: " + pot);
		pot = 0.;
	//determine winner and add pot to their bankroll
	}

}
