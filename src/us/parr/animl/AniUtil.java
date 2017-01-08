/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static us.parr.animl.classifiers.RandomForest.INVALID_CATEGORY;

public class AniUtil {
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

	public static <T> T findFirst(List<T> data, Predicate<T> pred) {
		if ( data!=null ) for (T x : data) {
			if ( pred.test(x) ) {
				return x;
			}
		}
		return null;
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
}
