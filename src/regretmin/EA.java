package regretmin;

import java.util.HashSet;
import java.util.Random;

public class EA {
    private static final double ELITISM_RATE = .05;
    private static final double MUTATION_RATE = .05;

    public static void evolve(Player loser) {
        Random random = new Random();
        int[][] actions = loser.getActions();

        // Not allow duplicates
        HashSet<Action> actionSet = new HashSet<>(actions.length);

        // Number of individuals to copy to next generation
        int eliteCount = (int) Math.ceil(ELITISM_RATE * actions.length);

        // Copy actions with highest probabilities
        double[] prob = loser.getLearnedStrategy();
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
                // Select 2 parent strategies for crossover
                int[][] parents = selectParents(actions);

                // Use crossover to produce child strategy
                child = crossover(parents, loser.getSoldierCount());

                // Mutate with probability
                if (random.nextDouble() < MUTATION_RATE) {
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

    private static int[][] selectParents(int[][] actions) {
        Random random = new Random();
        int bound = (int) Math.ceil(Math.max(2, actions.length * .2));
        int parent1Index = random.nextInt(bound), parent2Index;
        do {
            parent2Index = random.nextInt(bound);
        } while (parent2Index == parent1Index);
        return new int[][]{actions[parent1Index], actions[parent2Index]};
    }

    private static Action crossover(int[][] parents, int soldierCount) {
        int[] action = new int[parents[0].length];
        Random random = new Random();
        // Randomly choose battlefields from each parent to copy to child up to troop count of player
        int remainingSoldiers = soldierCount, battlefield = -1;
        for (int i = 0; i < action.length && remainingSoldiers > 0; i++) {
            // Select which parent to take troops from
            int[] parent = parents[random.nextInt(parents.length)];

            // Select which battlefield to take troops from
            do {
                battlefield = random.nextInt(action.length);
            } while (action[battlefield] > 0);

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
        Random random = new Random();
        int battlefield1 = random.nextInt(action.size()), battlefield2;
        do {
            battlefield2 = random.nextInt(action.size());
        } while (battlefield2 == battlefield1);
        action.swapSoldiers(battlefield1, battlefield2);
    }
}