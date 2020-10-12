package regretmin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;

public class EA {
    private static final double ELITISM_RATE = .2;
    private static final double MUTATION_RATE = .05;

    public static void evolve(Player loser) {
        int[][] actions = loser.getActions();

        // Not allow duplicates
        HashSet<Action> actionSet = new HashSet<>(actions.length);

        // Number of individuals to copy to next generation
        int eliteCount = (int) Math.ceil(ELITISM_RATE * actions.length);

        // Copy actions with highest probabilities
        double[] prob = loser.getLearnedStrategy().clone();
        for (int i = 0; i < eliteCount; i++) {
            double highest = -1;
            int highestIndex = -1;
            for (int p = 0; p < prob.length; p++) {
                if (prob[p] > highest) {
                    highest = prob[p];
                    highestIndex = p;
                }
            }
            actionSet.add(new Action(actions[highestIndex]));
            prob[highestIndex] = -1;
        }

        // Use reproduction and mutation to fill the new actions after elites are copied over
        for (int j = eliteCount; j < actions.length; j++) {
            Action child;
            do {
                // Select 2 parent strategies for crossover using Roulette wheel selection
                int[][] parents = selectParents(actions, loser.getLearnedStrategy());

                // Use crossover to produce child strategy
                child = crossover(parents, loser.getSoldierCount());

                // Mutate with probability
                if (Driver.prng.nextDouble() < MUTATION_RATE) {
                    mutation(child);
                }
            } while (actionSet.contains(child));
            actionSet.add(child);
        }

        int i = 0;
        for (Action action : actionSet) {
            actions[i++] = action.getArray();
        }
    }

    private static int[][] selectParents(int[][] actions, double[] learnedStrategy) {
        // Prepare mapping of crossover probabilities
        double indexBound1 = Driver.prng.nextDouble(), indexBound2;
        do {
            indexBound2 = Driver.prng.nextDouble();
        } while (indexBound1 == indexBound2);

        double firstSelector = Math.min(indexBound1, indexBound2);
        double secondSelector = Math.max(indexBound1, indexBound2);

        // Give actions with higher probabilities a great chance to be selected
        double sum = 0;
        int firstSelected = -1, secondSelected = -1;
        while (sum < secondSelector) {
            sum += learnedStrategy[++secondSelected];

            // Save lower indexed parent strategy without stopping loop
            if (firstSelected == -1 && sum >= firstSelector) {
                firstSelected = secondSelected;
            }
        }
        return new int[][]{actions[firstSelected], actions[secondSelected]};
    }

    private static Action crossover(int[][] parents, int soldierCount) {
        int[] action = new int[parents[0].length];

        // Stack to hold battlefield indices for random selection
        Stack<Integer> battlefieldIndices = new Stack<>();
        for (int i = 0; i < action.length; i++) {
            battlefieldIndices.push(i);
        }
        Collections.shuffle(battlefieldIndices);

        // Randomly choose battlefields from each parent to copy to child up to troop count of player
        int remainingSoldiers = soldierCount, battlefield = -1;
        for (int i = 0; i < action.length && remainingSoldiers > 0; i++) {
            // Select which parent to take troops from
            int[] parent = parents[Driver.prng.nextInt(parents.length)];

            // Select which battlefield to take troops from
            battlefield = battlefieldIndices.pop();

            // Copy troops to battlefield of child strategy
            action[battlefield] = parent[battlefield];

            remainingSoldiers -= parent[battlefield];
        }
        // Ensure soldier count for action is correct. Add or subtract the last battlefield visited
        if (remainingSoldiers != 0) {
            action[battlefield] += remainingSoldiers;
        }
        return new Action(action);
    }

    private static void mutation(Action action) {
        int battlefield1 = Driver.prng.nextInt(action.size()), battlefield2;
        do {
            battlefield2 = Driver.prng.nextInt(action.size());
        } while (battlefield2 == battlefield1);
        action.swapSoldiers(battlefield1, battlefield2);
    }
}
