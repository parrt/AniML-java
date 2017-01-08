/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

public class DataPair {
	public DataTable region1;
	public DataTable region2;

	public DataPair(DataTable region1, DataTable b) {
		this.region1 = region1;
		this.region2 = b;
	}
}
