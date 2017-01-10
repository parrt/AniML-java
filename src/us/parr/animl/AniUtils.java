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
import java.util.function.Function;
import java.util.function.Predicate;

public class AniUtils {
	public static FrequencySet<Integer> valueCountsInColumn(List<int[]> X, int splitVariable) {
		FrequencySet<Integer> valueCounts = new FrequencySet<>();
		for (int i = 0; i<X.size(); i++) { // for each row, count different values for col splitVariable
			int[] row = X.get(i);
			int col = row[splitVariable];
			valueCounts.add(col);
		}
		return valueCounts;
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

	public static <T> T findFirst(Collection<T> data, Predicate<T> pred) {
		if ( data!=null ) {
			for (T x : data) {
				if ( pred.test(x) ) {
					return x;
				}
			}
		}
		return null;
	}

	public static <T> T findFirst(T[] data, Predicate<T> pred) {
		if ( data!=null ) {
			for (T x : data) {
				if ( pred.test(x) ) {
					return x;
				}
			}
		}
		return null;
	}

	public static <T> int indexOf(Collection<? extends T> elems, Predicate<T> pred) {
		if ( elems!=null ) {
			int i = 0;
			for (T elem : elems) {
				if ( pred.test(elem) ) return i;
				i++;
			}
		}
		return -1;
	}

	public static <T> int indexOf(T[] elems, Predicate<T> pred) {
		if ( elems!=null ) {
			int i = 0;
			for (T elem : elems) {
				if ( pred.test(elem) ) return i;
				i++;
			}
		}
		return -1;
	}

	public static <T> int lastIndexOf(Collection<? extends T> elems, Predicate<T> pred) {
		if ( elems!=null ) {
			int i = elems.size()-1;
			for (T elem : elems) {
				if ( pred.test(elem) ) return i;
			}
		}
		return -1;
	}

	public static <T, R> List<R> map(Collection<T> data, Function<T, R> getter) {
		List<R> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			output.add(getter.apply(x));
		}
		return output;
	}

	public static <T, R> List<R> map(T[] data, Function<T, R> getter) {
		List<R> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			output.add(getter.apply(x));
		}
		return output;
	}

	public static String join(Collection<?> a, String separator) {
		StringBuilder buf = new StringBuilder();
		int i = 0;
		for (Object o : a) {
			if ( o!=null ) {
				buf.append(o.toString());
			}
			if ( (i+1)<a.size() ) {
				buf.append(separator);
			}
			i++;
		}
		return buf.toString();
	}

	public static String join(Object[] a, String separator) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<a.length; i++) {
			Object o = a[i];
			if ( o!=null ) {
				buf.append(o.toString());
			}
			if ( (i+1)<a.length ) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static String join(int[] a, String separator) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<a.length; i++) {
			buf.append(a[i]);
			if ( (i+1)<a.length ) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}


	public static String join(float[] a, String separator) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<a.length; i++) {
			buf.append(a[i]);
			if ( (i+1)<a.length ) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static String join(double[] a, String separator) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<a.length; i++) {
			buf.append(a[i]);
			if ( (i+1)<a.length ) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}
}
