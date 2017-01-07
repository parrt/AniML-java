package us.parr.rf.data;

import java.util.Iterator;

class DataFrameIterator implements Iterator<int[]> {
	protected final DataFrame table;
	protected int i = 0; // next element to return

	@Override
	public boolean hasNext() { return i<table.size(); }

	@Override
	public int[] next() { return table.get(i++); }

	public DataFrameIterator(DataFrame table) {	this.table = table;	}
}
