/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl;

import us.parr.animl.data.FrequencySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static us.parr.animl.classifiers.DecisionTree.random;

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

	public static double variance(List<Integer> data) {
		int n = data.size();
		double sum = 0;
		double avg = sum(data)/((double) n);
		for (int d : data) {
			sum += (d-avg)*(d-avg);
		}
		return sum/n;
	}

	public static double varianceFloats(List<Float> data) {
		int n = data.size();
		double sum = 0;
		double avg = sumFloats(data)/((double) n);
		for (float d : data) {
			sum += (d-avg)*(d-avg);
		}
		return sum/n;
	}

	public static int sum(Collection<Integer> data) {
		int sum = 0;
		for (int d : data) {
			sum += d;
		}
		return sum;
	}

	public static float sumFloats(Collection<Float> data) {
		float sum = 0;
		for (float d : data) {
			sum += d;
		}
		return sum;
	}

	public static float sumDoubles(Collection<Double> data) {
		float sum = 0;
		for (double d : data) {
			sum += d;
		}
		return sum;
	}

	public static List<Double> diffFloats(List<Float> a, List<Float> b) {
		List<Double> diffs = new ArrayList<>();
		for (int i = 0; i<a.size(); i++) {
			diffs.add((double) a.get(i)-b.get(i));
		}
		return diffs;
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

	public static double mean(Collection<Double> data) {
		double sum = 0.0;
		for (Double d : data) {
			sum += d;
		}
		return sum / data.size();
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
