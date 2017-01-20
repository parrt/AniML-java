/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.AniStats;
import us.parr.animl.classifiers.ClassifierModel;
import us.parr.animl.data.CountingSet;
import us.parr.animl.data.DataPair;
import us.parr.animl.data.DataTable;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

	public DecisionTree() { this(0, 1); }

	public DecisionTree(int varsPerSplit, int minLeafSize) {
		this.varsPerSplit = varsPerSplit;
		this.minLeafSize = minLeafSize;
	}

	public int classify(int[] X) { return root.classify(X); };

	@Override
	public Map<Integer, Double> classProbabilities(int[] X) {
		return root.classProbabilities(X);
	}

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

	/** Build a decision tree starting with arg data and recursively
	 *  build up children. data_i is the ith observation and the (usually) last column of
	 *  data is the predicted (dependent) variable.  Keeping the data together
	 *  makes it easier to implement since splitting a data set splits both
	 *  features and predicted variables.
	 *
	 *  If varsPerSplit>0, select split var from random subset of size m from all variable set.
	 */
	public void train(DataTable data) {
		root = build(data, varsPerSplit, minLeafSize);
	}

	protected static DecisionTreeNode build(DataTable data, int varsPerSplit, int minLeafSize) {
		if ( data==null || data.size()==0 ) { return null; }
		int N = data.size();
		int yi = data.getPredictedCol(); // last index is usually the target variable
		// if all predict same category or only one row of data,
		// create leaf predicting that
		CountingSet<Integer> completeCategoryCounts = data.valueCountsInColumn(yi);
		double complete_entropy = AniStats.entropy(completeCategoryCounts.counts());
		if ( completeCategoryCounts.size()==1 || data.size()<=minLeafSize ) {
			DecisionTreeNode t = new DecisionLeafNode(completeCategoryCounts, yi);
			t.data = data;
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
				t = new DecisionCategoricalSplitNode(best.var, colType, best.cat);
			}
			else {
				if ( colType==DataTable.VariableType.NUMERICAL_FLOAT ) {
					split = numericalFloatSplit(data, best.var, best.val);
				}
				else {
					split = numericalIntSplit(data, best.var, best.val);
				}
				t = new DecisionNumericalSplitNode(best.var, colType, best.val);
			}
			t.numRecords = N;
			t.entropy = complete_entropy;
			t.data = data;
			t.left = build(split.region1,  varsPerSplit, minLeafSize);
			t.right = build(split.region2, varsPerSplit, minLeafSize);
			return t;
		}
		// we would gain nothing by splitting, make a leaf predicting majority vote
		int majorityVote = data.valueCountsInColumn(yi).argmax();
		if ( debug ) {
			System.out.printf("FINAL no improvement; make leaf predicting %s\n",
			                  DataTable.getValue(data,majorityVote,yi));
		}
		DecisionTreeNode t = new DecisionLeafNode(completeCategoryCounts, yi);
		t.data = data;
		return t;
	}

	protected static BestInfo bestNumericSplit(DataTable data, int j, int yi,
	                                           CountingSet<Integer> completePredictionCounts,
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
		// look for discontinuities (transitions) in predictor var values,
		// recording prediction cat counts for each
		LinkedHashMap<Double, CountingSet<Integer>> predictionCountSets = new LinkedHashMap<>(); // track key order of insertion
		CountingSet<Integer> currentPredictionCounts = new CountingSet<>();
		DataTable.VariableType colType = data.getColTypes()[j];
		for (int i = 0; i<n; i++) { // walk all records, updating currentPredictionCounts
			if ( i>0 && data.compare(i-1, i, j)<0 ) { // if row i-1 < row i, discontinuity in predictor var
				// Take snapshot of current predictor counts, associate with current/new value as split point
				// Don't include new value in this snapshot
				// as split value, choose midway between previous and current value as it's likely more general.
				// it's unlikely that split value is exactly the current value
				double midpoint;
				if ( colType==DataTable.VariableType.NUMERICAL_INT ) {
					midpoint = (data.getAsInt(i, j)+data.getAsInt(i-1, j))/2.0;
				}
				else {
					midpoint = (data.getAsFloat(i, j)+data.getAsFloat(i-1, j))/2.0;
				}
				predictionCountSets.put(midpoint, new CountingSet<>(currentPredictionCounts));
			}
			currentPredictionCounts.add(data.getAsInt(i, yi));
		}

		// Now, walk all possible prediction count sets to find the split
		// with the minimum entropy. predictionCountSets keys are the
		// unique set of values from column j. predictionCountSets[v] is
		// the predictor count set for values of column j < v
		for (Double splitValue : predictionCountSets.keySet()) {
			CountingSet<Integer> region1 = predictionCountSets.get(splitValue);
			CountingSet<Integer> region2 = CountingSet.minus(completePredictionCounts, region1);

			double r1_entropy = region1.entropy();
			double r2_entropy = region2.entropy();
			int n1 = region1.total();
			int n2 = region2.total();
			double p1 = ((double) n1)/(n1+n2);
			double p2 = ((double) n2)/(n1+n2);
			double expectedEntropyValue = p1*r1_entropy + p2*r2_entropy;
			double gain = complete_entropy - expectedEntropyValue;

			if ( gain>best.gain && n1>0 && n2>0 ) {
				best.gain = gain;
				best.var = j;
				best.val = splitValue;
			}
			String var = data.getColNames()[j];
			if ( debug ) {
				System.out.printf("Entropies var=%13s val=%.2f r1=%d/%d*%.2f r2=%d/%d*%.2f, ExpEntropy=%.2f gain=%.2f\n",
				                  var, splitValue, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy, expectedEntropyValue, gain);
			}
		}
		return best;
	}

	protected static BestInfo bestCategoricalSplit(DataTable data, int j, int yi,
	                                               CountingSet<Integer> completePredictionCounts,
	                                               double complete_entropy)
	{
		int n = data.size();
		BestInfo best = new BestInfo();
		Set<Integer> uniqueValues = data.getUniqueValues(j);
		for (Integer splitCat : uniqueValues) { // for each unique category in col j
			CountingSet<Integer> eq = new CountingSet<>();
			CountingSet<Integer> noteq = new CountingSet<>();
			for (int i = 0; i<n; i++) { // walk all records, counting dep categories in two groups: indep cat equal and not-equal to splitCat
				int currentCat = data.getAsInt(i, j);
//				System.out.println(Arrays.toString(data.getRow(i))+", currentCat = "+currentCat+" @ "+i+","+j);
				int currentTargetCat = data.getAsInt(i, yi);
				if ( currentCat==splitCat ) {
					eq.add(currentTargetCat);
				}
				else {
					noteq.add(currentTargetCat);
				}
			}
//			System.out.println("eq="+eq+", noteq="+noteq);
			double r1_entropy = eq.entropy();
			double r2_entropy = noteq.entropy();
			int n1 = eq.total();
			int n2 = noteq.total();
			double p1 = ((double) n1)/(n1+n2);
			double p2 = ((double) n2)/(n1+n2);
			double expectedEntropyValue = p1*r1_entropy + p2*r2_entropy;
			double gain = complete_entropy - expectedEntropyValue;
			if ( gain>best.gain && n1>0 && n2>0 ) {
				best.gain = gain;
				best.var = j;
				best.cat = splitCat;
			}
			if ( debug ) {
				String var = data.getColNames()[j];
				Object p = DataTable.getValue(data, splitCat, j);
				System.out.printf("Entropies var=%13s cat=%-13s r1=%2d/%3d*%.2f r2=%2d/%3d*%.2f, ExpEntropy=%.2f gain=%.2f\n",
				                  var, p, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy,
				                  expectedEntropyValue, gain);
			}
		}
		return best;
		/*
		// no need to sort
		// Map col j category value to target category counts
		Map<Integer, CountingSet<Integer>> predictionCountSets = new HashMap<>();
		CountingSet<Integer> currentPredictionCounts = new CountingSet<>();
		for (int i = 0; i<n; i++) { // walk all records, updating currentPredictionCounts
			int cat = data.getAsInt(n-1, j);
			predictionCountSets.computeIfAbsent(cat, k -> new CountingSet<>());
			currentPredictionCounts.add(data.getAsInt(i, yi));
		}

		// Now, walk all possible col j category values and check count sets
		// to find the split with the minimum entropy.
		// predictionCountSets keys are the unique set of values from column j.
		// predictionCountSets[v] is the predictor count set for values of column j == v
		for (Integer splitValue : predictionCountSets.keySet()) {
			CountingSet<Integer> region1 = predictionCountSets.get(splitValue);
			CountingSet<Integer> region2 = CountingSet.minus(completePredictionCounts, region1);
			double r1_entropy = AniStats.entropy(region1.counts());
			double r2_entropy = AniStats.entropy(region2.counts());

			int n1 = sum(region1.counts());
			int n2 = sum(region2.counts());
			double p1 = ((double) n1)/(n1+n2);
			double p2 = ((double) n2)/(n1+n2);
			double expectedEntropyValue = p1*r1_entropy+p2*r2_entropy;
			double gain = complete_entropy-expectedEntropyValue;

			if ( gain>best.gain && n1>0 && n2>0 ) {
				best.gain = gain;
				best.var = j;
				best.cat = splitValue;
			}
			String var = data.getColNames()[j];
			if ( debug ) {
				System.out.printf("Entropies var=%13s val=%d r1=%d/%d*%.2f r2=%d/%d*%.2f, ExpEntropy=%.2f gain=%.2f\n",
				                  var, splitValue, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy, expectedEntropyValue, gain);
			}
		}
		return best;
		*/
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
