/*
 * Copyright (c) 2012-2016 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package us.parr.animl.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Count how many of each key we have; not thread safe */
public class FrequencySet<T> extends HashMap<T, MutableInt> {
	public int count(T key) {
		MutableInt value = get(key);
		if (value == null) return 0;
		return value.v;
	}

	public void add(T key) {
		MutableInt value = get(key);
		if (value == null) {
			value = new MutableInt(1);
			put(key, value);
		}
		else {
			value.v++;
		}
	}

	public List<Integer> counts() {
		List<Integer> counts = new ArrayList<>();
		for (MutableInt i : values()) {
			counts.add(i.v);
		}
		return counts;
	}

	public List<T> keys() {
		List<T> keys = new ArrayList<>();
		keys.addAll(keySet());
		return keys;
	}

	/** Return the key with the max count; tie goes to first cat at max found. */
	public T argmax() {
		T keyOfMax = null;
		for (T key : keySet()) {
			if ( keyOfMax==null ) { // initial condition
				keyOfMax = key;
				continue;
			}
			if ( count(key)>count(keyOfMax) ) keyOfMax = key;
		}
		return keyOfMax;
	}
}
