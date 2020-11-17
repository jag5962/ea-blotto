package baseline;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

public class EA {
    public static final double ELITISM_RATE = .2;   // The rate to bring the elite schemes to the next generation
    public static final double MUTATION_RATE = .05; // The rate to mutate an offspring
    public static final Random RANDOM = new Random();

    /**
     * Evaluate the expected value for each scheme. This is a competitive co-evolution fitness function.
     *
     * @param player1 the strategy pool of player 1
     * @param player2 the strategy pool of player 2
     */
    public static void evaluateFitness(Strategy player1, Strategy player2) {
        player1.resetExpectedValues();
        player2.resetExpectedValues();

        for (Scheme scheme1 : player1) {
            for (Scheme scheme2 : player2) {
                int p1Utility = BaselineDriver.utility(scheme1, scheme2);

                double expectedValue = scheme1.getExpectedValue();
                expectedValue += scheme2.getAverageProb() * p1Utility;
                scheme1.setExpectedValue(expectedValue);

                expectedValue = scheme2.getExpectedValue();
                expectedValue += scheme1.getAverageProb() * -p1Utility;
                scheme2.setExpectedValue(expectedValue);
            }
        }

        player1.sort();
        player2.sort();
    }

    /**
     * Evolve the strategy for loser.
     *
     * @param loser        the losing player's strategy pool
     * @param strategySize the size of the strategy
     * @return the evolved strategy for this player
     */
    public static Strategy evolve(Strategy loser, int strategySize) {
        // Use a set to prohibit duplicate schemes
        Set<Scheme> strategySet = new HashSet<>();

        // Copy elites to next generation
        int eliteCount = (int) Math.ceil(ELITISM_RATE * loser.size());
        for (int i = 0; i < eliteCount; i++) {
            strategySet.add(loser.get(i));
        }

        // Use reproduction and mutation to fill the rest of the new strategy
        while (strategySet.size() < strategySize) {
            // Select 2 parent schemes for crossover using tournament selection
            Scheme[] parents = selectParents(loser);

            // Use crossover to produce child schemes
            Scheme child = crossover(parents, loser.getTroopCount());

            // Mutate with probability
            if (RANDOM.nextDouble() < MUTATION_RATE) {
                mutate(child);
            }
            strategySet.add(child);
        }

        return new Strategy(loser, strategySet);
    }

    /**
     * Select two parent schemes to produce a child scheme using tournament selection. First part of reproduction.
     *
     * @param loser the losing player's strategy
     * @return two schemes from loser
     */
    public static Scheme[] selectParents(Strategy loser) {
        final int tournamentSize = (int) Math.max(Math.ceil(ELITISM_RATE * loser.size()), 5);

        TreeSet<Scheme> tournament1 = new TreeSet<>();
        while (tournament1.size() < tournamentSize) {
            tournament1.add(loser.get(RANDOM.nextInt(loser.size())));
        }

        TreeSet<Scheme> tournament2 = new TreeSet<>();
        while (tournament2.size() < tournamentSize) {
            tournament2.add(loser.get(RANDOM.nextInt(loser.size())));
        }

        return new Scheme[]{tournament1.last(), tournament2.last()};
    }

    /**
     * Perform the crossover portion of reproduction.
     *
     * @param parents    the two parent schemes
     * @param troopCount the number of troops for losing player's strategy
     * @return the child scheme
     */
    public static Scheme crossover(Scheme[] parents, int troopCount) {
        return new Scheme(parents, troopCount);
    }

    /**
     * Mutate the scheme by swapping troops between two battlefields.
     *
     * @param scheme the scheme to mutate
     */
    protected static void mutate(Scheme scheme) {
        int battlefield1 = RANDOM.nextInt(scheme.getNumberOfBattlefields()), battlefield2;
        do {
            battlefield2 = RANDOM.nextInt(scheme.getNumberOfBattlefields());
        } while (battlefield2 == battlefield1);
        scheme.swapTroops(battlefield1, battlefield2);
    }
}
