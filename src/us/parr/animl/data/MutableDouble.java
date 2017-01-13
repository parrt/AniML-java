/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

public class MutableDouble extends Number implements Comparable<Number> {
	public double v;

	public MutableDouble(double v) { this.v = v; }

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Number ) {
			return Double.compare(v, ((Number)o).doubleValue())==0;
		}
		return false;
	}

	@Override public int hashCode() { return Double.hashCode(v); }

	@Override public int compareTo(Number o) { return Double.compare(v, o.floatValue()); }
	@Override public int intValue() { return (int)v; }
	@Override public long longValue() { return (long)v; }
	@Override public float floatValue() { return (float)v; }
	@Override public double doubleValue() { return v; }

	@Override
	public String toString() {
		return String.valueOf(v);
	}
}
