package play;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import baseline.Scheme;
import baseline.Strategy;
import com.google.gson.Gson;

/**
 * This class plays the mixed strategies developed by the different approaches.
 *
 * @author John Gilbertson
 */
public class ColonelBlotto {
    private static final int NUMBER_OF_BATTLEFIELDS = 10;
    private static final double GAMES = 10000;
    private static final Gson GSON = new Gson();
    private static final Random RANDOM = new Random();
    private static Random BASELINE_RANDOM;
    private static Random VARIANT_RANDOM;

    public static void main(String[] args) throws IOException {
        long baselineSeed = System.currentTimeMillis();
        long variantSeed = System.nanoTime();

        String[] approaches = new String[] {"coevolutionA", "coevolutionB", "search"};
        for (String approach : approaches) {
            BASELINE_RANDOM = new Random(baselineSeed);
            VARIANT_RANDOM = new Random(variantSeed);

            long baselineWins = 0, variantWins = 0, ties = 0;
            long baselinePayoff = 0, variantPayoff = 0;
            double baselineUtilTotal = 0, variantUtilTotal = 0;

            for (int i = 0; i < GAMES; i++) {
                // Randomly select a mixed strategy from baseline approach for player 1
                Strategy baseline = getApproach("baseline");

                // Randomly select a mixed strategy from different approach for player 2
                Strategy variant = getApproach(approach);

                // Get a randomly chosen scheme from each approach
                Scheme baselineScheme = getRandom(baseline);
                Scheme variantScheme = getRandom(variant);

                int[] result = play(baselineScheme, variantScheme);
                int baselineUtil = result[0];
                int variantUtil = -baselineUtil;

                baselineUtilTotal += baselineUtil;
                variantUtilTotal += variantUtil;

                baselinePayoff += result[1];
                variantPayoff += result[2];

                if (baselineUtil > variantUtil) {
                    baselineWins++;
                } else if (baselineUtil < variantUtil) {
                    variantWins++;
                } else {
                    ties++;
                }
            }

            System.out.println(System.lineSeparator() + approach);
            System.out.printf("baseline wins: %.2f%%, %s wins: %.2f%%, Ties: %.2f%%%n", baselineWins / GAMES * 100, approach, variantWins / GAMES * 100, ties / GAMES * 100);
            System.out.printf("baseline avg payoff: %.2f, %s avg payoff: %.2f%n", baselinePayoff / GAMES, approach, variantPayoff / GAMES);
            System.out.printf("baseline utility per game: %s, %s utility per game: %s%n", baselineUtilTotal / GAMES, approach, variantUtilTotal / GAMES);
        }
    }

    /**
     * Play player 1's scheme against player 2's scheme
     *
     * @param player1Scheme the scheme for player 1
     * @param player2Scheme the scheme for player 2
     * @return any array of ints. 0: values 1, -1, or 0 if player 1 wins, loses, or ties, respectively. 1: player 1's payoff. 2: player 2's payoff.
     */
    public static int[] play(Scheme player1Scheme, Scheme player2Scheme) {
        int player1Score = 0, player2Score = 0;
        for (int i = 0; i < NUMBER_OF_BATTLEFIELDS; i++) {
            if (player1Scheme.getBattlefieldTroops(i) > player2Scheme.getBattlefieldTroops(i)) {
                player1Score += i + 1;
            } else if (player1Scheme.getBattlefieldTroops(i) < player2Scheme.getBattlefieldTroops(i)) {
                player2Score += i + 1;
            }
        }
        return new int[]{Integer.compare(player1Score, player2Score), player1Score, player2Score};
    }

    /**
     * Choose the next scheme based on their average probability from regret-matching.
     *
     * @return the scheme
     */
    private static Scheme getRandom(Strategy mixedStrategy) {
        double selector = RANDOM.nextDouble();
        int selection = 0;
        while (selection < mixedStrategy.size()) {
            selector -= mixedStrategy.get(selection).getAverageProb();
            if (selector <= 0) {
                return mixedStrategy.get(selection);
            }
            selection++;
        }
        return mixedStrategy.get(mixedStrategy.size() - 1);
    }

    /**
     * Get a mixed strategy created by the supplied approach within the strategies directory.
     * @param directory where the JSON files for the strategies are stored for an approach
     * @return one of the mixed strategies from an approach
     * @throws IOException if an I/O error occurs opening the file
     */
    private static Strategy getApproach(String directory) throws IOException {
        int selection = directory.equalsIgnoreCase("baseline") ? BASELINE_RANDOM.nextInt(100) : VARIANT_RANDOM.nextInt(100);
        String json, fileName = String.format("strategies/%s/%d.json", directory, selection);

        try (Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
            json = stream.collect(Collectors.joining(System.lineSeparator()));
        }
        return GSON.fromJson(json, Strategy.class);
    }
}
