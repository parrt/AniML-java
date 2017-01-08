/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

import java.util.List;

public class DataPair {
	public List<int[]> region1;
	public List<int[]> region2;

	public DataPair(List<int[]> region1, List<int[]> b) {
		this.region1 = region1;
		this.region2 = b;
	}
}
