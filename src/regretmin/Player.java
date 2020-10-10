package regretmin;

public class Player {
    private StrategyHMC strategy;
    private final int soldierCount;

    /**
     * Constructor
     *
     * @param actions The array of allocations in the strategy
     */
    public Player(int[][] actions, int soldierCount) {
        this.strategy = new StrategyHMC(actions);
        this.soldierCount = soldierCount;
    }

    /**
     * Reset the strategy either because we have a new set of
     * actions, or because we have a new opponent
     *
     * @param actions The array of allocations in the new strategy
     */
    public void resetStrategy(int[][] actions) {
        this.strategy = new StrategyHMC(actions);
    }

    /**
     * Get the index of the action for the player
     *
     * @return an index into the array of actions
     */
    public int chooseAction() {
        return strategy.chooseActionIndex();
    }

    /**
     * Return the allocation of soldiers at element index of array
     *
     * @param index the index into the array of allocations
     * @return the allocation
     */
    public int[] getAllocation(int index) {
        return strategy.getAction(index);
    }

    /**
     * Update the average strategy and the regret for taking
     * the action at index myAction, given the action chosen by
     * the villian
     *
     * @param myActionIndex the index of my action
     * @param otherAction   the allocation of soldiers used by the villain
     */
    public void update(int myActionIndex, int[] otherAction) {
        strategy.update(myActionIndex, otherAction);
    }

    /**
     * @return the equilibrium strategy
     */
    public double[] getLearnedStrategy() {
        return strategy.getStrategy();
    }

    /**
     * @return the allocations in the strategy
     */
    public int[][] getActions() {
        return strategy.getActions();
    }

    /**
     * @return the number of soldiers for this player
     */
    public int getSoldierCount() {
        return soldierCount;
    }
}
