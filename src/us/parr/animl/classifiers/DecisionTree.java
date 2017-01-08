package us.parr.animl.classifiers;

import us.parr.animl.AniStats;
import us.parr.animl.AniUtil;
import us.parr.animl.data.DataPair;
import us.parr.animl.data.FrequencySet;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** A classic CART decision tree but this implementation is suitable just for
 *  classification, not regression. I extended it to handle a subset of predictor
 *  variables at each node to support random forest construction.
 */
public abstract class DecisionTree {
	public static final int SEED = 777111333; // need randomness but use same seed to get reproducibility
	public static final Random random = new Random(SEED);

	// for debugging, fields below
	protected int numRecords;
	protected double entropy;

	public DecisionTree() {
	}

	public abstract int classify(int[] X);

	/** Conversion routine from separate X -> Y vectors to single augmented data vector */
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

	public static DecisionTree build(List<int[]> data) {
		return build(data, null, 0);
	}

	public static DecisionTree build(List<int[]> data, String[] varnames) {
		return build(data, varnames, 0);
	}

	/** Build a decision tree starting with arg data and recursively
	 *  build up children. data_i is the ith observation and the last column of
	 *  data is the predicted (dependent) variable.  Keeping the data together
	 *  makes it easier to implement since splitting a set splits both
	 *  features and predicted variables.
	 *
	 *  If m>0, select split var from random subset of size m from all variable set.
	 */
	public static DecisionTree build(List<int[]> data, String[] varnames, int m) {
		if ( data==null || data.size()==0 ) return null;
		int N = data.size();
		int M = data.get(0).length - 1; // last column is the predicted var
		int yi = M; // last index is the target variable
		// if all predict same category or only one row of data,
		// create leaf predicting that
		double complete_entropy = AniStats.entropy(AniStats.valueCountsInColumn(data, yi).counts());
		int pureCategory = AniUtil.uniqueValue(data, yi);
		if ( pureCategory!=RandomForest.INVALID_CATEGORY ) {
			DecisionTree t = new DecisionLeafNode(pureCategory);
			t.numRecords = N;
			t.entropy = complete_entropy;
			return t;
		}

		System.out.printf("entropy of all %d values = %.2f\n", N, complete_entropy);
		double best_gain = 0.0;
		int best_var = -1;
		int best_val = 0;
		DataPair best_split = null;
		// Non-random forest decision trees do just: for (int i=0; i<M; i++) {
		// but RF must use a subset m << M of predictor variables so this is
		// a generalization
		List<Integer> indexes = getVarIndexes(m, M); // consider all or a subset of M variables
		for (Integer i : indexes) { // for each variable i
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
				FrequencySet<Integer> r1_categoryCounts = AniStats.valueCountsInColumn(s.region1, yi);
				FrequencySet<Integer> r2_categoryCounts = AniStats.valueCountsInColumn(s.region2, yi);
				int n1 = s.region1.size();
				int n2 = s.region2.size();
				double r1_entropy = AniStats.entropy(r1_categoryCounts.counts());
				double r2_entropy = AniStats.entropy(r2_categoryCounts.counts());

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
			DecisionSplitNode t = new DecisionSplitNode(best_var, best_val);
			t.numRecords = N;
			t.entropy = complete_entropy;
			t.left = build(best_split.region1, varnames, m);
			t.right = build(best_split.region2, varnames, m);
			return t;
		}
		// we would gain nothing by splitting, make a leaf predicting majority vote
		int majorityVote = AniStats.valueCountsInColumn(data, yi).argmax();
		DecisionTree t = new DecisionLeafNode(majorityVote);
		t.numRecords = N;
		t.entropy = complete_entropy;
		return t;
	}

	public static List<Integer> getVarIndexes(int m, int M) {
		if ( m<=0 ) m = M;
		List<Integer> indexes = new ArrayList<>(M);
		for (int i = 0; i<M; i++) {
			indexes.add(i);
		}
		Collections.shuffle(indexes, random);
		indexes = indexes.subList(0, m);
		Collections.sort(indexes);
		return indexes;
	}

	public boolean isLeaf() { return this instanceof DecisionLeafNode; }

	public static DataPair split(List<int[]> X, int splitVariable, int splitValue) {
		List<int[]> a = AniUtil.filter(X, x -> x[splitVariable] < splitValue);
		List<int[]> b = AniUtil.filter(X, x -> x[splitVariable] >= splitValue);
		return new DataPair(a,b);
	}

	public JsonObject toJSON() {
		return toJSON(null, null);
	}

	public abstract JsonObject toJSON(String[] varnames, String[] catnames);

	public String toDOT(String[] varnames, String[] catnames) {
		StringBuilder buf = new StringBuilder();
		buf.append("digraph dtree {\n");
		List<String> nodes = new ArrayList<>();
		getDOTNodeNames(nodes, varnames, catnames);
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

	protected abstract void getDOTNodeNames(List<String> nodes, String[] varnames, String[] catnames);
}
