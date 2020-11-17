package coevolved;

import com.google.gson.Gson;
import baseline.*;

import java.io.FileWriter;
import java.io.IOException;

/**
 * A co-evolved variant for finding optimal mixed strategies in Colonel Blotto. Both players' strategies are evolved.
 *
 * @author John Gilbertson
 */
public class CoevolvedDriver {
    private static final int NUMBER_OF_BATTLEFIELDS = 10;
    private static final int GAMES = 50;
    private static final int ROUNDS_PER_GAME = 10_000;
    private static final int STRATEGY_SIZE = 50;

    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        for (int i = 0; i < 100; i++) {
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

                Strategy winner = p1TotalUtil > p2TotalUtil ? player1 : player2;
                Strategy loser = p1TotalUtil > p2TotalUtil ? player2 : player1;

                System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 1 : 2) + " Strategy:");
                System.out.println(winner);
                System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 2 : 1) + " Strategy:");
                System.out.println(loser);

                // Generate new strategies for loser
                if (p1TotalUtil > p2TotalUtil) {
                    player2 = EA.evolve(loser, STRATEGY_SIZE);
                } else {
                    player1 = EA.evolve(loser, STRATEGY_SIZE);
                }

                // Reset the winner's strategy
                winner.resetStrategy();
            }

            // Save strategy to JSON file
            String jsonString = gson.toJson(player1);
            FileWriter myWriter = new FileWriter("strategies/coevolved/" + i + "a.json");
            myWriter.write(jsonString);
            myWriter.close();
            jsonString = gson.toJson(player2);
            myWriter = new FileWriter("strategies/coevolved/" + i + "b.json");
            myWriter.write(jsonString);
            myWriter.close();
        }
    }
}
