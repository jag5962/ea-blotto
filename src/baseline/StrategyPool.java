package baseline;

import java.util.*;

public class StrategyPool implements Iterable<Strategy> {
    private final Strategy[] strategyPool;      // Hold all the strategies
    private final int troopCount;               // Used in crossover for child to have same
    private static final Random RANDOM = new Random();

    // Initialize strategy pool of random strategies
    public StrategyPool(int size, int troopCount) {
        HashSet<Strategy> strategySet = new HashSet<>(size);
        while (strategySet.size() < size) {
            Strategy strategy = new Strategy(troopCount, 1.0 / size);
            strategySet.add(strategy);
        }
        strategyPool = strategySet.toArray(new Strategy[0]);
        this.troopCount = troopCount;
    }

    // Construct next generation from previous strategy pool
    public StrategyPool(HashSet<Strategy> strategySet, int troopCount) {
        strategyPool = strategySet.toArray(new Strategy[0]);
        this.troopCount = troopCount;
    }

    public int size() {
        return strategyPool.length;
    }

    public Strategy get(int index) {
        return strategyPool[index];
    }

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

    public int getTroopCount() {
        return troopCount;
    }

    public void resetPlays() {
        for (Strategy strategy : strategyPool) {
            strategy.resetPlay();
        }
    }

    public void calculateFitness() {
        double total = 0, least = Double.MAX_VALUE;
        double[] winPercents = new double[strategyPool.length];

        // Calculate the utility of a strategy per play
        for (int i = 0; i < strategyPool.length; i++) {
            winPercents[i] = strategyPool[i].getPlaysThisRound() > 0 ? strategyPool[i].getUtilityThisRound() / (double) strategyPool[i].getPlaysThisRound() : 0;
            total += winPercents[i];

            // Save lowest win percentage to use for normalization
            if (winPercents[i] < least) {
                least = winPercents[i];
            }
        }

        // If the lowest win percentage is negative, slide them all so lowest = 0
        if (least < 0) {
            total = 0;
            for (int i = 0; i < winPercents.length; i++) {
                winPercents[i] -= least;
                total += winPercents[i];
            }
        }

        // Normalize probabilities [0,1]
        for (int i = 0; i < winPercents.length; i++) {
            strategyPool[i].setProbability(winPercents[i] / total);
        }

        Arrays.sort(strategyPool, Collections.reverseOrder());
    }

    @Override
    public Iterator<Strategy> iterator() {
        return Arrays.stream(strategyPool).iterator();
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        int i = 0;
        for (Strategy strategy : strategyPool) {
            description.append(++i).append(": ").append(strategy);
        }
        return description.toString();
    }
}
