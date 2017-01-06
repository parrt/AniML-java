package us.parr.rf;

import us.parr.rf.misc.DataPair;
import us.parr.rf.misc.FrequencySet;
import us.parr.rf.misc.RFUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	// for debugging, fields below

	protected int numRecords;
	protected double entropy;

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
		return build(data, (String[])null);
	}

	public static DecisionTree build(List<int[]> data, String[] varnames) {
		if ( data==null || data.size()==0 ) return null;
		int N = data.size();
		int M = data.get(0).length - 1; // last column is the predicted var
		int yi = M; // last index is the target variable
		// if all predict same category or only one row of data,
		// create leaf predicting that
		double complete_entropy = RFUtils.entropy(RFUtils.valueCountsInColumn(data, yi).counts());
		int pureCategory = RFUtils.uniqueValue(data, yi);
		if ( pureCategory!=INVALID_CATEGORY ) {
			DecisionTree t = new DecisionTree(pureCategory);
			t.numRecords = N;
			t.entropy = complete_entropy;
			return t;
		}

		System.out.printf("entropy of all %d values = %.2f\n", N, complete_entropy);
		double best_gain = 0.0;
		int best_var = -1;
		int best_val = 0;
		DataPair best_split = null;
		for (int i = 0; i<M; i++) { // for each variable i
//			FrequencySet<Integer> valuesAndCounts = RFUtils.valueCountsInColumn(data, i);
//			List<Integer> uniqueValues = valuesAndCounts.keys();
			// Sort data set on independent var i
			final int varIndex = i;
			Collections.sort(data, (ra,rb)-> {return Integer.compare(ra[varIndex],rb[varIndex]);});
			// look for discontinuities (transitions) in dependent var, record row index
			Set<Integer> splitValues = new HashSet<>();
			for (int j=1;j<N;j++){ // walk all records
				if (data.get(j-1)[yi] != data.get(j)[yi]) { // discontinuity in predicted var (not var i)
					int splitValue = data.get(j)[i];
					splitValues.add(splitValue);
				}
			}
			for (Integer splitValue : splitValues) {
				DataPair s = split(data, i, splitValue);
				FrequencySet<Integer> r1_categoryCounts = RFUtils.valueCountsInColumn(s.region1, yi);
				FrequencySet<Integer> r2_categoryCounts = RFUtils.valueCountsInColumn(s.region2, yi);
				int n1 = s.region1.size();
				int n2 = s.region2.size();
				double r1_entropy = RFUtils.entropy(r1_categoryCounts.counts());
				double r2_entropy = RFUtils.entropy(r2_categoryCounts.counts());

				double p1 = ((double)n1)/(n1+n2);
				double p2 = ((double)n2)/(n1+n2);
				double expectedEntropyValue = p1*r1_entropy+p2*r2_entropy;
				double gain = complete_entropy - expectedEntropyValue;

				String newbest = "";
				if ( gain > best_gain && n1>0 && n2>0 ) {
					best_gain = gain;
					best_var = i;
					best_val = splitValue;
					best_split = s;
					newbest=" (new best)";
				}
				String var = varnames!=null ? varnames[i] : String.valueOf(i);
				System.out.printf("Entropies var=%13s val=%d r1=%d/%d*%.2f r2=%d/%d*%.2f, ExpEntropy=%.2f gain=%.2f%s\n",
				                  var, splitValue, n1, n1+n2, r1_entropy, n2, n1+n2,r2_entropy, expectedEntropyValue, gain, newbest);
			}
		}
		if ( best_gain>0.0 ) {
			DecisionTree t = new DecisionTree(best_var, best_val);
			t.numRecords = N;
			t.entropy = complete_entropy;
			t.left = build(best_split.region1, varnames);
			t.right = build(best_split.region2, varnames);
			return t;
		}
		return null;
	}

	public boolean isLeaf() { return left==null && right==null && category!=INVALID_CATEGORY; }

	public void makeLeaf(int predictedCategory) { left=null; right=null; category=predictedCategory; }

	public static DataPair split(List<int[]> X, int splitVariable, int splitValue) {
		List<int[]> a = RFUtils.filter(X, x -> x[splitVariable] < splitValue);
		List<int[]> b = RFUtils.filter(X, x -> x[splitVariable] >= splitValue);
		return new DataPair(a,b);
	}

	public JsonObject toJSON() {
		return toJSON(null, null);
	}

	public JsonObject toJSON(String[] varnames, String[] catnames) {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		if ( isLeaf() ) {
			if ( catnames!=null ) {
				builder.add("predict", catnames[category]);
			}
			else {
				builder.add("predict", category);
			}
		}
		else {
			if ( varnames!=null ) {
				builder.add("var", varnames[splitVariable]);
			}
			else {
				builder.add("var", splitVariable);
			}
			builder.add("val", splitValue);
			builder.add("left", left.toJSON(varnames, catnames));
			builder.add("right", right.toJSON(varnames, catnames));
		}
		return builder.build();
	}

	public String toDOT(String[] varnames, String[] catnames) {
		StringBuilder buf = new StringBuilder();
		buf.append("digraph dtree {\n");
		List<String> nodes = new ArrayList<>();
		getDOTNodeNames(nodes, this, varnames, catnames);
		for (String node : nodes) {
			buf.append("\t"+node+"\n");
		}
		List<String> edges = new ArrayList<>();
		getDOTEdges(edges, this);
		for (String edge : edges) {
			buf.append("\t"+edge+"\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	protected static void getDOTEdges(List<String> edges, DecisionTree t) {
		if ( !t.isLeaf() ) {
			edges.add(String.format("n%s -> n%s [label=\"<%d\"];", System.identityHashCode(t), System.identityHashCode(t.left), t.splitValue));
			edges.add(String.format("n%s -> n%s [label=\">=%d\"];", System.identityHashCode(t), System.identityHashCode(t.right), t.splitValue));
			getDOTEdges(edges, t.left);
			getDOTEdges(edges, t.right);
		}
	}

	protected static void getDOTNodeNames(List<String> nodes, DecisionTree t, String[] varnames, String[] catnames) {
		int id = System.identityHashCode(t);
		if ( t.isLeaf() ) {
			if ( catnames!=null ) {
				nodes.add(String.format("n%d [shape=box, label=\"%s\\nn=%d\\nE=%.2f\"];",
				                        id, catnames[t.category], t.numRecords, t.entropy));
			}
			else {
				nodes.add(String.format("n%d [shape=box, label=\"%d\\nn=%d\\nE=%.2f\"];",
				                        id, t.category, t.numRecords, t.entropy));
			}
		}
		else {
			if ( varnames!=null ) {
				nodes.add(String.format("n%d [label=\"%s\\nn=%d\\nE=%.2f\"];",
				                        id, varnames[t.splitVariable], t.numRecords, t.entropy));
			}
			else {
				nodes.add(String.format("n%d [label=\"x%d\\nn=%d\\nE=%.2f\"];",
				                        id, t.splitVariable, t.numRecords, t.entropy));
			}
			getDOTNodeNames(nodes, t.left, varnames, catnames);
			getDOTNodeNames(nodes, t.right, varnames, catnames);
		}
	}
}
