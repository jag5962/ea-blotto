package baseline;

import java.util.*;

public class Strategy implements Comparable<Strategy> {
    private final int[] strategy;
    private final Map<Strategy, Integer> sumD;  // sumD.get(s) is the sum of difference in it's payoff up to time t
                                                // of not choosing s when they chose this strategy
    private double probability;                 // current probability
    private double averageProb;                 // average probability

    /**
     * Construct a new strategy with a random allocation of troops.
     *
     * @param numberOfBattlefields the number of battlefields in this instance of Colonel Blotto
     * @param poolSize             the size of strategy pool that this strategy is in
     * @param troopCount           the number of troops the player can allocate
     */
    public Strategy(int numberOfBattlefields, int poolSize, int troopCount) {
        strategy = new int[numberOfBattlefields];
        sumD = new HashMap<>(poolSize);
        probability = 1.0 / poolSize;

        // Randomly allocate the troops
        Random random = new Random();
        int remainingTroops = troopCount;
        while (remainingTroops-- > 0) {
            strategy[random.nextInt(numberOfBattlefields)]++;
        }
    }

    /**
     * Construct a new strategy by the method of crossover.
     *
     * @param parents the parent strategies
     * @param loser   the losing player's strategy pool
     */
    public Strategy(Strategy[] parents, StrategyPool loser) {
        strategy = new int[parents[0].getNumberOfBattlefields()];
        sumD = new HashMap<>(loser.size());

        // Stack to hold battlefield indices for random selection
        Stack<Integer> battlefieldIndices = new Stack<>();
        for (int i = 0; i < strategy.length; i++) {
            battlefieldIndices.push(i);
        }
        Collections.shuffle(battlefieldIndices);

        // Randomly choose battlefields from each parent to copy to child up to troop count of player
        Random random = new Random();
        int remainingTroops = loser.getTroopCount(), battlefield = -1;
        for (int i = 0; i < strategy.length && remainingTroops > 0; i++) {
            // Select which parent to take troops from
            Strategy parent = parents[random.nextInt(2)];

            // Select which battlefield to take troops from
            battlefield = battlefieldIndices.pop();

            // Copy troops to battlefield of child strategy
            strategy[battlefield] = parent.getBattlefieldTroops(battlefield);

            remainingTroops -= parent.getBattlefieldTroops(battlefield);
        }

        // Ensure troop count for action is correct. Add or subtract the last battlefield visited
        if (remainingTroops != 0) {
            strategy[battlefield] += remainingTroops;
        }
    }

    /**
     * @return the current probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Set the current probability.
     *
     * @param probability the current probability
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * @return the average probability
     */
    public double getAverageProb() {
        return averageProb;
    }

    /**
     * Set the average probability
     *
     * @param averageProb the average probability
     */
    public void setAverageProb(double averageProb) {
        this.averageProb = averageProb;
    }

    /**
     * @param anotherStrat another strategy within this player's strategy pool
     * @return the sum of difference in it's payoff up to this timestep of not choosing anotherStrat when they chose this strategy
     */
    public int getPayoffDifferenceSum(Strategy anotherStrat) {
        return sumD.get(anotherStrat);
    }

    /**
     * Update the sum of difference in it's payoff up to this timestep of not choosing anotherStrat when they chose this strategy.
     *
     * @param anotherStrat another strategy within this player's strategy pool
     * @param utility      the difference of utility between playing anotherStrat or this strategy against the opponent's strategy
     */
    public void updatePayoffDifferenceSum(Strategy anotherStrat, int utility) {
        sumD.put(anotherStrat, sumD.getOrDefault(anotherStrat, 0) + utility);
    }

    /**
     * @return the number of battlefields in this instance of Colonel Blotto
     */
    public int getNumberOfBattlefields() {
        return strategy.length;
    }

    /**
     * @param index the index of the battlefield
     * @return the number of troops on battlefield index
     */
    public int getBattlefieldTroops(int index) {
        return strategy[index];
    }

    /**
     * Swap the number of troops on battlefield1 & battlefield2.
     *
     * @param battlefield1 the index of a battlefield
     * @param battlefield2 the index of another battlefield
     */
    public void swapTroops(int battlefield1, int battlefield2) {
        int temp = strategy[battlefield1];
        strategy[battlefield1] = strategy[battlefield2];
        strategy[battlefield2] = temp;
    }

    /**
     * Ready the strategy for the next game of Colonel Blotto.
     *
     * @param poolSize the size of strategy pool that this strategy is in
     */
    public void resetStrategy(int poolSize) {
        probability = 1.0 / poolSize;
        sumD.clear();
    }

    @Override
    public int compareTo(Strategy strategy) {
        return Double.compare(averageProb, strategy.averageProb);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(strategy, ((Strategy) o).strategy);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(strategy);
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder("|");
        for (int battlefieldTroops : strategy) {
            description.append(String.format("%02d", battlefieldTroops)).append("|");
        }
        return description.append(" Prob: ").append(averageProb).toString();
    }
}
