package regretmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Driver {
    static Random prng = new Random();
    static final int STRATEGY_SIZE = 5;
    static final int BATTLEFIELD_COUNT = 10;
    static final int SOLDIER_COUNT = 100;
    static final int TOTAL_EPOCHS = 2_000;
    static final int ROUNDS_PER_EPOCH = 100;

    public static void main(String[] args) {

        // Initialize the players with random allocations
        // You should use whatever initial solutions from your GA
        Player player1 = new Player(getRandomAllocations(STRATEGY_SIZE, BATTLEFIELD_COUNT, SOLDIER_COUNT), SOLDIER_COUNT);
        Player player2 = new Player(getRandomAllocations(STRATEGY_SIZE, BATTLEFIELD_COUNT, SOLDIER_COUNT), SOLDIER_COUNT);

        for (int i = 0; i < TOTAL_EPOCHS; i++) {

            int p1Wins = 0, p2Wins = 0;
            double p1TotalUtility = 0, p2TotalUtility = 0;
            // Train the players with the current strategies
            for (int r = 0; r < ROUNDS_PER_EPOCH; r++) {

                // Get a randomly chosen strategy for each player
                int p1Choice = player1.chooseAction();
                int[] p1Action = player1.getAllocation(p1Choice);

                int p2Choice = player2.chooseAction();
                int[] p2Action = player2.getAllocation(p2Choice);

                double p1Utility = GameBlotto.utility(p1Action, p2Action);
                double p2Utility = -p1Utility;

                p1TotalUtility += p1Utility;
                p2TotalUtility += p2Utility;

                if (p1Utility > p2Utility) {
                    p1Wins++;
                } else if (p1Utility < p2Utility) {
                    p2Wins++;
                }
                player1.update(p1Choice, p2Action);
                player2.update(p2Choice, p1Action);
            }

            System.out.println("\n\nEpoch: " + i + " results");
            System.out.println("Player1 wins: " + p1Wins + " Player2 wins: " + p2Wins);
            System.out.println("Player1 utility per game: " + (p1TotalUtility / ROUNDS_PER_EPOCH) + " Player2 utility per game: " + (p2TotalUtility / ROUNDS_PER_EPOCH));

            Player winner = p1TotalUtility > p2TotalUtility ? player1 : player2;
            Player loser = p1TotalUtility > p2TotalUtility ? player2 : player1;
            // Output the winning player's strategy

            System.out.println("Player " + (p1TotalUtility > p2TotalUtility ? 1 : 2) + " Strategy:");
            double[] s = winner.getLearnedStrategy();
            for (int j = 0; j < s.length; j++) {
                System.out.print("Prob:  " + s[j] + " {");
                for (int k = 0; k < winner.getActions()[j].length; k++) {
                    System.out.print(" " + winner.getActions()[j][k]);
                }
                System.out.println(" }");
            }

            System.out.println("Player " + (p1TotalUtility > p2TotalUtility ? 2 : 1) + " Strategy:");
            s = loser.getLearnedStrategy();
            for (int j = 0; j < s.length; j++) {
                System.out.print("Prob:  " + s[j] + " {");
                for (int k = 0; k < loser.getActions()[j].length; k++) {
                    System.out.print(" " + loser.getActions()[j][k]);
                }
                System.out.println(" }");
            }

            // Adjust losing player's actions
            // Rather than using my random adjustment, you will want to use your GA
            // to determine new allocations
            EA.evolve(loser);


            // Reset the strategies
            loser.resetStrategy(loser.getActions());
            // Note that the actions are the same for the winner, but we want to 0 out the regret
            winner.resetStrategy(winner.getActions());

        }
    }


    static int[][] getRandomAllocations(int strategy_size, int battlefield_size, int soldier_size) {
        int[][] actions = new int[strategy_size][battlefield_size];
        for (int i = 0; i < strategy_size; i++) {
            int remaining = soldier_size;
            for (int j = 0; j < battlefield_size - 1; j++) {
                if (remaining > 0) {
                    actions[i][j] = Math.min(remaining, prng.nextInt(2 * soldier_size / battlefield_size));
                    remaining -= actions[i][j];
                }
            }
            actions[i][battlefield_size - 1] = remaining;
        }
        return actions;
    }

    /**
     * Adjust an allocation that is played less than 1/n of the time
     * where n is the number of allocations in the strategy
     *
     * @param actions  the allocations in the current strategy
     * @param strategy the probabilities of choosing each allocation
     */
    static void adjustAllocations(int[][] actions, double[] strategy, int[][] otherActions, double[] otherStrategy) {
        ArrayList<Integer> badActionIndexes = new ArrayList<>();
        ArrayList<Integer> oppActionIndexes = new ArrayList<>();
        for (int i = 0; i < actions.length; i++) {
            if (strategy[i] <= 1.0 / strategy.length) {
                badActionIndexes.add(i);
            }
            if (otherStrategy[i] >= 1.0 / strategy.length) {
                oppActionIndexes.add(i);
            }
        }

        int from = oppActionIndexes.get(prng.nextInt(oppActionIndexes.size()));
        int target = badActionIndexes.get(prng.nextInt(badActionIndexes.size()));

        int[] allocation = new int[BATTLEFIELD_COUNT];

        int soldiersRemaining = SOLDIER_COUNT;
        for (int j = 0; soldiersRemaining > 0 && j < BATTLEFIELD_COUNT; j++) {
            int bfield = -1;
            double payoff = -1;
            for (int k = 0; k < BATTLEFIELD_COUNT; k++) {
                if (allocation[k] == 0) {
                    if (otherActions[from][k] < soldiersRemaining) {
                        double tmpPayoff = (k + 1.0) / (otherActions[from][k] + 1.0);
                        if (tmpPayoff > payoff) {
                            payoff = tmpPayoff;
                            bfield = k;
                        }
                    }
                }
            }
            if (bfield >= 0) {
                allocation[bfield] = otherActions[from][bfield] + 1;
                soldiersRemaining -= allocation[bfield];
                if (soldiersRemaining < 0) throw new RuntimeException();
            } else
                j = BATTLEFIELD_COUNT;
        }
        while (soldiersRemaining > 0) {
            int to = prng.nextInt(allocation.length);
            allocation[to]++;
            soldiersRemaining--;
        }
        // Ensure that this is unique
        boolean unique = true;
        for (int i = 0; unique && i < actions.length; i++) {
            if (Arrays.equals(actions[i], allocation))
                unique = false;
        }
        if (unique) {
            actions[target] = allocation;
        }
    }
}