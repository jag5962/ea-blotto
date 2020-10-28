package baseline;

/**
 * The baseline for finding optimal mixed strategies in Colonel Blotto. Regret-matching is used to determine the
 * probabilities for every pure strategy. This instance of Colonel Blotto uses static strategy pool sizes and
 * distinctly increasing battlefield payoffs.
 *
 * @author John Gilbertson
 */
public class ColonelBlotto {
    private static final int NUMBER_OF_BATTLEFIELDS = 10;
    private static final int GAMES = 10;
    private static final int ROUNDS_PER_GAME = 10_000;

    public static void main(String[] args) {
        StrategyPool player1 = new StrategyPool(NUMBER_OF_BATTLEFIELDS, 50, 100);
        StrategyPool player2 = new StrategyPool(NUMBER_OF_BATTLEFIELDS, 50, 100);

        for (int g = 1; g <= GAMES; g++) {
            int player1Wins = 0, player2Wins = 0;
            double p1TotalUtil = 0, p2TotalUtil = 0;

            for (int r = 0; r < ROUNDS_PER_GAME; r++) {
                // Get a randomly chosen strategy for each player
                Strategy player1Strat = player1.getRandom();
                Strategy player2Strat = player2.getRandom();

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

            // Calculate expected value for all the strategies for both players
            EA.evaluateFitness(player1, player2);

            System.out.println("Game " + g);
            System.out.println("Player1 wins: " + player1Wins + ", Player2 wins: " + player2Wins);
            System.out.println("Player1 utility per game: " + (p1TotalUtil / ROUNDS_PER_GAME) + ", Player2 utility per game: " + (p2TotalUtil / ROUNDS_PER_GAME));

            StrategyPool winner = p1TotalUtil > p2TotalUtil ? player1 : player2;
            StrategyPool loser = p1TotalUtil > p2TotalUtil ? player2 : player1;

            System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 1 : 2) + " Strategy:");
            System.out.println(winner);
            System.out.println("Player " + (p1TotalUtil > p2TotalUtil ? 2 : 1) + " Strategy:");
            System.out.println(loser);

            // Generate new strategies for loser
            if (p1TotalUtil > p2TotalUtil) {
                player2 = EA.evolve(loser);
            } else {
                player1 = EA.evolve(loser);
            }

            // Reset the winner's strategies
            winner.resetStrategies();
        }
    }

    /**
     * Play player 1's strategy against player 2's strategy
     *
     * @param player1Strat the strategy for player 1
     * @param player2Strat the strategy for player 2
     * @return the values 1, -1, or 0 if player 1 wins, loses, or ties, respectively
     */
    public static int utility(Strategy player1Strat, Strategy player2Strat) {
        int player1Score = 0, player2Score = 0;
        for (int i = 0; i < NUMBER_OF_BATTLEFIELDS; i++) {
            if (player1Strat.getBattlefieldTroops(i) > player2Strat.getBattlefieldTroops(i)) {
                player1Score += i + 1;
            } else if (player1Strat.getBattlefieldTroops(i) < player2Strat.getBattlefieldTroops(i)) {
                player2Score += i + 1;
            }
        }
        return Integer.compare(player1Score, player2Score);
    }
}
