package baseline;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.Random;

public class Strategy implements Comparable<Strategy> {
    private final int[] strategy;
    private int utility;

    // Construct new strategy with random disbursement of troops
    public Strategy(int troopCount) {
        strategy = new int[ColonelBlotto.NUMBER_OF_BATTLEFIELDS];

        // Randomly disburse the troops
        Random random = new Random();
        int remainingTroops = troopCount;
        while (remainingTroops-- > 0) {
            strategy[random.nextInt(ColonelBlotto.NUMBER_OF_BATTLEFIELDS)]++;
        }
    }

    // Crossover parents to create child
    public Strategy(Strategy[] parents, int troopCount) {
        strategy = new int[ColonelBlotto.NUMBER_OF_BATTLEFIELDS];

        // Stack to hold battlefield indices for random selection
        Stack<Integer> battlefieldIndices = new Stack<>();
        for (int i = 0; i < strategy.length; i++) {
            battlefieldIndices.push(i);
        }
        Collections.shuffle(battlefieldIndices);

        // Randomly choose battlefields from each parent to copy to child up to troop count of player
        Random random = new Random();
        int remainingTroops = troopCount, battlefield = -1;
        for (int i = 0; i < strategy.length && remainingTroops > 0; i++) {
            // Select which parent to take troops from
            Strategy parent = parents[random.nextInt(parents.length)];

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

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public int getUtility() {
        return utility;
    }

    public int getBattlefieldTroops(int index) {
        return strategy[index];
    }

    public int getBattleCount() {
        return strategy.length;
    }

    public void swapTroops(int battlefield1, int battlefield2) {
        int temp = strategy[battlefield1];
        strategy[battlefield1] = strategy[battlefield2];
        strategy[battlefield2] = temp;
    }

    @Override
    public int compareTo(Strategy strategy) {
        return Integer.compare(utility, strategy.utility);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strategy strategy1 = (Strategy) o;
        return Arrays.equals(strategy, strategy1.strategy);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(strategy);
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder("|");
        for (int battlefieldTroops : strategy) {
            description.append(battlefieldTroops).append("|");
        }
        return description.toString();
    }
}
