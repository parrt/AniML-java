/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl;

import static java.lang.Math.log;

public class AniMath {
	public static boolean isClose(double a, double b) {
		boolean result = Math.abs(a-b)<0.000000001;
		return result;
	}

	public static double log2(double p) {
		return log(p) / log(2.0); // log2(x) = log(x)/log(2)
	}
}
