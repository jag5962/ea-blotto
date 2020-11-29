package search;

import baseline.Scheme;
import baseline.Strategy;

import java.util.HashSet;
import java.util.Set;

public class EA {
    /**
     * Evolve the strategy for loser.
     *
     * @param loser  the losing player's strategy
     * @param winner the winning player's strategy
     * @return the evolved strategy for this player
     */
    public static Strategy evolve(Strategy loser, Strategy winner) {
        // Use a set to prohibit duplicate schemes
        Set<Scheme> strategySet = new HashSet<>();

        // Copy elites to next generation
        int eliteCount = (int) Math.ceil(baseline.EA.ELITISM_RATE * loser.size());
        for (int i = 0; i < eliteCount; i++) {
            strategySet.add(loser.get(i));
        }

        // Use reproduction and mutation to fill the rest of the new strategy
        while (strategySet.size() < loser.size()) {
            // Select 2 parent schemes for crossover using tournament selection
            Scheme[] parents = baseline.EA.selectParents(loser);

            // Use crossover to produce child scheme
            Scheme child = baseline.EA.crossover(parents, loser.getTroopCount());

            // Mutate with probability
            if (baseline.EA.RANDOM.nextDouble() < baseline.EA.MUTATION_RATE) {
                mutate(child, winner);
            }
            strategySet.add(child);
        }

        return new Strategy(loser, strategySet);
    }

    /**
     * Mutate the scheme by searching for better troop allocation for a battlefield.
     *
     * @param scheme the scheme to mutate
     * @param winner the pool of strategies that won
     */
    private static void mutate(Scheme scheme, Strategy winner) {
        final double BOUND = .25;
        double[] lossPercent = new double[scheme.getNumberOfBattlefields()];
        int[] largestDeficit = new int[lossPercent.length];

        // Determine the percentage that this battlefield is loss against the enemy's scheme
        for (int battlefield = 0; battlefield < scheme.getNumberOfBattlefields(); battlefield++) {
            for (Scheme enemyScheme : winner) {
                if (scheme.getBattlefieldTroops(battlefield) < enemyScheme.getBattlefieldTroops(battlefield)) {
                    lossPercent[battlefield]++;
                    largestDeficit[battlefield] = Math.max(largestDeficit[battlefield],
                            enemyScheme.getBattlefieldTroops(battlefield) - scheme.getBattlefieldTroops(battlefield));
                }
            }
            lossPercent[battlefield] /= winner.size();
        }

        // Get the largest and smallest loss percentages
        double smallest = 1, largest = 0;
        int smallestIndex = -1, largestIndex = -1;
        for (int battlefield = 0; battlefield < scheme.getNumberOfBattlefields(); battlefield++) {
            if (lossPercent[battlefield] < smallest) {
                smallest = lossPercent[battlefield];
                smallestIndex = battlefield;
            } else if (lossPercent[battlefield] >= largest) {
                largest = lossPercent[battlefield];
                largestIndex = battlefield;
            }
        }

        // Only perform mutation when going to a later battlefield
        if (smallestIndex < largestIndex) {
            int troopsToMove = Math.min(largestDeficit[largestIndex], scheme.getBattlefieldTroops(smallestIndex));
            scheme.moveTroops(largestIndex, smallestIndex, troopsToMove);
        }
    }
}
