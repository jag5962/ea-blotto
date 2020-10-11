package baseline;

import java.util.HashSet;
import java.util.Random;

public class EA {
    private static final double ELITISM_RATE = .05;
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
                        // Select 2 parent strategies for crossover
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
        int parent1Index = random.nextInt(5), parent2Index;
        do {
            parent2Index = random.nextInt(5);
        } while (parent2Index == parent1Index);
        return new Strategy[]{strategyPool.get(parent1Index), strategyPool.get(parent2Index)};
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
