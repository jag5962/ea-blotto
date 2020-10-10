package regretmin;

import java.util.Random;

public class StrategyHMC {
	private double[][] sumD_t_i; // sumD_t_i[j][k] is the sum of difference in it's payoff up to time t of
								 // not choosing k when they chose j
	private double[] p_t_i; 	 // current strategy
	private double[] p_ave;		 // average strategy
	private int t;				 // the current timestep
	private double mu;
	private int[][] actions;
	private Random random = new Random();
	

	/**
	 * Constructor
	 * 
	 * @param actions - the array of soldier allocations in the player's strategy
	 */
	public StrategyHMC(int[][] actions) {
		sumD_t_i = new double[actions.length][actions.length];
		p_t_i = new double[actions.length];
		for (int i = 0; i < p_t_i.length; i++) {
			p_t_i[i] = 1.0 / p_t_i.length;
		}
		p_ave = new double[actions.length];
		t = 0;
		
		//Suggested that mu >= ([number of strategies] - 1) * (Max difference in utility)
		mu = (sumD_t_i.length - 1) *  (1 - (-1));
		this.actions = actions;
	}

	/**
	 * Choose the next action based on the current strategy
	 * @return the action
	 */
	public int chooseActionIndex() {
		double selector = random.nextDouble();
		int ndx = 0;
		while (ndx < p_t_i.length) {
			selector -=  p_t_i[ndx];
			if (selector <= 0) return ndx;
			ndx++;
		}
		return p_t_i.length - 1;
	}

	/** 
	 * Update the accumulated regret and strategies based on the actions taken
	 * 
	 * @param myIndex the index of hero's action
	 * @param otherAction the villian's soldier allocation
	 */
	public void update(int myIndex, int[] otherAction) {
		t++;
		int utility = GameBlotto.utility(actions[myIndex], otherAction);
		for (int a = 0; a < sumD_t_i.length; a++) {
			sumD_t_i[myIndex][a] += GameBlotto.utility(actions[a], otherAction) - utility;
		}
		
		double sum = 0;
		for (int a = 0; a < p_t_i.length; a++) {
			if (a != myIndex) {
				if (sumD_t_i[myIndex][a] > 0) {
					p_t_i[a] = 1.0/ t / mu * sumD_t_i[myIndex][a];
				}
				else {
					p_t_i[a] = 0;
				}
				sum += p_t_i[a];
			}
		}
		if (sum > 1) throw new RuntimeException("Ooops!!!  Need a better mu"); 
		p_t_i[myIndex] = 1 - sum;
		
		for (int a = 0; a < p_t_i.length; a++) {
			p_ave[a] = ((t-1) * p_ave[a] + p_t_i[a]) / t;
		}
	}


	/**
	 * Get an allocation of soldiers from the strategy
	 * @param index the index into the array of possible allocations
	 * @return the allocation of soldiers
	 */
	public int[] getAction(int index) {
		return actions[index];
	}

	/** 
	 * 
	 * @return all allocations for this strategy
	 */
	public int[][] getActions() {
		return actions;
	}
	
	/**
	 * Return the average regret-based strategy, which 
	 * converges to an equilibrium strategy
	 * 
	 * @return array of probabilities for the strategy
	 */
	public double[] getStrategy() {
		return p_ave;
	}
}
