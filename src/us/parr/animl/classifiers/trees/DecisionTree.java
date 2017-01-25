/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.classifiers.ClassifierModel;
import us.parr.animl.data.DataPair;
import us.parr.animl.data.DataTable;
import us.parr.lib.ParrtStats;
import us.parr.lib.collections.CountingDenseIntSet;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static us.parr.lib.ParrtStats.sum;

/** A classic CART decision tree but this implementation is suitable just for
 *  classification, not regression. I extended it to handle a subset of predictor
 *  variables at each node to support random forest construction.
 */
public class DecisionTree implements ClassifierModel {
	public static final int SEED = 777111333; // need randomness but use same seed to get reproducibility
	public static final Random random = new Random(SEED);
	public static final int INVALID_CATEGORY = -1;

	protected static class BestInfo {
		public double gain = 0.0;
		public int var = -1;
		public double val = 0.0;
		public int cat = INVALID_CATEGORY;

		public BestInfo() { }

		public BestInfo(double gain, int var, int val) {
			this.gain = gain;
			this.var = var;
			this.val = val;
		}
	}

	public static boolean debug = false;

	protected DecisionTreeNode root;

	/** 0 implies use all possible vars when searching for a split var */
	protected int varsPerSplit;

	protected int minLeafSize;

	/** How much of data to examine at each node to find split point */
	protected int nodeSampleSize = 20;

	public DecisionTree() { this(0, 1, 20); }

	public DecisionTree(int varsPerSplit, int minLeafSize) {
		this(varsPerSplit, minLeafSize, 20);
	}

	public DecisionTree(int varsPerSplit, int minLeafSize, int nodeSampleSize) {
		this.varsPerSplit = varsPerSplit;
		this.minLeafSize = minLeafSize;
		this.nodeSampleSize = nodeSampleSize;
	}

	public int classify(int[] X) { return root.classify(X); };

	@Override
	public Map<Integer, Double> classProbabilities(int[] X) {
		return root.classProbabilities(X);
	}

	/** Build a decision tree starting with arg data and recursively
	 *  build up children. data_i is the ith observation and the (usually) last column of
	 *  data is the predicted (dependent) variable.  Keeping the data together
	 *  makes it easier to implement since splitting a data set splits both
	 *  features and predicted variables.
	 *
	 *  If varsPerSplit>0, select split var from random subset of size m from all variable set.
	 */
	public void train(DataTable data) {
		root = build(data, varsPerSplit, minLeafSize, nodeSampleSize);
	}

	protected static DecisionTreeNode build(DataTable data, int varsPerSplit, int minLeafSize, int nodeSampleSize) {
		if ( data==null || data.size()==0 ) { return null; }
		int N = data.size();
		int yi = data.getPredictedCol(); // last index is usually the target variable
		// if all predict same category or only one row of data,
		// create leaf predicting that
		CountingDenseIntSet completeCategoryCounts = (CountingDenseIntSet)data.valueCountsInColumn(yi);
		double complete_entropy = completeCategoryCounts.entropy();
		if ( completeCategoryCounts.size()==1 || data.size()<=minLeafSize ) {
			DecisionTreeNode t = new DecisionLeafNode(data, completeCategoryCounts, yi);
			return t;
		}

		if ( debug ) System.out.printf("entropy of all %d values = %.2f\n", N, complete_entropy);
		BestInfo best = new BestInfo();
		// Non-random forest decision trees do just: for (int i=0; i<M; i++) {
		// but RF must use a subset m << M of predictor variables so this is
		// a generalization
		List<Integer> indexes = data.getSubsetOfVarIndexes(varsPerSplit, random); // consider all or a subset of M variables
		for (Integer j : indexes) { // for each variable i
			// The goal is to find the lowest expected entropy for all possible
			// values of predictor variable j.  Then we compare best for j against
			// best for any variable
			DataTable.VariableType colType = data.getColTypes()[j];
			BestInfo bestj;
			if ( DataTable.isCategoricalVar(colType) ) {
				bestj = bestCategoricalSplit(data, j, yi, completeCategoryCounts, complete_entropy);
			}
			else {
				bestj = bestNumericSplit(data, j, yi, completeCategoryCounts, complete_entropy);
			}
			if ( bestj.gain > best.gain ) {
				best = bestj;
				if ( debug ) System.out.printf("Best is now var %s val %s gain=%.2f\n", data.getColNames()[best.var], best.val, best.gain);
			}
		}
		if ( best.gain>0.0 ) {
			if ( debug ) {
				System.out.printf("FINAL best is var %s val %s gain=%.2f\n",
				                  data.getColNames()[best.var], best.val, best.gain);
			}
			DataPair split;
			DecisionSplitNode t;
			DataTable.VariableType colType = data.getColTypes()[best.var];
			if ( DataTable.isCategoricalVar(colType) ) {
				// split is expensive, do it only after we get best var/val
				split = categoricalSplit(data, best.var, best.cat);
				t = new DecisionCategoricalSplitNode(data, best.var, colType, best.cat);
			}
			else {
				if ( colType==DataTable.VariableType.NUMERICAL_FLOAT ) {
					split = numericalFloatSplit(data, best.var, best.val);
				}
				else {
					split = numericalIntSplit(data, best.var, best.val);
				}
				t = new DecisionNumericalSplitNode(data, best.var, colType, best.val);
			}
			t.numRecords = N;
			t.entropy = (float)complete_entropy;
			t.left = build(split.region1,  varsPerSplit, minLeafSize, nodeSampleSize);
			t.right = build(split.region2, varsPerSplit, minLeafSize, nodeSampleSize);
			return t;
		}
		// we would gain nothing by splitting, make a leaf predicting majority vote
		int majorityVote = completeCategoryCounts.argmax();
		if ( debug ) {
			System.out.printf("FINAL no improvement; make leaf predicting %s\n",
			                  DataTable.getValue(data,majorityVote,yi));
		}
		DecisionTreeNode t = new DecisionLeafNode(data, completeCategoryCounts, yi);
		return t;
	}

	protected static BestInfo bestNumericSplit(DataTable data, int j, int yi,
	                                           CountingDenseIntSet completePredictionCounts,
	                                           double complete_entropy)
	{
		int n = data.size();
		BestInfo best = new BestInfo();
		// Rather than splitting the data table for each unique value of this variable
		// (which would be O(n^2)), we sort on this variable and then
		// walk the data records, keeping track of the predicted category counts.
		// We keep a snapshot of the category counts every time the predictor variable
		// changes in the sorted list.
		data.sortBy(j);

		int[] allCounts = completePredictionCounts.toDenseArray();

		// look for discontinuities (transitions) in predictor var values,
		// computing less than, greater than entropy for each from target cat counts;
		// track best split
		DataTable.VariableType colType = data.getColTypes()[j];
		int targetCatMaxValue = (Integer) data.getColMax(yi);
		int[] currentCounts = new int[targetCatMaxValue+1];
		int[] greaterThanCounts = new int[targetCatMaxValue+1]; // allocate this just once
		for (int i = 0; i<n; i++) { // walk all records, updating currentCounts
			if ( i>0 && data.compare(i-1, i, j)<0 ) { // if row i-1 < row i, discontinuity in predictor var
				double splitValue; // midpoint between new value and previous
				if ( colType==DataTable.VariableType.NUMERICAL_INT ) {
					splitValue = (data.getAsInt(i, j)+data.getAsInt(i-1, j))/2.0; // assumes col j sorted!
				}
				else {
					splitValue = (data.getAsFloat(i, j)+data.getAsFloat(i-1, j))/2.0;
				}
				int[] lessThanCounts = currentCounts;
				int n1 = i; // how many observations less than current discontinuity value
				int n2 = n - i;
				ParrtStats.minus(allCounts, lessThanCounts, greaterThanCounts);
				double expectedEntropyValue = expectedEntropy(lessThanCounts, n1, greaterThanCounts, n2);
				double gain = complete_entropy - expectedEntropyValue;
				if ( gain>best.gain ) {
					best.gain = gain;
					best.var = j;
					best.val = splitValue;
				}
				String var = data.getColNames()[j];
				if ( debug ) {
					double r1_entropy = ParrtStats.entropy(lessThanCounts);
					double r2_entropy = ParrtStats.entropy(greaterThanCounts);
					System.out.printf("Entropies var=%13s val=%.2f r1=%d/%d*%.2f r2=%d/%d*%.2f, ExpEntropy=%.2f gain=%.2f\n",
					                  var, splitValue, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy, expectedEntropyValue, gain);
				}
			}
			int targetCat = data.getAsInt(i, yi);
			currentCounts[targetCat]++;
		}

		return best;
	}

//	static int[][] catCounts = new int[20][20]; // seems to help but not by much

	protected static BestInfo bestCategoricalSplit(DataTable data, int j, int yi,
	                                               CountingDenseIntSet completePredictionCounts,
	                                               double complete_entropy) {
		int n = data.size();
		BestInfo best = new BestInfo();
		Integer targetCatMaxValue = (Integer) data.getColMax(yi);
		Integer colCatMaxValue = (Integer) data.getColMax(j);
//		for (int i = 0; i<20; i++) { // walk all records, counting dep categories in two groups: indep cat equal and not-equal to splitCat
//			Arrays.fill(catCounts[i], 0);
//		}
		int[][] catCounts = new int[colCatMaxValue+1][targetCatMaxValue+1];
		for (int i = 0; i<n; i++) { // walk all records, counting dep categories in two groups: indep cat equal and not-equal to splitCat
			int currentColCat = data.getAsInt(i, j);
			int currentTargetCat = data.getAsInt(i, yi);
			catCounts[currentColCat][currentTargetCat]++;
		}
		int[] notEqCounts = new int[targetCatMaxValue+1];
		int[] allCounts = completePredictionCounts.toDenseArray();
		for (int colCat = 0; colCat<catCounts.length; colCat++) {
			int[] currentCatCounts = catCounts[colCat];
			int n1 = sum(currentCatCounts);
			// category values are not necessarily contiguous; ignore col category values w/o observations
			if ( n1==0 ) continue;
			ParrtStats.minus(allCounts, currentCatCounts, notEqCounts);
			int n2 = sum(notEqCounts);
			double expectedEntropyValue = expectedEntropy(currentCatCounts, n1, notEqCounts, n2);
			double gain = complete_entropy-expectedEntropyValue;
			if ( gain>best.gain ) {
				best.gain = gain;
				best.var = j;
				best.cat = colCat;
			}
			if ( debug ) {
				double r1_entropy = ParrtStats.entropy(currentCatCounts);
				double r2_entropy = ParrtStats.entropy(notEqCounts);
				String var = data.getColNames()[j];
				Object p = DataTable.getValue(data, colCat, j);
				System.out.printf("Entropies var=%13s cat=%-13s r1=%2d/%3d*%.2f r2=%2d/%3d*%.2f, ExpEntropy=%.2f gain=%.2f\n",
				                  var, p, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy,
				                  expectedEntropyValue, gain);
			}
		}

		return best;
	}

	public static double expectedEntropy(int[] region1CatCounts, int n1,
	                                     int[] region2CatCounts, int n2)
	{
		double r1_entropy = ParrtStats.entropy(region1CatCounts);
		double r2_entropy = ParrtStats.entropy(region2CatCounts);
		double p1 = ((double) n1)/(n1+n2);
		double p2 = ((double) n2)/(n1+n2);
		return p1*r1_entropy+p2*r2_entropy;
	}

	public boolean isLeaf() { return root instanceof DecisionLeafNode; }

	public static DataPair numericalIntSplit(DataTable X, int splitVariable, double splitValue) {
		DataTable a = X.filter(x -> x[splitVariable] < splitValue);
		DataTable b = X.filter(x -> x[splitVariable] >= splitValue);
		return new DataPair(a,b);
	}

	public static DataPair numericalFloatSplit(DataTable X, int splitVariable, double splitValue) {
		DataTable a = X.filter(x -> Float.intBitsToFloat(x[splitVariable]) < splitValue);
		DataTable b = X.filter(x -> Float.intBitsToFloat(x[splitVariable]) >= splitValue);
		return new DataPair(a,b);
	}

	public static DataPair categoricalSplit(DataTable X, int splitVariable, int splitCategory) {
		DataTable a = X.filter(x -> x[splitVariable] == splitCategory);
		DataTable b = X.filter(x -> x[splitVariable] != splitCategory);
		return new DataPair(a,b);

		/* This code is surprisingly a bit slower than the above simple code.
		List<int[]> eq = new ArrayList<>();
		List<int[]> notEq = new ArrayList<>();
		for (int[] row : X.getRows()) {
			if ( row[splitVariable] == splitCategory ) {
				eq.add(row);
			}
			else {
				notEq.add(row);
			}
		}
		return new DataPair(new DataTable(X, eq), new DataTable(X, notEq));
		*/
	}

	public JsonObject toJSON() { return root!=null ? root.toJSON() : Json.createObjectBuilder().build(); }

	public String toDOT() {
		StringBuilder buf = new StringBuilder();
		buf.append("digraph dtree {\n");
		List<String> nodes = new ArrayList<>();
		getDOTNodeNames(root, nodes);
		for (String node : nodes) {
			buf.append("\t"+node+"\n");
		}
		List<String> edges = new ArrayList<>();
		getDOTEdges(root, edges);
		for (String edge : edges) {
			buf.append("\t"+edge+"\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	protected static void getDOTNodeNames(DecisionTreeNode t, List<String> nodes) {
		nodes.add(t.getDOTNodeDef());
		if ( t instanceof DecisionSplitNode ) {
			DecisionSplitNode s = (DecisionSplitNode)t;
			getDOTNodeNames(s.left, nodes);
			getDOTNodeNames(s.right, nodes);
		}
	}

	protected static void getDOTEdges(DecisionTreeNode t, List<String> edges) {
		if ( t instanceof DecisionSplitNode ) {
			DecisionSplitNode s = (DecisionSplitNode) t;
			edges.add(s.getDOTLeftEdge());
			edges.add(s.getDOTRightEdge());
			getDOTEdges(s.left, edges);
			getDOTEdges(s.right, edges);
		}
	}
}
