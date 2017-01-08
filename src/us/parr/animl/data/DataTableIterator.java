/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

import java.util.Iterator;

class DataTableIterator implements Iterator<int[]> {
	protected final DataTable table;
	protected int i = 0; // next element to return

	@Override
	public boolean hasNext() { return i<table.size(); }

	@Override
	public int[] next() { return table.get(i++); }

	public DataTableIterator(DataTable table) {this.table = table;	}
}
