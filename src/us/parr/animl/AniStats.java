package us.parr.animl;

import us.parr.animl.data.FrequencySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static us.parr.animl.classifiers.DecisionTree.random;
import static us.parr.animl.classifiers.RandomForest.INVALID_CATEGORY;

public class AniStats {
	public static FrequencySet<Integer> valueCountsInColumn(List<int[]> X, int splitVariable) {
		FrequencySet<Integer> valueCounts = new FrequencySet<>();
		for (int i = 0; i<X.size(); i++) { // for each row, count different values for col splitVariable
			int[] row = X.get(i);
			int col = row[splitVariable];
			valueCounts.add(col);
		}
		return valueCounts;
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

	public static int max(Collection<Integer> data) {
		if ( data==null ) {
			return Integer.MIN_VALUE;
		}
		int m = Integer.MIN_VALUE;
		for (int d : data) {
			if ( d>m ) m = d;
		}
		return m;
	}

	public static int sum(Collection<Integer> data) {
		int sum = 0;
		for (int d : data) {
			sum += d;
		}
		return sum;
	}

	/** Compute the gini impurity from a collection of counts */
	public static double gini(Collection<Integer> counts) {
		double impurity = 0.0;
		int n = sum(counts);
		for (Integer v : counts) {
			double p = ((double)v) / n;
			impurity += p * (1-p);
		}
		return impurity;
	}

	/** Compute the entropy from a collection of counts */
	public static double entropy(Collection<Integer> counts) {
		double entropy = 0.0;
		int n = sum(counts);
		for (Integer v : counts) {
			if ( v==0 ) continue; // avoid log(0), which is undefined
			double p = ((double)v) / n;
			entropy += p *AniMath.log2(p);
		}
		entropy = -entropy;
		return entropy;
	}

	/** Produce an array of n random integers in [0..highvalue) */
	public static int[] randint(int n, int highvalue) {
		int[] values = new int[n];
		for (int i = 0; i<n; i++) {
			values[i] = random.nextInt(highvalue);
		}
		return values;
	}

	/** From data, grab n records at random with replacement */
	public static List<int[]> bootstrapWithRepl(List<int[]> data) {
		return bootstrapWithRepl(data, null);
	}

	/** From data, grab n records at random with replacement, fill in oob with
	 *  data NOT in returned bootstrap (if non-null).
	 */
	public static List<int[]> bootstrapWithRepl(List<int[]> data, List<int[]> oob) {
		int[] indexes = randint(data.size(), data.size());
		if ( oob!=null ) {
			oob.addAll(data);
		}
		List<int[]> bootstrap = new ArrayList<>(indexes.length);
		for (int i : indexes) {
			bootstrap.add(data.get(i));
			if ( oob!=null ) {
				oob.remove(i); // make sure bootstrap records are not in oob
			}
		}
		return bootstrap;
	}

	public static int majorityVote(Collection<Integer> data) {
		FrequencySet<Integer> valueCounts = new FrequencySet<>();
		for (Integer d : data) {
			valueCounts.add(d);
		}
		return valueCounts.argmax();
	}
}
