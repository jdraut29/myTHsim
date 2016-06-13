package texasSim;
import java.io.FileNotFoundException;

public class mainSimulator {
	public static void main(String [] args) throws FileNotFoundException {
	    PokerTable mySim = new PokerTable();
	    mySim.setUp();
//	    System.out.println(System.currentTimeMillis());
//	    for (int m = 0; m < 100; m++) {
	    	for (int n = 0; n < 100000; n++) {
		    	mySim.shuffle();
		    	mySim.deal();
		    	mySim.findWinner();
		    }
	    	System.out.println("Flops played: " + mySim.flopsPlayed + " ; Hands won: " +mySim.handsWon);
	    	mySim.flopsPlayed = 0;
//	    }
	    for (int i = 0; i < 169; i++) {
	    	System.out.println(i + "," + mySim.getWinningPocketHands(i)/mySim.getPocketHands(i));
	    	//System.out.println(mySim.getWinningPocketHands(i) + " \tindex: " + i);
	    }
//	    System.out.println(System.currentTimeMillis());
//	    for (int i = 0; i < mySim.getNumPlayers(); i++) {
	    	System.out.println("Bankroll " + 9 + ": " + mySim.getPlayerBankRoll(9) );
//	    	//System.out.println(mySim.getWinningPocketHands(i) + " \tindex: " + i);
//	    }
	    System.out.println("Min Bankroll for #9: " + mySim.minBankRoll );
	    System.out.println("Rivers seen: " + mySim.riversSeen );
//	    System.out.println("Raise count: " + mySim.getRaiseCount() );
	    System.out.println("Total pots: " + mySim.getTotalPots() );
	    System.out.println("Flops bet: " + mySim.flopBets );
	    System.out.println("Flops seen: " + mySim.flopsSeen );
	    System.out.println("Flops played: " + mySim.flopPlayers );
	    System.out.println("Turns played: " + mySim.turnPlayers );
	    System.out.println("Turns seen: " + mySim.turnsSeen );
	    System.out.println("Turns bet: " + mySim.turnBets );
	    System.out.println("Rivers played: " + mySim.riverPlayers );
	    System.out.println("Rivers seen: " + mySim.riversSeen );
	    System.out.println("Rivers bet: " + mySim.riverBets );
	    System.out.println("Showdowns: " + mySim.showdowns );
	    return;
	  }
}
