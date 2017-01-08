/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

import us.parr.animl.AniStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.PREDICTED_CATEGORICAL_INT;

public class DataTable implements Iterable<int[]> {
	enum VariableType {
		CATEGORICAL_INT, CATEGORICAL_STRING, NUMERICAL_INT, NUMERICAL_FLOAT,
		PREDICTED_CATEGORICAL_INT,
		UNUSED_INT
	}

	protected List<int[]> rows;
	protected String[] colNames;
	protected VariableType[] colTypes;
	protected StringTable[] colStringToIntMap;

	public DataTable() {
	}

	public DataTable(List<int[]> rows, VariableType[] colTypes, String[] colNames) {
		this.rows = rows;
		this.colNames = colNames;
		this.colTypes = colTypes;
	}

	public static DataTable empty(VariableType[] colTypes, String[] colNames) {
		return new DataTable(new ArrayList<>(), colTypes, colNames);
	}

	public static DataTable fromInts(List<int[]> rows, VariableType[] colTypes, String[] colNames) {
		if ( rows==null ) return null;
		if ( rows.size()==0 && colTypes==null ) {
			return null;
		}

		int dim = rows.size()>0 ? rows.get(0).length : colTypes.length;
		if ( colTypes==null ) {
			colTypes = getDefaultColTypes(dim);
		}
		if ( colNames==null ) {
			colNames = getDefaultColNames(colTypes, dim);
		}
		return new DataTable(rows, colTypes, colNames);
	}

	public static DataTable fromStrings(List<String[]> rows, VariableType[] colTypes, String[] colNames, boolean hasHeaderRow) {
		if ( rows==null || rows.size()==0 ) return empty(colTypes, colNames);
		if ( rows.size()==1 && hasHeaderRow ) {
			return empty(colTypes, colNames);
		}

		if ( hasHeaderRow && colNames==null ) {
			colNames = rows.get(0);
		}

		int dim = rows.get(0).length;
		if ( colTypes==null ) {
			colTypes = getDefaultColTypes(dim);
		}
		if ( colNames==null ) {
			colNames = getDefaultColNames(colTypes, dim);
		}
		StringTable[] colStringToIntMap = new StringTable[colTypes.length];
		// don't waste space on string tables unless we need to
		for (int j = 0; j < colTypes.length; j++) {
			if ( colTypes[j]== CATEGORICAL_STRING ) {
				colStringToIntMap[j] = new StringTable();
			}
		}
		// process strings into ints using appropriate conversion
		List<int[]> rows2 = new ArrayList<>();
		for (int i = hasHeaderRow ? 1 : 0; i < rows.size(); i++) {
			String[] row = rows.get(i);
			int[] rowAsInts = new int[row.length];
			for (int j = 0; j < row.length; j++) {
				int col=0;
				VariableType colType = colTypes[j];
				String colValue = row[j];
				switch ( colType ) {
					case CATEGORICAL_INT :
					case NUMERICAL_INT :
					case UNUSED_INT :
						col = Integer.valueOf(colValue);
						break;
					case CATEGORICAL_STRING :
						col = colStringToIntMap[j].add(colValue);
						break;
					case NUMERICAL_FLOAT :
						col = Float.floatToIntBits(Float.valueOf(colValue));
						break;
				}
				rowAsInts[i] = col;
			}
			rows2.add(rowAsInts);
		}
		DataTable t = new DataTable(rows2, colTypes, colNames);
		t.colStringToIntMap = colStringToIntMap;
		return t;
	}

	public Set<Integer> uniqueValues(int colIndex) {
		FrequencySet<Integer> valueCounts = valueCountsInColumn(colIndex);
		return valueCounts.keySet();
	}

	public double entropy(int colIndex) {
		FrequencySet<Integer> valueCounts = valueCountsInColumn(colIndex);
		return AniStats.entropy(valueCounts.counts());
	}

	/** Create a set that counts how many of each value in colIndex there is. Only
	 *  works on int-valued columns.
	 */
	public FrequencySet<Integer> valueCountsInColumn(int colIndex) {
		FrequencySet<Integer> valueCounts = new FrequencySet<>();
		if ( !(colTypes[colIndex]==NUMERICAL_INT ||
			   colTypes[colIndex]==CATEGORICAL_INT ||
			   colTypes[colIndex]==CATEGORICAL_STRING ||
		       colTypes[colIndex]==PREDICTED_CATEGORICAL_INT) )
		{
			throw new IllegalArgumentException(colNames[colIndex]+" is not an int-based column");
		}
		for (int i = 0; i<size(); i++) { // for each row, count different values for col splitVariable
			int[] row = get(i);
			int col = row[colIndex];
			valueCounts.add(col);
		}
		return valueCounts;
	}

	public void sortBy(int colIndex) {
		switch ( colTypes[colIndex] ) {
			case CATEGORICAL_INT :
			case NUMERICAL_INT :
			case UNUSED_INT :
			case CATEGORICAL_STRING : // these are encoded as ints
			case PREDICTED_CATEGORICAL_INT :
				Collections.sort(rows, (ra, rb) -> {
					return Integer.compare(ra[colIndex], rb[colIndex]);
				});
				break;
			case NUMERICAL_FLOAT :
				Collections.sort(rows, (ra, rb) -> {
					return Float.compare(Float.intBitsToFloat(ra[colIndex]),
										 Float.intBitsToFloat(rb[colIndex]));
				});
				break;
		}
	}

	public int size() { return rows.size(); }

	public int[] get(int i) {
		return rows.get(i);
	}

	public int get(int i, int j) {
		return getAsInt(i,j);
	}

	public Object getValue(int rowi, int colj) {
		int[] row = rows.get(rowi);
		switch ( colTypes[colj] ) {
			case CATEGORICAL_INT :
			case NUMERICAL_INT :
			case UNUSED_INT :
				return row[colj];
			case CATEGORICAL_STRING :
				return colStringToIntMap[colj].get(row[colj]);
			case NUMERICAL_FLOAT :
				return Float.intBitsToFloat(row[colj]);
			default :
				throw new IllegalArgumentException(colNames[colj]+" has invalid type");
		}
	}

	public Object[] getValues(int rowi) {
		int dim = colTypes.length;
		Object[] o = new Object[dim];
		for (int j = 0; j<dim; j++) {
			o[j] = getValue(rowi, j);
		}
		return o;
	}

	public int getAsInt(int i, int j) {
		return rows.get(i)[j];
	}

	public float getAsFloat(int i, int j) {
		return Float.intBitsToFloat(rows.get(i)[j]);
	}

	public int compare(int rowi, int rowj, int colIndex) {
		switch (colTypes[colIndex]) {
			case CATEGORICAL_INT:
			case NUMERICAL_INT:
			case UNUSED_INT:
			case CATEGORICAL_STRING: // these are encoded as ints
			case PREDICTED_CATEGORICAL_INT :
				return Integer.compare(get(rowi, colIndex), get(rowj, colIndex));
			case NUMERICAL_FLOAT:
				return Float.compare(getAsFloat(rowi, colIndex), getAsFloat(rowj, colIndex));
			default :
				throw new IllegalArgumentException(colNames[colIndex]+" has invalid type");
		}
	}

	@Override
	public Iterator<int[]> iterator() {
		return new DataTableIterator(this);
	}

	@Override
	public void forEach(Consumer<? super int[]> action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Spliterator<int[]> spliterator() {
		throw new UnsupportedOperationException();
	}

	public static String[] getDefaultColNames(VariableType[] colTypes, int dim) {
		String[] colNames;
		colNames = new String[dim];
		for (int i = 0; i < dim; i++) {
			if ( colTypes[i]==PREDICTED_CATEGORICAL_INT ) {
				colNames[i] = "y";
			}
			else {
				colNames[i] = "x" + i;
			}
		}
		return colNames;
	}

	public static VariableType[] getDefaultColTypes(int dim) {
		VariableType[] colTypes;
		colTypes = new VariableType[dim];
		for (int i = 0; i<dim-1; i++) {
			colTypes[i] = NUMERICAL_FLOAT;
		}
		colTypes[dim-1] = PREDICTED_CATEGORICAL_INT;
		return colTypes;
	}

	public String toTestString() {
		StringBuilder buf = new StringBuilder();
		buf.append(Arrays.toString(colNames));
		buf.append("\n");
		for (int i = 0; i<rows.size(); i++) {
			Object[] values = getValues(i);
			buf.append(Arrays.toString(values));
			buf.append("\n");
		}
		return buf.toString();
	}

}
