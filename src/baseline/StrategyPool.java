package baseline;

import java.util.*;

public class StrategyPool implements Iterable<Strategy> {
    private static final Random RANDOM = new Random();
    private final Strategy[] strategyPool;  // Holds the strategies in descending order of average probability
    private final int troopCount;           // Used in crossover share with resulting child
    private final int mu;
    private int timestep;                   // The current timestep

    /**
     * Construct a strategy pool of strategies with randomly allocated troops.
     *
     * @param numberOfBattlefields the number of battlefields in this instance of Colonel Blotto
     * @param size                 the number of strategies in the strategy pool
     * @param troopCount           the number of troops the player can allocate
     */
    public StrategyPool(int numberOfBattlefields, int size, int troopCount) {
        Set<Strategy> strategySet = new HashSet<>(size);
        while (strategySet.size() < size) {
            strategySet.add(new Strategy(numberOfBattlefields, size, troopCount));
        }
        strategyPool = strategySet.toArray(new Strategy[0]);
        this.troopCount = troopCount;

        // Suggested that mu >= ([number of strategies] - 1) * (Max difference in utility)
        mu = (size - 1) * (1 - (-1));
    }

    /**
     * Construct a strategy pool from the previous strategy pool.
     *
     * @param loser       the losing player's strategy pool
     * @param strategySet the set of distinct strategies evolved from loser
     */
    public StrategyPool(StrategyPool loser, Set<Strategy> strategySet) {
        strategyPool = strategySet.toArray(new Strategy[0]);
        troopCount = loser.troopCount;
        mu = loser.mu;
        resetStrategies();
    }

    /**
     * @return the number of strategies
     */
    public int size() {
        return strategyPool.length;
    }

    /**
     * @param index the index of the strategy
     * @return the strategy at the index
     */
    public Strategy get(int index) {
        return strategyPool[index];
    }

    /**
     * Choose the next strategy based on their current probability.
     *
     * @return the strategy
     */
    public Strategy getRandom() {
        double selector = RANDOM.nextDouble();
        int selection = 0;
        while (selection < size()) {
            selector -= get(selection).getProbability();
            if (selector <= 0) {
                return get(selection);
            }
            selection++;
        }
        return get(size() - 1);
    }

    /**
     * Update the accumulated regret and probabilities based on the strategies used.
     *
     * @param myStrat    the index of hero's action
     * @param theirStrat the villian's soldier allocation
     * @param utility    the resulting utility from playing these strategies
     */
    public void update(Strategy myStrat, Strategy theirStrat, int utility) {
        timestep++;

        for (Strategy strat : strategyPool) {
            myStrat.updatePayoffDifferenceSum(strat, ColonelBlotto.utility(strat, theirStrat) - utility);
        }

        double sum = 0;
        for (Strategy strat : strategyPool) {
            if (strat != myStrat) {
                int payoffDiffSum = myStrat.getPayoffDifferenceSum(strat);
                strat.setProbability(payoffDiffSum > 0 ? 1.0 / timestep / mu * payoffDiffSum : 0);
                sum += strat.getProbability();
            }
        }

        // Account for double precision error
        if (Math.abs(sum - 1) < .00001) {
            sum = 1;
        }

        if (sum > 1) {
            throw new RuntimeException("Ooops!!!  Need a better mu");
        }
        myStrat.setProbability(1 - sum);

        for (Strategy strat : strategyPool) {
            strat.setAverageProb(((timestep - 1) * strat.getAverageProb() + strat.getProbability()) / timestep);
        }
    }

    /**
     * Reset the expected value for every strategy.
     */
    public void resetExpectedValues() {
        for (Strategy strat : strategyPool) {
            strat.setExpectedValue(0);
        }
    }

    /**
     * Ready the strategy pool for the next game of Colonel Blotto.
     */
    public void resetStrategies() {
        for (Strategy strategy : strategyPool) {
            strategy.resetStrategy(size());
        }
        timestep = 0;
    }

    /**
     * Sort the strategies in descending order by their expected value.
     */
    public void sort() {
        Arrays.sort(strategyPool, Collections.reverseOrder());
    }

    /**
     * @return the number of troops for this player
     */
    public int getTroopCount() {
        return troopCount;
    }

    @Override
    public Iterator<Strategy> iterator() {
        return Arrays.stream(strategyPool).iterator();
    }

    @Override
    public String toString() {
        int i = 0;
        StringBuilder description = new StringBuilder();
        for (Strategy strategy : strategyPool) {
            description.append(++i).append(": ").append(strategy).append("\n");
        }
        return description.toString();
    }
}
