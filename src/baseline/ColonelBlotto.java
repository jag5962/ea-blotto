package baseline;

public class ColonelBlotto {
    public static final int NUMBER_OF_BATTLEFIELDS = 10;
    public static int[] BATTLEFIELD_PAYOFFS = new int[NUMBER_OF_BATTLEFIELDS];

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        for (int i = 0; i < NUMBER_OF_BATTLEFIELDS; ) {
            BATTLEFIELD_PAYOFFS[i] = ++i;
        }
        StrategyPool[] strategyPools = new StrategyPool[]{
                new StrategyPool(200, 100),
                new StrategyPool(100, 100)
        };
        EA.evaluate(strategyPools);
        System.out.println("OldA: " + strategyPools[0]);
        System.out.println("OldB: " + strategyPools[1]);

        final int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            strategyPools = EA.evolve(strategyPools);
            EA.evaluate(strategyPools);
            System.out.print(i + "\r");
        }

        System.out.println("NewA: " + strategyPools[0]);
        System.out.println("NewB: " + strategyPools[1]);
        System.out.println("Fittest A: " + strategyPools[0].getFittest());
        System.out.println("Fittest B: " + strategyPools[1].getFittest());

        long durationInMillis = System.currentTimeMillis() - start;
        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;

        System.out.printf("%02d:%02d.%d", minute, second, millis);
    }
}
