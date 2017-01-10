/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

import us.parr.animl.AniStats;
import us.parr.animl.data.DataPair;
import us.parr.animl.data.DataTable;
import us.parr.animl.data.FrequencySet;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static us.parr.animl.AniStats.sum;

/** A classic CART decision tree but this implementation is suitable just for
 *  classification, not regression. I extended it to handle a subset of predictor
 *  variables at each node to support random forest construction.
 */
public abstract class DecisionTree {
	public static final int SEED = 777111333; // need randomness but use same seed to get reproducibility
	public static final Random random = new Random(SEED);
	public static final int INVALID_CATEGORY = -1;

	protected static class BestInfo {
		public double gain = 0.0;
		public int var = -1;
		public int val = 0;
		public DataPair split = null;

		public BestInfo() { }

		public BestInfo(double gain, int var, int val, DataPair split) {
			this.gain = gain;
			this.var = var;
			this.val = val;
			this.split = split;
		}
	}

	public static final boolean debug = false;

	/** This tree was created from which data table? */ // TODO try to remove this ref. it'll keep all that data from being GC'd
	protected DataTable data;

	// for debugging, fields below
	protected int numRecords;
	protected double entropy;

	public DecisionTree() {
	}

	public abstract int classify(int[] X);

	/** Conversion routine from separate X -> Y vectors to single augmented data vector */
//	public static DecisionTree build(List<int[]> X, List<Integer> Y) {
//		List<int[]> data = new ArrayList<>(X.size());
//		for (int i = 0; i<X.size(); i++) {
//			int[] row = X.get(i);
//			int[] augmented = new int[row.length+1];
//			System.arraycopy(row, 0, augmented, 0, row.length);
//			augmented[row.length] = Y.get(i);
//			data.add(augmented);
//		}
//		return build(data);
//	}

	public static DecisionTree build(DataTable data) {
		return build(data, 0, 1);
	}

	/** Build a decision tree starting with arg data and recursively
	 *  build up children. data_i is the ith observation and the last column of
	 *  data is the predicted (dependent) variable.  Keeping the data together
	 *  makes it easier to implement since splitting a set splits both
	 *  features and predicted variables.
	 *
	 *  If m>0, select split var from random subset of size m from all variable set.
	 */
	public static DecisionTree build(DataTable data, int m, int leafSize) {
		if ( data==null || data.size()==0 ) return null;
		int N = data.size();
		int yi = data.getPredictedCol(); // last index is usually the target variable
		// if all predict same category or only one row of data,
		// create leaf predicting that
		FrequencySet<Integer> completePredictionCounts = data.valueCountsInColumn(yi);
		double complete_entropy = AniStats.entropy(completePredictionCounts.counts());
		Set<Integer> predictions = data.uniqueValues(yi);
		if ( predictions.size()==1 || data.size()<=leafSize ) {
			DecisionTree t = new DecisionLeafNode(predictions.iterator().next(), yi);
			t.numRecords = N;
			t.entropy = complete_entropy;
			t.data = data;
			return t;
		}

		if ( debug ) System.out.printf("entropy of all %d values = %.2f\n", N, complete_entropy);
		BestInfo best = new BestInfo();
		// Non-random forest decision trees do just: for (int i=0; i<M; i++) {
		// but RF must use a subset m << M of predictor variables so this is
		// a generalization
		List<Integer> indexes = data.getSubsetOfVarIndexes(m, random); // consider all or a subset of M variables
		for (Integer j : indexes) { // for each variable i
			// The goal is to find the lowest expected entropy for all possible
			// values of this predictor variable.
			// CATEGORICAL
			DataTable.VariableType colType = data.getColTypes()[j];
			if ( DataTable.isCategoricalVar(colType) ) {
			}
			else {
				// Rather than splitting the data table for each unique value of this variable
				// (which would be O(n^2)), we sort on this variable and then
				// walk the data records, keeping track of the predicted category counts.
				// We keep a snapshot of the category counts every time the predictor variable
				// changes in the sorted list.
				data.sortBy(j);
				// look for discontinuities (transitions) in predictor var values,
				// recording prediction cat counts for each
				LinkedHashMap<Integer, FrequencySet<Integer>> predictionCountSets = new LinkedHashMap<>(); // track key order of insertion
				FrequencySet<Integer> currentPredictionCounts = new FrequencySet<>();
				for (int i = 0; i<N; i++) { // walk all records, updating currentPredictionCounts
					if ( i>0 && data.compare(i-1, i, j)<0 ) { // if row i-1 < row i, discontinuity in predictor var
						// Take snapshot of current predictor counts, associate with current/new value as split point
						// Don't include new value in this snapshot
						predictionCountSets.put(data.get(i, j), new FrequencySet<>(currentPredictionCounts));
					}
					currentPredictionCounts.add(data.get(i, yi));
				}


				// Now, walk all possible prediction count sets to find the split
				// with the minimum entropy. predictionCountSets keys are the
				// unique set of values from column j. predictionCountSets[v] is
				// the predictor count set for values of column j < v
				for (Integer splitValue : predictionCountSets.keySet()) {
					FrequencySet<Integer> region1 = predictionCountSets.get(splitValue);
					FrequencySet<Integer> region2 = FrequencySet.minus(completePredictionCounts, region1);
					double r1_entropy = AniStats.entropy(region1.counts());
					double r2_entropy = AniStats.entropy(region2.counts());

					int n1 = sum(region1.counts());
					int n2 = sum(region2.counts());
					double p1 = ((double) n1)/(n1+n2);
					double p2 = ((double) n2)/(n1+n2);
					double expectedEntropyValue = p1*r1_entropy+p2*r2_entropy;
					double gain = complete_entropy-expectedEntropyValue;

					String newbest = "";
					if ( gain>best.gain && n1>0 && n2>0 ) {
						best.gain = gain;
						best.var = j;
						best.val = splitValue;
						best.split = split(data, best.var, best.val);
						newbest = " (new best)";
					}
					String var = data.getColNames()[j];
					if ( debug ) {
						System.out.printf("Entropies var=%13s val=%d r1=%d/%d*%.2f r2=%d/%d*%.2f, ExpEntropy=%.2f gain=%.2f%s\n",
						                  var, splitValue, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy, expectedEntropyValue, gain, newbest);
					}
				}
			}
		}
		if ( best.gain>0.0 ) {
			DecisionSplitNode t;
			DataTable.VariableType colType = data.getColTypes()[best.var];
			if ( DataTable.isCategoricalVar(colType) ) {
				t = new DecisionCategoricalSplitNode(best.var, best.val);
			}
			else {
				t = new DecisionNumericSplitNode(best.var, best.val);
			}
			t.numRecords = N;
			t.entropy = complete_entropy;
			t.data = data;
			t.left = build(best.split.region1, m, leafSize);
			t.right = build(best.split.region2, m, leafSize);
			return t;
		}
		// we would gain nothing by splitting, make a leaf predicting majority vote
		int majorityVote = data.valueCountsInColumn(yi).argmax();
		DecisionTree t = new DecisionLeafNode(majorityVote, yi);
		t.numRecords = N;
		t.entropy = complete_entropy;
		t.data = data;
		return t;
	}

	public boolean isLeaf() { return this instanceof DecisionLeafNode; }

	public DataTable getData() {
		return data;
	}

	public static DataPair split(DataTable X, int splitVariable, int splitValue) {
		DataTable a = X.filter(x -> x[splitVariable] < splitValue);
		DataTable b = X.filter(x -> x[splitVariable] >= splitValue);
		return new DataPair(a,b);
	}

	public abstract JsonObject toJSON();

	public String toDOT() {
		StringBuilder buf = new StringBuilder();
		buf.append("digraph dtree {\n");
		List<String> nodes = new ArrayList<>();
		getDOTNodeNames(nodes);
		for (String node : nodes) {
			buf.append("\t"+node+"\n");
		}
		List<String> edges = new ArrayList<>();
		getDOTEdges(edges);
		for (String edge : edges) {
			buf.append("\t"+edge+"\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	protected abstract void getDOTEdges(List<String> edges);

	protected abstract void getDOTNodeNames(List<String> nodes);
}
