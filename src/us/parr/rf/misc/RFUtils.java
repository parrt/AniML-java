package us.parr.rf.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static java.lang.Math.log;
import static us.parr.rf.RandomForest.INVALID_CATEGORY;

public class RFUtils {
//	public static final int SEED = 777111333; // need randomness but use same seed to get reproducibility
//	final public static Random random = new Random();
//	static {
//		random.setSeed(SEED);
//	}

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

	public static boolean isClose(double a, double b) {
		boolean result = Math.abs(a-b)<0.000000001;
		return result;
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
			entropy += p *log2(p);
		}
		entropy = -entropy;
		return entropy;
	}

	public static double log2(double p) {
		return log(p) / log(2.0); // log2(x) = log(x)/log(2)
	}

	/** Produce an array of n random integers in [0..highvalue) */
	public static int[] randint(int n, int highvalue, int seed) {
		Random random = new Random(seed);
		int[] values = new int[n];
		for (int i = 0; i<n; i++) {
			values[i] = random.nextInt(highvalue);
		}
		return values;
	}
}
