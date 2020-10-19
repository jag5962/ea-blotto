package baseline;

public class ColonelBlotto {
    public static final int NUMBER_OF_BATTLEFIELDS = 10;
    private static final int GAMES = 10;
    public static final int ROUNDS_PER_GAME = 10000;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        StrategyPool player1 = new StrategyPool(50, 100);
        StrategyPool player2 = new StrategyPool(50, 100);

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

                player1Strat.updateUtilityThisRound(player1Util);
                player2Strat.updateUtilityThisRound(player2Util);
            }
            player1.calculateFitness();
            player2.calculateFitness();

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

            // Reset utility and play count for each strategy
            winner.resetPlays();
        }

        long durationInMillis = System.currentTimeMillis() - start;
        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / 60000) % 60;

        System.out.printf("%02d:%02d.%d\n", minute, second, millis);
    }

    private static int utility(Strategy player1, Strategy player2) {
        int player1Score = 0, player2Score = 0;
        for (int i = 0; i < NUMBER_OF_BATTLEFIELDS; i++) {
            if (player1.getBattlefieldTroops(i) > player2.getBattlefieldTroops(i)) {
                player1Score += i + 1;
            } else if (player1.getBattlefieldTroops(i) < player2.getBattlefieldTroops(i)) {
                player2Score += i + 1;
            }
        }
        return Integer.compare(player1Score, player2Score);
    }
}
