package baseline;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

public class EA {
    private static final double ELITISM_RATE = .2;   // The rate to bring the elite strategies to the next generation
    private static final double MUTATION_RATE = .05; // The rate to mutate an offspring
    private static final Random RANDOM = new Random();

    /**
     * Evolve the strategy pool for loser.
     *
     * @param loser the losing player's strategy pool
     * @return the evolved strategy pool for this player
     */
    public static StrategyPool evolve(StrategyPool loser) {
        // Use a set to prohibit duplicate strategies
        Set<Strategy> strategySet = new HashSet<>();

        // Copy elites to next generation
        int eliteCount = (int) Math.ceil(ELITISM_RATE * loser.size());
        for (int i = 0; i < eliteCount; i++) {
            strategySet.add(loser.get(i));
        }

        // Use reproduction and mutation to fill the rest of the new strategy pool
        while (strategySet.size() < loser.size()) {
            // Select 2 parent strategies for crossover using tournament selection
            Strategy[] parents = selectParents(loser);

            // Use crossover to produce child strategy
            Strategy child = crossover(parents, loser);

            // Mutate with probability
            if (RANDOM.nextDouble() < MUTATION_RATE) {
                mutate(child);
            }
            strategySet.add(child);
        }

        StrategyPool evolved = new StrategyPool(loser, strategySet);
        evolved.resetStrategies();
        return evolved;
    }

    /**
     * Select two parent strategies to produce a child strategy using tournament selection. First part of reproduction.
     *
     * @param loser the losing player's strategy pool
     * @return two strategies from loser
     */
    private static Strategy[] selectParents(StrategyPool loser) {
        final int tournamentSize = (int) Math.max(Math.ceil(ELITISM_RATE * loser.size()), 5);

        TreeSet<Strategy> tournament1 = new TreeSet<>();
        while (tournament1.size() < tournamentSize) {
            tournament1.add(loser.get(RANDOM.nextInt(loser.size())));
        }

        TreeSet<Strategy> tournament2 = new TreeSet<>();
        while (tournament2.size() < tournamentSize) {
            tournament2.add(loser.get(RANDOM.nextInt(loser.size())));
        }

        return new Strategy[]{tournament1.last(), tournament2.last()};
    }

    /**
     * Perform the crossover portion of reproduction.
     *
     * @param parents the two parent strategies
     * @param loser   the losing player's strategy pool
     * @return the child strategy
     */
    private static Strategy crossover(Strategy[] parents, StrategyPool loser) {
        return new Strategy(parents, loser);
    }

    /**
     * Mutate the strategy by swapping troops between two battlefields.
     *
     * @param strategy the strategy to mutate
     */
    private static void mutate(Strategy strategy) {
        int battlefield1 = RANDOM.nextInt(strategy.getNumberOfBattlefields()), battlefield2;
        do {
            battlefield2 = RANDOM.nextInt(strategy.getNumberOfBattlefields());
        } while (battlefield2 == battlefield1);
        strategy.swapTroops(battlefield1, battlefield2);
    }
}
