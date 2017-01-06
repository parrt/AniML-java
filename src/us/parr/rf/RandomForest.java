package us.parr.rf;

import us.parr.rf.misc.RFUtils;

import java.util.ArrayList;
import java.util.List;

import static us.parr.rf.misc.RFUtils.majorityVote;

/** A Random Forest classifier operating on categorical and numerical integer
 *  values only. Predicts integer categories only. -1 is an invalid predicted
 *  category value.
 */
public class RandomForest {
	enum VariableType { CATEGORICAL, NUMERICAL }

	public static final int INVALID_CATEGORY = -1;

	/** Number of samples */
	protected int N;

	/** Number of independent variables in data set */
	protected int M;

	/** How many trees to create in the forest */
	protected int numEstimators;

	/** The forest of trees */
	protected List<DecisionTree> trees;

	public RandomForest(int numEstimators) {
		this.numEstimators = numEstimators;
		this.trees = new ArrayList<>(numEstimators);
	}

	/** Train numEstimators trees using 2D matrix data as training
	 *  where last column in data is the category of the ith feature vector data_i.
	 */
	public static RandomForest train(List<int[]> data, int numEstimators) {
		if ( data==null || data.size()==0 ) return null;
		RandomForest forest = new RandomForest(numEstimators);
		forest.N = data.size();
		forest.M = data.get(0).length-1; // last column is predicted var
		// Number of variables to select at random at each decision node to find best split
		int m = (int)Math.sqrt(forest.M);
		for (int i = 1; i<=numEstimators; i++) {
			List<int[]> bootstrap = RFUtils.bootstrapWithRepl(data);
			DecisionTree tree = DecisionTree.build(bootstrap, null, m);
			forest.trees.add(tree);
		}
		return forest;
	}

	public int classify(int[] unknown) {
		if ( unknown==null ) {
			return INVALID_CATEGORY;
		}
		List<Integer> predictions = new ArrayList<>();
		for (int i = 0; i<numEstimators; i++) {
			predictions.add( trees.get(i).classify(unknown) );
		}
		return majorityVote(predictions);
	}
}
