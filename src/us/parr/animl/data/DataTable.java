/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import us.parr.animl.AniStats;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import static us.parr.animl.AniUtils.join;
import static us.parr.animl.AniUtils.map;
import static us.parr.animl.data.DataTable.VariableFormat.CENTER;
import static us.parr.animl.data.DataTable.VariableFormat.RIGHT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.PREDICTED_CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.PREDICTED_CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;

public class DataTable implements Iterable<int[]> {
	public enum VariableType {
		CATEGORICAL_INT, CATEGORICAL_STRING, NUMERICAL_INT, NUMERICAL_FLOAT,
		PREDICTED_CATEGORICAL_INT, PREDICTED_CATEGORICAL_STRING,
		UNUSED_INT
	}
	public enum VariableFormat {
		LEFT, CENTER, RIGHT
	}

	public static final String[] varTypeShortNames = new String[VariableType.values().length];
	public static final VariableFormat[] defaultVarFormats = new VariableFormat[VariableType.values().length];
	static {
		varTypeShortNames[CATEGORICAL_INT.ordinal()] = "cat";
		varTypeShortNames[CATEGORICAL_STRING.ordinal()] = "string";
		varTypeShortNames[NUMERICAL_INT.ordinal()] = "int";
		varTypeShortNames[NUMERICAL_FLOAT.ordinal()] = "float";
		varTypeShortNames[PREDICTED_CATEGORICAL_INT.ordinal()] = "predicted";
		varTypeShortNames[PREDICTED_CATEGORICAL_STRING.ordinal()] = "predicted-string";
		varTypeShortNames[UNUSED_INT.ordinal()] = "unused";

		defaultVarFormats[CATEGORICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[CATEGORICAL_STRING.ordinal()] = CENTER;
		defaultVarFormats[NUMERICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[NUMERICAL_FLOAT.ordinal()] = RIGHT;
		defaultVarFormats[PREDICTED_CATEGORICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[PREDICTED_CATEGORICAL_STRING.ordinal()] = CENTER;
		defaultVarFormats[UNUSED_INT.ordinal()] = CENTER;
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
			if ( colTypes[j]==CATEGORICAL_STRING || colTypes[j]==PREDICTED_CATEGORICAL_STRING ) {
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
					case PREDICTED_CATEGORICAL_INT :
						col = Integer.valueOf(colValue);
						break;
					case CATEGORICAL_STRING :
					case PREDICTED_CATEGORICAL_STRING :
						col = colStringToIntMap[j].add(colValue);
						break;
					case NUMERICAL_FLOAT :
						col = Float.floatToIntBits(Float.valueOf(colValue));
						break;
				}
				rowAsInts[j] = col;
			}
			rows2.add(rowAsInts);
		}
		DataTable t = new DataTable(rows2, colTypes, colNames);
		t.colStringToIntMap = colStringToIntMap;
		return t;
	}

	public static DataTable loadCSV(String fileName, String formatType, VariableType[] colTypesOverride, String[] colNamesOverride, boolean hasHeaderRow) {
		try {
			// use apache commons io + csv to load but convert to list of String[]
			// byte-order markers are handled if present at start of file.
			FileInputStream fis = new FileInputStream(fileName);
			final Reader reader = new InputStreamReader(new BOMInputStream(fis), "UTF-8");
			CSVFormat format;
			switch ( formatType.toLowerCase() ) {
				case "tsv" :
					format = hasHeaderRow ? CSVFormat.TDF.withHeader() : CSVFormat.TDF;
					break;
				case "mysql" :
					format = hasHeaderRow ? CSVFormat.MYSQL.withHeader() : CSVFormat.MYSQL;
					break;
				case "excel" :
					format = hasHeaderRow ? CSVFormat.EXCEL.withHeader() : CSVFormat.EXCEL;
					break;
				case "rfc4180" :
				default :
					format = hasHeaderRow ? CSVFormat.RFC4180.withHeader() : CSVFormat.RFC4180;
					break;
			}
			final CSVParser parser = new CSVParser(reader, format);
			List<String[]> rows = new ArrayList<>();
			int numHeaderNames = parser.getHeaderMap().size();
			VariableType[] actualTypes = new VariableType[numHeaderNames];
			for (int j = 0; j<numHeaderNames; j++) {
				actualTypes[j] = NUMERICAL_INT; // assume all types are int at first
			}
			try {
			    for (final CSVRecord record : parser) {
			    	String[] row = new String[record.size()];
				    for (int j = 0; j<record.size(); j++) {
					    row[j] = record.get(j);
					    if ( StringUtils.isNumeric(row[j]) && row[j].contains(".") ) {
					    	actualTypes[j] = NUMERICAL_FLOAT;
					    }
					    else if ( StringUtils.isAlphanumericSpace(row[j]) ) {
					    	if ( j==record.size()-1 ) {
							    actualTypes[j] = PREDICTED_CATEGORICAL_STRING;
						    }
						    else {
							    actualTypes[j] = CATEGORICAL_STRING;
						    }
					    }
				    }
				    rows.add(row);
			    }
			}
			finally {
			    parser.close();
			    reader.close();
			}

			Set<String> colNameSet = parser.getHeaderMap().keySet();
			String[] colNames = colNameSet.toArray(new String[colNameSet.size()]);
			if ( colNamesOverride!=null ) {
				colNames = colNamesOverride;
			}
			if ( colTypesOverride!=null ) {
				actualTypes = colTypesOverride;
			}
			return fromStrings(rows, actualTypes, colNames, false);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Can't open and/or read "+fileName, e);
		}
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
			   colTypes[colIndex]==PREDICTED_CATEGORICAL_INT ||
			   colTypes[colIndex]==PREDICTED_CATEGORICAL_STRING) )
		{
			throw new IllegalArgumentException(colNames[colIndex]+" is not an int-based column; type is "+colTypes[colIndex]);
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
			case CATEGORICAL_STRING : // strings are encoded as ints
			case PREDICTED_CATEGORICAL_STRING :
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
			case PREDICTED_CATEGORICAL_INT :
				return row[colj];
			case CATEGORICAL_STRING :
			case PREDICTED_CATEGORICAL_STRING :
				return colStringToIntMap[colj].get(row[colj]);
			case NUMERICAL_FLOAT :
				return Float.intBitsToFloat(row[colj]);
			default :
				throw new IllegalArgumentException(colNames[colj]+" has invalid type: "+colTypes[colj]);
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
			case CATEGORICAL_STRING: // strings are encoded as ints
			case PREDICTED_CATEGORICAL_STRING :
			case PREDICTED_CATEGORICAL_INT :
				return Integer.compare(get(rowi, colIndex), get(rowj, colIndex));
			case NUMERICAL_FLOAT:
				return Float.compare(getAsFloat(rowi, colIndex), getAsFloat(rowj, colIndex));
			default :
				throw new IllegalArgumentException(colNames[colIndex]+" has invalid type: "+colTypes[colIndex]);
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
			if ( colTypes[i]==PREDICTED_CATEGORICAL_INT || colTypes[i]==PREDICTED_CATEGORICAL_STRING ) {
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
			colTypes[i] = NUMERICAL_INT;
		}
		colTypes[dim-1] = PREDICTED_CATEGORICAL_INT;
		return colTypes;
	}

	public String toTestString() {
		StringBuilder buf = new StringBuilder();
		if ( colNames!=null ) {
			List<String> strings = map(colNames, Object::toString);
			if ( colTypes!=null ) {
				for (int j = 0; j<strings.size(); j++) {
					strings.set(j, strings.get(j)+"("+varTypeShortNames[colTypes[j].ordinal()]+")");
				}
			}
			buf.append(join(strings, ", "));
			buf.append("\n");
		}
		for (int i = 0; i<rows.size(); i++) {
			Object[] values = getValues(i);
			buf.append(join(values, ", "));
			buf.append("\n");
		}
		return buf.toString();
	}

	@Override
	public String toString() {
		return toString(defaultVarFormats);
	}

	public String toString(VariableFormat[] colFormats) {
		StringBuilder buf = new StringBuilder();
		List<Integer> colWidths = map(colNames, n -> n.length());
		// compute column widths as max of col name or widest value in column
		for (int j = 0; j<colWidths.size(); j++) {
			int w = Math.max(colWidths.get(j), getColumnMaxWidth(j));
			colWidths.set(j, w);
			String name = StringUtils.center(colNames[j], w);
			if ( j>0 ) {
				buf.append(" ");
			}
			buf.append(name);
		}
		buf.append("\n");
		for (int i = 0; i<rows.size(); i++) {
			Object[] values = getValues(i);
			for (int j = 0; j<colWidths.size(); j++) {
				int colWidth = colWidths.get(j);
				String colValue = values[j].toString();
				switch ( colFormats[colTypes[j].ordinal()] ) {
					case LEFT :
						colValue = String.format("%-"+colWidth+"s", colValue);
						break;
					case CENTER :
						colValue = StringUtils.center(colValue, colWidth);
						break;
					case RIGHT :
						colValue = String.format("%"+colWidth+"s", colValue);
						break;
				}
				if ( j>0 ) {
					buf.append(" ");
				}
				buf.append(colValue);
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	public int getColumnMaxWidth(int colIndex) {
		int w = 0;
		// scan column, find max width
		for (int i = 0; i<rows.size(); i++) {
			String v = getValue(i, colIndex).toString();
			if ( v.length()>w ) {
				w = v.length();
			}
		}
		return w;
	}
}
