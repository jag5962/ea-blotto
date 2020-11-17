package dynamicstrategy;

import baseline.*;

/**
 * The dynamic strategy variation of the baseline for finding optimal mixed strategies in Colonel Blotto. It sizes the
 * strategy dynamically.
 *
 * @author John Gilbertson
 */
public class DynamicStrategyDriver {
    private static final int NUMBER_OF_BATTLEFIELDS = 10;
    private static final int GAMES = 50;
    private static final int ROUNDS_PER_GAME = 10_000;
    private static final int STRATEGY_SIZE = 50;

    public static void main(String[] args) {
        Strategy player1 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);
        Strategy player2 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);

        for (int g = 1; g <= GAMES; g++) {
            int player1Wins = 0, player2Wins = 0;
            double p1TotalUtil = 0, p2TotalUtil = 0;

            for (int r = 0; r < ROUNDS_PER_GAME; r++) {
                // Get a randomly chosen scheme for each player
                Scheme player1Scheme = player1.getRandom();
                Scheme player2Scheme = player2.getRandom();

                int player1Util = BaselineDriver.utility(player1Scheme, player2Scheme);
                int player2Util = -player1Util;

                p1TotalUtil += player1Util;
                p2TotalUtil += player2Util;

                if (player1Util > player2Util) {
                    player1Wins++;
                } else if (player1Util < player2Util) {
                    player2Wins++;
                }

                player1.update(player1Scheme, player2Scheme, player1Util);
                player2.update(player2Scheme, player1Scheme, player2Util);
            }

            // Calculate expected value for all the schemes for both players
            EA.evaluateFitness(player1, player2);

            System.out.println("Game " + g);
            System.out.println("Player1 wins: " + player1Wins + ", Player2 wins: " + player2Wins);
            System.out.println("Player1 utility per game: " + (p1TotalUtil / ROUNDS_PER_GAME) + ", Player2 utility per game: " + (p2TotalUtil / ROUNDS_PER_GAME));

            System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 1 : 2) + " Strategy:");
            System.out.println(p1TotalUtil > p2TotalUtil ? player1 : player2);
            System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 2 : 1) + " Strategy:");
            System.out.println(p1TotalUtil > p2TotalUtil ? player2 : player1);

            // Generate new schemes for loser and reset the winner's strategy
            if (p1TotalUtil > p2TotalUtil) {
                player2 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);
                player1.adjustSize();
                player1.resetStrategy();
            } else {
                player1 = EA.evolve(player1, STRATEGY_SIZE);
                player2.resetStrategy();
            }
        }
    }
}
