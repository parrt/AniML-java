package us.parr.rf;

import java.util.ArrayList;
import java.util.List;

/** A Random Forest classifier operating on categorical and numerical integer
 *  values only. Predicts integer categories only.
 */
public class RandomForest {
	enum VariableType { CATEGORICAL, NUMERICAL }

	public static final int INVALID_CATEGORY = -1;

	/** Number of samples */
	protected int N;

	/** Number of independent variables in data set */
	protected int M;

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
	 *  where Y_i is the category of the ith feature vector X_i. variables_j
	 *  indicates the {@link VariableType} of variable j.
	 *
	 *  @param X
	 *  @param variables
	 *  @param Y
	 *  @param m
	 */
	public void train(List<int[]> X, int[] variables, List<Integer> Y, int m) {
		if ( X==null || X.size()==0 ) return;
		this.m = m;
		N = X.size();
		M = X.get(0).length;
		this.m = (int)Math.sqrt(M);
	}

	public int classify(int[] unknown) {
		return INVALID_CATEGORY;
	}
}
