package us.parr.rf.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static us.parr.rf.RandomForest.INVALID_CATEGORY;

public class RFUtils {
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
}
