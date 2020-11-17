package baseline;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;

/**
 * The baseline for finding optimal mixed strategies in Colonel Blotto. Regret-matching is used to determine
 * the probabilities to play each scheme. This instance of Colonel Blotto uses static strategy sizes and
 * distinctly increasing battlefield payoffs. Only player 1 evolves their strategy.
 *
 * @author John Gilbertson
 */
public class BaselineDriver {
    private static final int NUMBER_OF_BATTLEFIELDS = 10;
    private static final int GAMES = 50;
    private static final int ROUNDS_PER_GAME = 10_000;
    private static final int STRATEGY_SIZE = 50;

    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        for (int i = 0; i < 1; i++) {
            Strategy player1 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);
            Strategy player2 = new Strategy(NUMBER_OF_BATTLEFIELDS, STRATEGY_SIZE, 100);

            double avgUtilPerGame1 = 0;
            double avgUtilPerGame2 = 0;

            for (int g = 1; g <= GAMES; g++) {
                int player1Wins = 0, player2Wins = 0;
                double p1TotalUtil = 0, p2TotalUtil = 0;

                for (int r = 0; r < ROUNDS_PER_GAME; r++) {
                    // Get a randomly chosen scheme for each player
                    Scheme player1Strat = player1.getRandom();
                    Scheme player2Strat = player2.getRandom();

                    int player1Util = utility(player1Strat, player2Strat);
                    int player2Util = -player1Util;

                    p1TotalUtil += player1Util;
                    p2TotalUtil += player2Util;

                    if (player1Util > player2Util) {
                        player1Wins++;
                    } else if (player1Util < player2Util) {
                        player2Wins++;
                    }

                    player1.update(player1Strat, player2Strat, player1Util);
                    player2.update(player2Strat, player1Strat, player2Util);
                }

                // Calculate expected value for all the schemes for both players
                EA.evaluateFitness(player1, player2);

                avgUtilPerGame1 += (p1TotalUtil / ROUNDS_PER_GAME);
                avgUtilPerGame2 += (p2TotalUtil / ROUNDS_PER_GAME);

                System.out.println("Game " + g);
                System.out.println("Player1 wins: " + player1Wins + ", Player2 wins: " + player2Wins);
                System.out.println("Player1 utility per game: " + (p1TotalUtil / ROUNDS_PER_GAME) + ", Player2 utility per game: " + (p2TotalUtil / ROUNDS_PER_GAME));

                Strategy winner = p1TotalUtil > p2TotalUtil ? player1 : player1;
                Strategy loser = p1TotalUtil > p2TotalUtil ? player1 : player1;

                System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 1 : 2) + " Strategy:");
                System.out.println(winner);
                System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 2 : 1) + " Strategy:");
                System.out.println(loser);

                // Generate new schemes for loser
                if (p1TotalUtil > p2TotalUtil) {
                    player2 = new Strategy(NUMBER_OF_BATTLEFIELDS, 50, 100);
                } else {
                    player1 = EA.evolve(loser, STRATEGY_SIZE);
                }

                // Reset the winner's strategy
                winner.resetStrategy();
            }

            // Save strategy to JSON file
            String jsonString = gson.toJson(player1);
            FileWriter myWriter = new FileWriter("strategies/baseline/" + i + ".json");
            myWriter.write(jsonString);
            myWriter.close();

            System.out.println("\nPlayer1 avg util: " + avgUtilPerGame1 / GAMES);
            System.out.println("Player2 avg util: " + avgUtilPerGame2 / GAMES);
        }
    }

    /**
     * Play player 1's scheme against player 2's strategy
     *
     * @param player1Scheme the scheme for player 1
     * @param player2Scheme the scheme for player 2
     * @return the values 1, -1, or 0 if player 1 wins, loses, or ties, respectively
     */
    public static int utility(Scheme player1Scheme, Scheme player2Scheme) {
        int player1Score = 0, player2Score = 0;
        for (int i = 0; i < NUMBER_OF_BATTLEFIELDS; i++) {
            if (player1Scheme.getBattlefieldTroops(i) > player2Scheme.getBattlefieldTroops(i)) {
                player1Score += i + 1;
            } else if (player1Scheme.getBattlefieldTroops(i) < player2Scheme.getBattlefieldTroops(i)) {
                player2Score += i + 1;
            }
        }
        return Integer.compare(player1Score, player2Score);
    }
}
