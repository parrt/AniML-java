package us.parr.rf;

import java.util.ArrayList;
import java.util.List;

public class RandomForest {
	/** Number of samples */
	protected int N;

	/** Number of variables to select at random at each decision node to find best split */
	protected int m;

	/** How many trees to create in the forest */
	protected int numEstimators;

	/** The forest of trees */
	protected List<DecisionTree> trees;

	public RandomForest(int numEstimators) {
		this.numEstimators = numEstimators;
		this.trees = new ArrayList<>(numEstimators);
	}

	/** Train numEstimators trees using 2D matrix X -> vector Y as training data,
	 *  where Y_i is the category of the ith feature vector X_i.
	 */
	public void train(List<int[]> X, List<Integer> Y) {
		if ( X==null || X.size()==0 ) return;
		int[] first = X.get(0);
		N = X.size();
		this.m = (int)Math.sqrt(first.length);
	}

	public int classify(int[] unknown) {
		return 0;
	}
}
