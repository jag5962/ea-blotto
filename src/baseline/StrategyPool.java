package baseline;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

public class StrategyPool implements Iterable<Strategy> {
    private final Strategy[] strategyPool;
    private final int troopCount;
    private double opponentStrategyPoolSize;

    // Initialize strategy pool of random strategies
    public StrategyPool(int size, int troopCount) {
        HashSet<Strategy> strategySet = new HashSet<>(size);
        while (strategySet.size() < size) {
            strategySet.add(new Strategy(troopCount));
        }
        strategyPool = strategySet.toArray(new Strategy[0]);
        this.troopCount = troopCount;
    }

    // Construct next generation for previous strategy pool
    public StrategyPool(HashSet<Strategy> strategySet, int troopCount) {
        strategyPool = strategySet.toArray(new Strategy[0]);
        this.troopCount = troopCount;
    }

    // Calculate fitness for every strategy
    public void calculateUtility(StrategyPool opponentStrategyPool) {
        // Calculate average payoff for every strategy in this pool
        for (Strategy thisStrategy : this) {
            int utility = 0;

            // Compare strategy to every one of the opponent's strategies
            for (Strategy opponentStrategy : opponentStrategyPool) {
                int thisStrategyPayoff = 0, opponentStrategyPayoff = 0;
                // Compare troops on each battlefield
                for (int battlefield = 0; battlefield < opponentStrategy.getBattleCount(); battlefield++) {
                    int thisStrategyTroops = thisStrategy.getBattlefieldTroops(battlefield);
                    int opponentTroops = opponentStrategy.getBattlefieldTroops(battlefield);
                    if (thisStrategyTroops > opponentTroops) {
                        thisStrategyPayoff += ColonelBlotto.BATTLEFIELD_PAYOFFS[battlefield];
                    } else if (thisStrategyTroops < opponentTroops) {
                        opponentStrategyPayoff += ColonelBlotto.BATTLEFIELD_PAYOFFS[battlefield];
                    }
                }

                // If this strategy wins, this strategy gets 1, else loses 1
                if (thisStrategyPayoff > opponentStrategyPayoff) {
                    utility++;
                } else if (thisStrategyPayoff < opponentStrategyPayoff) {
                    utility--;
                }
            }
            thisStrategy.setUtility(utility);
        }
        Arrays.sort(strategyPool, Comparator.reverseOrder());
        opponentStrategyPoolSize = opponentStrategyPool.size();
    }

    public int size() {
        return strategyPool.length;
    }

    public Strategy get(int index) {
        return strategyPool[index];
    }

    public Strategy getFittest() {
        return strategyPool[0];
    }

    public int getTroopCount() {
        return troopCount;
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
            description.append(++i).append(": ").append(strategy).append(" Utility: ")
                    .append(strategy.getUtility() / opponentStrategyPoolSize).append("\n");
        }
        return description.toString();
    }
}
