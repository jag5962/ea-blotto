package dynamicstrategy;

import baseline.*;
import com.google.gson.Gson;

import java.io.FileWriter;

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
    private static final int STRATEGY_SIZE = 10;

    public static void main(String[] args) throws Exception {
        final boolean write = false;
        Gson gson = new Gson();

        for (int s = 0; s < (write ? 100 : 1); s++) {
            Strategy player1 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);
            Strategy player2 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);

            double avgUtilPerGame1 = 0;
            double avgUtilPerGame2 = 0;
            double p1WinPercent = 0;
            double p1TotalUtil = 0, p2TotalUtil = 0;

            // Terminates once at least GAMES games are played, p1 win percent is 75%-90%, all schemes have a positive prob, and strategy size is correct.
            for (int g = 1; g <= GAMES || p1WinPercent > .9 || p1WinPercent < .75 || player1.hasZeroProbabilities()
                    || player1.size() != STRATEGY_SIZE; g++) {
                int player1Wins = 0, player2Wins = 0;

                // Change strategy size only when beginning a new game
                if (p1TotalUtil > p2TotalUtil) {
                    player1.adjustSize(STRATEGY_SIZE);
                    player1.resetStrategy();
                }

                p1TotalUtil = 0;
                p2TotalUtil = 0;

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

                avgUtilPerGame1 += (p1TotalUtil / ROUNDS_PER_GAME);
                avgUtilPerGame2 += (p2TotalUtil / ROUNDS_PER_GAME);

                Strategy winner = p1TotalUtil > p2TotalUtil ? player1 : player2;
                Strategy loser = p1TotalUtil > p2TotalUtil ? player2 : player1;

                p1WinPercent = player1Wins / (double) ROUNDS_PER_GAME;

                if (!write) {
                    System.out.println("Game " + g);
                    System.out.println("Player1 wins: " + player1Wins + ", Player2 wins: " + player2Wins);
                    System.out.println("Player1 utility per game: " + (p1TotalUtil / ROUNDS_PER_GAME) + ", Player2 utility per game: " + (p2TotalUtil / ROUNDS_PER_GAME));

                    System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 1 : 2) + " Strategy:");
                    System.out.println(winner);
                    System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 2 : 1) + " Strategy:");
                    System.out.println(loser);
                }

                // Generate new schemes for loser and reset the winner's strategy
                if (p1TotalUtil > p2TotalUtil) {
                    player2 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);
                } else {
                    player1 = EA.evolve(player1, STRATEGY_SIZE);
                    player2.resetStrategy();
                }
            }

            if (write) {
                // Save strategy to JSON file
                System.out.print("\r" + s);
                try (FileWriter file = new FileWriter("strategies/dynamicstrategy/" + s + ".json")) {
                    file.write(gson.toJson(player1));
                }
            } else {
                System.out.println("\nPlayer1 avg util: " + avgUtilPerGame1 / GAMES);
                System.out.println("Player2 avg util: " + avgUtilPerGame2 / GAMES);
            }
        }
    }
}
