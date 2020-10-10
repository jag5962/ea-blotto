package regretmin;

public class GameBlotto {

    /**
     * @param myAction    action played by hero
     * @param otherAction action played by villain
     * @return utility for the player of interest
     */
    public static int utility(int[] myAction, int[] otherAction) {
        int myScore = 0;
        int otherScore = 0;
        for (int i = 0; i < myAction.length; i++) {
            if (myAction[i] > otherAction[i])
                myScore += i + 1;
            else if (myAction[i] < otherAction[i])
                otherScore += i + 1;
        }

        if (myScore > otherScore)
            return 1;
        else if (myScore < otherScore)
            return -1;
        else
            return 0;
    }

    /**
     * Provide a description of an action
     *
     * @param action the integers representing the action
     * @return the description of the action
     */
    public static String actionAsString(int[] action) {
        StringBuilder sb = new StringBuilder();
        sb.append(action[0]);
        for (int i = 1; i < action.length; i++) {
            sb.append(" ");
            sb.append(action[i]);
        }
        return sb.toString();
    }
}
