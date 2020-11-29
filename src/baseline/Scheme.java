package baseline;

import java.util.*;

public class Scheme implements Comparable<Scheme> {
    private final int[] scheme;
    private transient final Map<Scheme, Integer> sumD; // sumD.get(s) is the sum of difference in it's payoff up to time t
                                                // of not choosing s when they chose this scheme
    private double averageProb;                 // average probability
    private transient double probability;       // current probability
    private transient double expectedValue;     // expected value/fitness

    /**
     * Construct a new scheme with a random allocation of troops.
     *
     * @param numberOfBattlefields the number of battlefields in this instance of Colonel Blotto
     * @param strategySize         the size of strategy that this scheme is in
     * @param troopCount           the number of troops the player can allocate
     */
    public Scheme(int numberOfBattlefields, int strategySize, int troopCount) throws Exception {
        scheme = new int[numberOfBattlefields];
        sumD = new HashMap<>(strategySize);
        probability = 1.0 / strategySize;

        // Randomly allocate the troops
        Random random = new Random();
        int remainingTroops = troopCount;
//        while (remainingTroops-- > 0) {
//            scheme[random.nextInt(numberOfBattlefields)]++;
//        }

        // Randomly allocate the troops V2
        Stack<Integer> battlefieldIndices = createBattlefieldIndexStack();
        while (remainingTroops > 0) {
            int troopsToAllocate = random.nextInt((int) Math.ceil(remainingTroops / 2.) + 1);
            scheme[battlefieldIndices.pop()] += troopsToAllocate;
            remainingTroops -= troopsToAllocate;

            if (battlefieldIndices.isEmpty()) {
                battlefieldIndices = createBattlefieldIndexStack();
            }
        }
        if (Arrays.stream(scheme).sum() > 100) {
            throw new Exception(this + ": " + Arrays.stream(scheme).sum());
        }
    }

    /**
     * Construct a new strategy by the method of crossover.
     *
     * @param parents    the parent strategies
     * @param troopCount the number of troops for losing player's strategy pool
     */
    public Scheme(Scheme[] parents, int troopCount) {
        scheme = new int[parents[0].getNumberOfBattlefields()];
        sumD = new HashMap<>();

        // Stack to hold battlefield indices for random selection
        Stack<Integer> battlefieldIndices = createBattlefieldIndexStack();

        // Randomly choose battlefields from each parent to copy to child up to troop count of player
        Random random = new Random();
        int remainingTroops = troopCount, battlefield = -1;
        for (int i = 0; i < scheme.length && remainingTroops > 0; i++) {
            // Select which parent to take troops from
            Scheme parent = parents[random.nextInt(2)];

            // Select which battlefield to take troops from
            battlefield = battlefieldIndices.pop();

            // Copy troops to battlefield of child scheme
            scheme[battlefield] = parent.getBattlefieldTroops(battlefield);

            remainingTroops -= parent.getBattlefieldTroops(battlefield);
        }

        // Ensure troop count for action is correct. Add or subtract the last battlefield visited
        if (remainingTroops != 0) {
            scheme[battlefield] += remainingTroops;
        }
    }

    /**
     * Create a stack to hold battlefield indices for random selection
     *
     * @return a stack containing randomized battlefield indices
     */
    private Stack<Integer> createBattlefieldIndexStack() {
        Stack<Integer> battlefieldIndices = new Stack<>();
        for (int i = 0; i < scheme.length; i++) {
            battlefieldIndices.push(i);
        }
        Collections.shuffle(battlefieldIndices);
        return battlefieldIndices;
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
     * @param otherScheme another strategy within this player's strategy pool
     * @return the sum of difference in it's payoff up to this timestep of not choosing otherScheme when they chose this scheme
     */
    public int getPayoffDifferenceSum(Scheme otherScheme) {
        return sumD.get(otherScheme);
    }

    /**
     * Update the sum of difference in it's payoff up to this timestep of not choosing otherScheme when they chose this strategy.
     *
     * @param otherScheme another scheme within this player's strategy
     * @param utility     the difference of utility between playing otherScheme or this scheme against the opponent's scheme
     */
    public void updatePayoffDifferenceSum(Scheme otherScheme, int utility) {
        sumD.put(otherScheme, sumD.getOrDefault(otherScheme, 0) + utility);
    }

    /**
     * @return the number of battlefields in this instance of Colonel Blotto
     */
    public int getNumberOfBattlefields() {
        return scheme.length;
    }

    /**
     * @param index the index of the battlefield
     * @return the number of troops on battlefield index
     */
    public int getBattlefieldTroops(int index) {
        return scheme[index];
    }

    /**
     * @return the expected value
     */
    public double getExpectedValue() {
        return expectedValue;
    }

    /**
     * Set the expected value.
     *
     * @param expectedValue the expected value
     */
    public void setExpectedValue(double expectedValue) {
        this.expectedValue = expectedValue;
    }

    /**
     * Swap the number of troops on battlefield1 & battlefield2.
     *
     * @param battlefield1 the index of a battlefield
     * @param battlefield2 the index of another battlefield
     */
    public void swapTroops(int battlefield1, int battlefield2) {
        int temp = scheme[battlefield1];
        scheme[battlefield1] = scheme[battlefield2];
        scheme[battlefield2] = temp;
    }

    /**
     * Move troops to another battlefield
     *
     * @param troopsTo     the battlefield to add troops to
     * @param troopsFrom   the battlefield to take troops from
     * @param troopsToMove the number of troops to move
     */
    public void moveTroops(int troopsTo, int troopsFrom, int troopsToMove) {
        scheme[troopsTo] += troopsToMove;
        scheme[troopsFrom] -= troopsToMove;
    }

    /**
     * Ready the scheme for the next game of Colonel Blotto.
     *
     * @param strategySize the size of strategy that this scheme is in
     */
    public void resetScheme(int strategySize) {
        probability = 1.0 / strategySize;
        sumD.clear();
    }

    @Override
    public int compareTo(Scheme scheme) {
        int compared = Double.compare(expectedValue, scheme.expectedValue);
        if (compared == 0 && !equals(scheme)) {
            int util = baseline.BaselineDriver.utility(this, scheme);
            return util != 0 ? util : 1;
        }
        return compared;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(scheme, ((Scheme) o).scheme);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(scheme);
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder("|");
        for (int battlefieldTroops : scheme) {
            description.append(String.format("%2d", battlefieldTroops)).append("|");
        }
        return description.append(" Prob: ").append(averageProb).toString();
    }
}
