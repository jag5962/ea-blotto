package baseline;

import java.util.HashSet;
import java.util.Random;

public class EA {
    private static final double ELITISM_RATE = .2;
    private static final double MUTATION_RATE = .05;
    private static final Random RANDOM = new Random();

    public static StrategyPool evolve(StrategyPool loser) {
        // Use a set to prohibit duplicate strategies
        HashSet<Strategy> strategySet = new HashSet<>();

        // Number of individuals to copy to next generation
        int eliteCount = (int) Math.ceil(ELITISM_RATE * loser.size());

        // Use reproduction and mutation to fill the new strategy pool after elites are copied over
        Strategy child;
        double eliteFitnessSum = 0, childInitialFitness = -1;
        for (int s = 0; s < loser.size(); s++) {
            // Copy elites
            if (s < eliteCount) {
                strategySet.add(loser.get(s));
                eliteFitnessSum += loser.get(s).getProbability();
            } else {
                // Every child starts with same probability
                if (childInitialFitness == -1) {
                    childInitialFitness = (1 - eliteFitnessSum) / (loser.size() - eliteCount);
                }

                // Guarantee distinct children
                do {
                    // Select 2 parent strategies for crossover using roulette wheel selection
                    Strategy[] parents = selectParents(loser);

                    // Use crossover to produce child strategy
                    child = crossover(parents, loser.getTroopCount(), childInitialFitness);

                    // Mutate with probability
                    if (RANDOM.nextDouble() < MUTATION_RATE) {
                        mutate(child);
                    }
                } while (strategySet.contains(child));
                strategySet.add(child);
            }
        }

        return new StrategyPool(strategySet, loser.getTroopCount());
    }

    private static Strategy[] selectParents(StrategyPool loser) {
        double selector1 = RANDOM.nextDouble(), selector2;
        do {
            selector2 = RANDOM.nextDouble();
        } while (selector1 == selector2);

        int selection1 = -1, selection2 = -1;
        while (selection1 < loser.size() - 1 && selector1 > 0) {
            selector1 -= loser.get(++selection1).getProbability();
        }
        while (selection2 < loser.size() - 1 && selector2 > 0) {
            selector2 -= loser.get(++selection2).getProbability();
        }
        return new Strategy[]{loser.get(selection1), loser.get(selection2)};
    }

    private static Strategy crossover(Strategy[] parents, int troopCount, double initialFitness) {
        return new Strategy(parents, troopCount, initialFitness);
    }

    private static void mutate(Strategy strategy) {
        int battlefield1 = RANDOM.nextInt(ColonelBlotto.NUMBER_OF_BATTLEFIELDS), battlefield2;
        do {
            battlefield2 = RANDOM.nextInt(ColonelBlotto.NUMBER_OF_BATTLEFIELDS);
        } while (battlefield2 == battlefield1);
        strategy.swapTroops(battlefield1, battlefield2);
    }
}
