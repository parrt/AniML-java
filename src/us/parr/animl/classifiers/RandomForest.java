/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

import us.parr.animl.AniStats;
import us.parr.animl.data.DataTable;

import java.util.ArrayList;
import java.util.List;

import static us.parr.animl.AniStats.majorityVote;

/** A Random Forest classifier operating on categorical and numerical integer
 *  values only. Predicts integer categories only. -1 is an invalid predicted
 *  category value.
 */
public class RandomForest {
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
	public static RandomForest train(DataTable data, int numEstimators) {
		if ( data==null || data.size()==0 ) return null;
		RandomForest forest = new RandomForest(numEstimators);
		forest.N = data.size();
		forest.M = data.get(0).length-1; // last column is predicted var
		// Number of variables to select at random at each decision node to find best split
		int m = (int)Math.sqrt(forest.M);
		for (int i = 1; i<=numEstimators; i++) {
			List<int[]> bootstrap = AniStats.bootstrapWithRepl(data.getRows());
			DataTable table = DataTable.fromInts(bootstrap, data.getColTypes(), data.getColNames());
			DecisionTree tree = DecisionTree.build(table, m);
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

	public DecisionTree getTree(int i) {
		return trees.get(i);
	}
}
