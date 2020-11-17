package baseline;

import java.util.*;

public class Strategy implements Iterable<Scheme> {
    private static final Random RANDOM = new Random();
    private Scheme[] strategy;    // Holds the schemes in descending order of expected payoff
    private final int troopCount; // Used in crossover share with resulting child
    private int mu;
    private int timestep;         // The current timestep

    /**
     * Construct a strategy of schemes with randomly allocated troops.
     *
     * @param numberOfBattlefields the number of battlefields in this instance of Colonel Blotto
     * @param size                 the number of schemes in the strategy
     * @param troopCount           the number of troops the player can allocate
     */
    public Strategy(int numberOfBattlefields, int size, int troopCount) {
        Set<Scheme> strategySet = new HashSet<>(size);
        while (strategySet.size() < size) {
            strategySet.add(new Scheme(numberOfBattlefields, size, troopCount));
        }
        strategy = strategySet.toArray(new Scheme[0]);
        this.troopCount = troopCount;

        // Suggested that mu >= ([number of schemes] - 1) * (Max difference in utility)
        mu = (size - 1) * (1 - (-1));
    }

    /**
     * Construct a strategy from the previous strategy.
     *
     * @param loser       the losing player's strategy
     * @param strategySet the set of distinct schemes evolved from loser
     */
    public Strategy(Strategy loser, Set<Scheme> strategySet) {
        strategy = strategySet.toArray(new Scheme[0]);
        troopCount = loser.troopCount;
        mu = (strategySet.size() - 1) * (1 - (-1));
        resetStrategy();
    }

    /**
     * @return the number of schemes
     */
    public int size() {
        return strategy.length;
    }

    /**
     * @param index the index of the scheme
     * @return the scheme at the index
     */
    public Scheme get(int index) {
        return strategy[index];
    }

    /**
     * Choose the next scheme based on their current probability.
     *
     * @return the scheme
     */
    public Scheme getRandom() {
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
     * Update the accumulated regret and probabilities based on the schemes used.
     *
     * @param myScheme    the index of hero's action
     * @param theirScheme the enemy's soldier allocation
     * @param utility     the resulting utility from playing these schemes
     */
    public void update(Scheme myScheme, Scheme theirScheme, int utility) {
        timestep++;

        for (Scheme scheme : strategy) {
            myScheme.updatePayoffDifferenceSum(scheme, BaselineDriver.utility(scheme, theirScheme) - utility);
        }

        double sum = 0;
        for (Scheme scheme : strategy) {
            if (scheme != myScheme) {
                int payoffDiffSum = myScheme.getPayoffDifferenceSum(scheme);
                scheme.setProbability(payoffDiffSum > 0 ? 1.0 / timestep / mu * payoffDiffSum : 0);
                sum += scheme.getProbability();
            }
        }

        // Account for double precision error
        if (Math.abs(sum - 1) < .00001) {
            sum = 1;
        }

        if (sum > 1) {
            throw new RuntimeException("Ooops!!!  Need a better mu");
        }
        myScheme.setProbability(1 - sum);

        for (Scheme scheme : strategy) {
            scheme.setAverageProb(((timestep - 1) * scheme.getAverageProb() + scheme.getProbability()) / timestep);
        }
    }

    /**
     * Reset the expected value for every scheme.
     */
    public void resetExpectedValues() {
        for (Scheme scheme : strategy) {
            scheme.setExpectedValue(0);
        }
    }

    /**
     * Ready the strategy for the next game of Colonel Blotto.
     */
    public void resetStrategy() {
        for (Scheme scheme : strategy) {
            scheme.resetScheme(size());
        }
        timestep = 0;
    }

    /**
     * Sort the schemes in descending order by their expected value.
     */
    public void sort() {
        Arrays.sort(strategy, Collections.reverseOrder());
    }

    /**
     * @return the number of troops for this player
     */
    public int getTroopCount() {
        return troopCount;
    }

    /**
     * Reduce the size of the strategy if it contains schemes with 0 average probability.
     */
    public void adjustSize() {
        List<Scheme> strategyList = new ArrayList<>();
        for (Scheme scheme : strategy) {
            if (scheme.getAverageProb() > 0) {
                strategyList.add(scheme);
            }
        }
        if (strategyList.size() != strategy.length) {
            strategy = strategyList.toArray(new Scheme[0]);
            mu = (strategy.length - 1) * (1 - (-1));
        }
    }

    @Override
    public Iterator<Scheme> iterator() {
        return Arrays.stream(strategy).iterator();
    }

    @Override
    public String toString() {
        int i = 0;
        StringBuilder description = new StringBuilder();
        for (Scheme scheme : strategy) {
            description.append(++i).append(": ").append(scheme).append(System.lineSeparator());
        }
        return description.toString();
    }
}
