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
     * Evaluate the expected value for each strategy. This is a competitive co-evolution fitness function.
     *
     * @param player1 the strategy pool of player 1
     * @param player2 the strategy pool of player 2
     */
    public static void evaluateFitness(StrategyPool player1, StrategyPool player2) {
        player1.resetExpectedValues();
        player2.resetExpectedValues();

        for (Strategy strat1 : player1) {
            for (Strategy strat2 : player2) {
                int p1Utility = ColonelBlotto.utility(strat1, strat2);

                double expectedValue = strat1.getExpectedValue();
                expectedValue += strat2.getAverageProb() * p1Utility;
                strat1.setExpectedValue(expectedValue);

                expectedValue = strat2.getExpectedValue();
                expectedValue += strat1.getAverageProb() * -p1Utility;
                strat2.setExpectedValue(expectedValue);
            }
        }

        player1.sort();
        player2.sort();
    }

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
            Strategy child = crossover(parents, loser.getTroopCount());

            // Mutate with probability
            if (RANDOM.nextDouble() < MUTATION_RATE) {
                mutate(child);
            }
            strategySet.add(child);
        }

        return new StrategyPool(loser, strategySet);
    }

    /**
     * Select two parent strategies to produce a child strategy using tournament selection. First part of reproduction.
     *
     * @param loser the losing player's strategy pool
     * @return two strategies from loser
     */
    protected static Strategy[] selectParents(StrategyPool loser) {
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
     * @param parents    the two parent strategies
     * @param troopCount the number of troops for losing player's strategy pool
     * @return the child strategy
     */
    protected static Strategy crossover(Strategy[] parents, int troopCount) {
        return new Strategy(parents, troopCount);
    }

    /**
     * Mutate the strategy by swapping troops between two battlefields.
     *
     * @param strategy the strategy to mutate
     */
    protected static void mutate(Strategy strategy) {
        int battlefield1 = RANDOM.nextInt(strategy.getNumberOfBattlefields()), battlefield2;
        do {
            battlefield2 = RANDOM.nextInt(strategy.getNumberOfBattlefields());
        } while (battlefield2 == battlefield1);
        strategy.swapTroops(battlefield1, battlefield2);
    }
}
