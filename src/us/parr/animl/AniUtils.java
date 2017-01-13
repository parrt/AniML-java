/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl;

import us.parr.animl.data.CountingSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class AniUtils {
	public static CountingSet<Integer> valueCountsInColumn(List<int[]> X, int splitVariable) {
		CountingSet<Integer> valueCounts = new CountingSet<>();
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

	public static <T> int indexOf(T[] elems, T value) {
		if ( elems!=null ) {
			int i = 0;
			for (T elem : elems) {
				if ( elem.equals(value) ) return i;
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

	public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
		Set<T> inter = new HashSet<T>();
		for (T v : a) {
			if ( b.contains(v) ) inter.add(v);
		}
		return inter;
	}

	public static <T> Set<T> difference(Set<T> a, Set<T> b) { // 1,2,3 - 2 = 1,3
		Set<T> diff = new HashSet<T>();
		for (T v : a) {
			if ( !b.contains(v) ) diff.add(v);
		}
		return diff;
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

	public static String join(double[] a, String separator, int numDecPlaces) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<a.length; i++) {
			buf.append(String.format("%."+numDecPlaces+"f",a[i]));
			if ( (i+1)<a.length ) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}
}
