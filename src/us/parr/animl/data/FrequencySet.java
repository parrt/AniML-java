/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Count how many of each key we have; not thread safe */
public class FrequencySet<T> extends HashMap<T, MutableInt> {
	public FrequencySet() {
	}

	public FrequencySet(FrequencySet<T> old) {
		for (T key : old.keySet()) {
			put(key, new MutableInt(old.get(key).v)); // make sure MutableInts are copied deeply
		}
	}

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

	/** Return a new set containing a[i]-b[i] for all keys i. Values in b
	 *  but not in a are ignored.  Values in a but not in b yield a's same value
	 *  in the result.
	 */
	public static <T> FrequencySet<T> minus(FrequencySet<T> a, FrequencySet<T> b) {
		FrequencySet<T> r = new FrequencySet<T>(a);
		for (T key : r.keySet()) {
			MutableInt bI = b.get(key);
			if ( bI!=null ) {
				r.put(key, new MutableInt(r.get(key).v - bI.v)); // can't alter any MutableInts
			}
		}
		return r;
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
