package us.parr.rf;

import us.parr.rf.misc.DataPair;
import us.parr.rf.misc.FrequencySet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static us.parr.rf.RandomForest.INVALID_CATEGORY;

/** A classic CART decision tree but this implementation is suitable just for
 *  classification, not regression.
 */
public class DecisionTree {
//	public static final double MIN_GINI_IMPURITY_TO_BE_LEAF = 0.0001;

	/** This node is split on which variable? */
	protected int splitVariable;

	/** Split at what variable value? */
	protected int splitValue;

	/** Left child if not a leaf node; non-null implies not a leaf node. */
	protected DecisionTree left;
	protected DecisionTree right;

	/** The predicted category if this is a leaf node; non-leaf by default */
	protected int category = INVALID_CATEGORY;

	public DecisionTree() {
	}

	public DecisionTree(int predictedCategory) {
		makeLeaf(predictedCategory);
	}

	public DecisionTree(int splitVariable, int splitValue) {
		this.splitVariable = splitVariable;
		this.splitValue = splitValue;
	}

	public static DecisionTree build(List<int[]> X, List<Integer> Y) {
		List<int[]> data = new ArrayList<>(X.size());
		for (int i = 0; i<X.size(); i++) {
			int[] row = X.get(i);
			int[] augmented = new int[row.length+1];
			System.arraycopy(row, 0, augmented, 0, row.length);
			augmented[row.length] = Y.get(i);
			data.add(augmented);
		}
		return build(data);
	}

	/** Build a decision tree starting with arg data and recursively
	 *  build up children. data_i is the ith observation and the last column of
	 *  data is the predicted (dependent) variable.  Keeping the data together
	 *  makes it easier to implement since splitting a set splits both
	 *  features and predicted variables.
	 */
	public static DecisionTree build(List<int[]> data) {
		if ( data==null || data.size()==0 ) return null;
		int N = data.size();
		int M = data.get(0).length;
		int yi = M-1; // last index is the target variable
		// if all predict same category or only one row of data,
		// create leaf predicting that
		int pureCategory = uniqueValue(data, yi);
		if ( pureCategory!=INVALID_CATEGORY ) {
			return new DecisionTree(pureCategory);
		}

		double complete_gini = gini(values(data, yi), N);
		double best_gain = 0.0;
		int best_var = -1;
		int best_val = 0;
		DataPair best_split = null;
		for (int i = 0; i<M; i++) { // for each variable
			FrequencySet<Integer> values = values(data, i);
			for (Integer uniqueValue : values.keySet()) { // for each value that variable takes on
				DataPair s = split(data, i, uniqueValue);
				FrequencySet<Integer> r1_categoryCounts = values(s.region1, yi);
				FrequencySet<Integer> r2_categoryCounts = values(s.region2, yi);
				int n1 = s.region1.size();
				int n2 = s.region2.size();
				double r1_gini = gini(r1_categoryCounts, n1);
				double r2_gini = gini(r2_categoryCounts, n2);

				double p1 = ((double)n1)/(n1+n2);
				double p2 = ((double)n2)/(n1+n2);
				double gain = complete_gini - (p1 * r1_gini + p2 * r2_gini);
				if ( gain > best_gain && n1>0 && n2>0 ) {
					best_gain = gain;
					best_var = i;
					best_val = uniqueValue;
					best_split = s;
				}
			}
		}
		if ( best_gain>0.0 ) {
			DecisionTree t = new DecisionTree(best_var, best_val);
			t.left = build(best_split.region1);
			t.right = build(best_split.region2);
		}
		return null;
	}

	public boolean isLeaf() { return left==null && right==null && category!=INVALID_CATEGORY; }

	public void makeLeaf(int predictedCategory) { left=null; right=null; category=predictedCategory; }

	/** Compute the gini impurity */
	public static double gini(FrequencySet<Integer> categoryCounts, int n) {
		double impurity = 0.0;
		for (Integer cat : categoryCounts.keySet()) {
			int count = categoryCounts.count(cat);
			double freq = ((double)count) / n;
			impurity += freq * (1-freq);
		}
		return impurity;
	}

	public static FrequencySet<Integer> values(List<int[]> X, int splitVariable) {
		FrequencySet<Integer> valueCounts = new FrequencySet<>();
		for (int i = 0; i<X.size(); i++) { // for each row, count different values for col splitVariable
			int[] row = X.get(i);
			int col = row[splitVariable];
			valueCounts.add(col);
		}
		return valueCounts;
	}

	public static DataPair split(List<int[]> X, int splitVariable, int splitValue) {
		List<int[]> a = filter(X, x -> x[splitVariable] < splitValue);
		List<int[]> b = filter(X, x -> x[splitVariable] >= splitValue);
		return new DataPair(a,b);
	}

	public static int uniqueValue(List<int[]> data, int varIndex) {
		if ( data==null ) {
			return INVALID_CATEGORY;
		}
		int[] firstRow = data.get(0);
		int v = firstRow[varIndex];
		for (int[] row : data) {
			if ( row[varIndex]!=v ) {
				return INVALID_CATEGORY;
			}
		}
		return v;
	}

	public static <T> List<T> filter(List<T> data, Predicate<T> pred) {
		List<T> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			if ( pred.test(x) ) {
				output.add(x);
			}
		}
		return output;
	}
}
