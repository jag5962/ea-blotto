package baseline;

import java.util.HashSet;
import java.util.Random;

public class EA {
    private static final double ELITISM_RATE = .2;
    private static final double MUTATION_RATE = .05;
    private static Random random = new Random();

    public static void evaluate(StrategyPool[] strategyPools) {
        // Calculate every strategy's utility
        strategyPools[0].calculateUtility(strategyPools[1]);
        strategyPools[1].calculateUtility(strategyPools[0]);
    }

    public static StrategyPool[] evolve(StrategyPool[] strategyPools) {
        StrategyPool[] newStrategyPools = new StrategyPool[strategyPools.length];

        for (int i = 0; i < strategyPools.length; i++) {
            // Use a set to prohibit duplicate strategies
            HashSet<Strategy> strategySet = new HashSet<>();

            // Number of individuals to copy to next generation
            int eliteCount = (int) Math.ceil(ELITISM_RATE * strategyPools[i].size());

            // Use reproduction and mutation to fill the new strategy pool after elites are copied over
            Strategy child;
            for (int j = 0; j < strategyPools[i].size(); j++) {
                // Copy elites
                if (j < eliteCount) {
                    strategySet.add(strategyPools[i].get(j));
                } else {
                    do {
                        // Select 2 parent strategies for crossover using roulette wheel selection
                        Strategy[] parents = selectParents(strategyPools[i]);

                        // Use crossover to produce child strategy
                        child = crossover(parents, strategyPools[i].getTroopCount());

                        // Mutate with probability
                        if (random.nextDouble() < MUTATION_RATE) {
                            mutation(child);
                        }
                    } while (strategySet.contains(child));
                    strategySet.add(child);
                }
            }

            newStrategyPools[i] = new StrategyPool(strategySet, strategyPools[i].getTroopCount());
        }
        return newStrategyPools;
    }

    private static Strategy[] selectParents(StrategyPool strategyPool) {
        // Summation of this strategy pool's utilities
        double positiveUtilitySum = strategyPool.getPositiveUtilitySum();

        // Prepare mapping of crossover probabilities
        double indexBound1 = random.nextDouble(), indexBound2;
        do {
            indexBound2 = random.nextDouble();
        } while (indexBound1 == indexBound2);

        double firstSelector = Math.min(indexBound1, indexBound2);
        double secondSelector = Math.max(indexBound1, indexBound2);

        // Give actions with higher probabilities a great chance to be selected
        double sum = 0;
        int firstSelected = -1, secondSelected = -1;
        while (sum < secondSelector) {
            sum += strategyPool.get(++secondSelected).getUtility() / positiveUtilitySum;

            // Save lower indexed parent strategy without stopping loop
            if (firstSelected == -1 && sum >= firstSelector) {
                firstSelected = secondSelected;
            }
        }
        return new Strategy[]{strategyPool.get(firstSelected), strategyPool.get(secondSelected)};
    }

    private static Strategy crossover(Strategy[] parents, int troopCount) {
        return new Strategy(parents, troopCount);
    }

    private static void mutation(Strategy strategy) {
        int battlefield1 = random.nextInt(ColonelBlotto.NUMBER_OF_BATTLEFIELDS), battlefield2;
        do {
            battlefield2 = random.nextInt(ColonelBlotto.NUMBER_OF_BATTLEFIELDS);
        } while (battlefield2 == battlefield1);
        strategy.swapTroops(battlefield1, battlefield2);
    }
}
