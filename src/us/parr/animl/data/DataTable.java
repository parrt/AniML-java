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
import us.parr.lib.collections.CountingDenseIntSet;
import us.parr.lib.collections.CountingSet;
import us.parr.lib.collections.DenseIntSet;
import us.parr.lib.collections.ParrtCollections;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Collections.max;
import static us.parr.animl.data.DataTable.VariableFormat.CENTER;
import static us.parr.animl.data.DataTable.VariableFormat.RIGHT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.INVALID;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_STRING;
import static us.parr.lib.collections.ParrtCollections.indexOf;
import static us.parr.lib.collections.ParrtCollections.join;
import static us.parr.lib.collections.ParrtCollections.map;

public class DataTable implements Iterable<int[]> {
	public static final Pattern floatPattern = Pattern.compile("[0-9]+\\.[0-9]*|\\.[0-9]+");

	/** Input sometimes has NA or blanks for unknown values */
	public static final Set<String> UNKNOWN_VALUE_STRINGS = new HashSet<String>() {{
		add("");
		add("NA");
		add("N/A");
	}};

	public enum VariableType {
		CATEGORICAL_INT, CATEGORICAL_STRING, NUMERICAL_INT, NUMERICAL_FLOAT,
		TARGET_CATEGORICAL_INT, TARGET_CATEGORICAL_STRING,
		UNUSED_INT,
		UNUSED_FLOAT,
		UNUSED_STRING,
		INVALID
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
		varTypeShortNames[TARGET_CATEGORICAL_INT.ordinal()] = "target";
		varTypeShortNames[TARGET_CATEGORICAL_STRING.ordinal()] = "target-string";
		varTypeShortNames[UNUSED_INT.ordinal()] = "unused";
		varTypeShortNames[UNUSED_FLOAT.ordinal()] = "unused";
		varTypeShortNames[UNUSED_STRING.ordinal()] = "unused";

		defaultVarFormats[CATEGORICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[CATEGORICAL_STRING.ordinal()] = CENTER;
		defaultVarFormats[NUMERICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[NUMERICAL_FLOAT.ordinal()] = RIGHT;
		defaultVarFormats[TARGET_CATEGORICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[TARGET_CATEGORICAL_STRING.ordinal()] = CENTER;
		defaultVarFormats[UNUSED_INT.ordinal()] = RIGHT;
		defaultVarFormats[UNUSED_FLOAT.ordinal()] = RIGHT;
		defaultVarFormats[UNUSED_STRING.ordinal()] = CENTER;
	}

	// TODO: this should be int[j][i] stored in columnar form; first index is the column then it goes down rows in that column
	protected List<int[]> rows;
	protected String[] colNames;
	protected VariableType[] colTypes;
	protected StringTable[] colStringToIntMap;
	protected int[] colMaxes;

	protected Set<Integer> cachedPredictionCategories;
	protected int cachedMaxPredictionCategoryValue = -1;

	public DataTable() {
	}

	public DataTable(List<int[]> rows, VariableType[] colTypes, String[] colNames, int[] colMaxes) {
		this(rows, colTypes, colNames, colMaxes, null);
	}

	public DataTable(List<int[]> rows, VariableType[] colTypes, String[] colNames, int[] colMaxes, StringTable[] colStringToIntMap) {
		this.rows = rows;
		this.colMaxes = colMaxes;
		this.colNames = colNames;
		this.colTypes = colTypes;
		this.colStringToIntMap = colStringToIntMap;
		if ( this.colMaxes==null ) {
			computeColMaxes();
		}
	}

	public static DataTable empty(VariableType[] colTypes, String[] colNames) {
		return new DataTable(new ArrayList<>(), colTypes, colNames, null, null);
	}

	/** Make a new table from an old table with a subset of rows */
	public DataTable(DataTable old, List<int[]> rows) {
		this(rows, old.colTypes, old.colNames, old.colMaxes, old.colStringToIntMap);
	}

	/** Make a new table from an old table with shallow copy of rows */
	public DataTable(DataTable old) {
		this.rows = new ArrayList<>(old.rows.size());
		this.rows.addAll(old.rows);
		this.colNames = old.colNames;
		System.arraycopy(old.colMaxes, 0, this.colMaxes, 0, old.colMaxes.length);
		this.colTypes = old.colTypes;
		this.colStringToIntMap = old.colStringToIntMap;
	}

	public static DataTable fromInts(List<int[]> rows, VariableType[] colTypes, String[] colNames) {
		if ( rows==null ) return empty(colTypes, colNames);
		if ( rows.size()==0 && colTypes==null ) {
			return empty(colTypes, colNames);
		}

		int dim = rows.size()>0 ? rows.get(0).length : colTypes.length;
		if ( colTypes==null ) {
			colTypes = getDefaultColTypes(dim);
		}
		if ( colNames==null ) {
			colNames = getDefaultColNames(colTypes, dim);
		}
		return new DataTable(rows, colTypes, colNames, null);
	}

	public static DataTable fromStrings(List<String[]> rows) {
		if ( rows==null ) return empty(null, null);
		if ( rows.size()==0 ) {
			return empty(null, null);
		}
		String[] headerRow = rows.get(0);
		if ( headerRow==null ) {
			return empty(null, null);
		}
		int numCols = headerRow.length;
		if ( rows.size()==1 ) { // just header row?
			return empty(null, headerRow);
		}

		rows = rows.subList(1, rows.size()); // don't use first row.

		VariableType[] actualTypes = computeColTypes(rows, numCols);

		return fromStrings(rows, actualTypes, headerRow, false);
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
			if ( colTypes[j]==CATEGORICAL_STRING || colTypes[j]==TARGET_CATEGORICAL_STRING ) {
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
					case UNUSED_INT:
					case TARGET_CATEGORICAL_INT:
						if ( !UNKNOWN_VALUE_STRINGS.contains(row[j]) ) {
							col = Integer.valueOf(colValue);
						}
						break;
					case CATEGORICAL_STRING :
					case TARGET_CATEGORICAL_STRING:
					case UNUSED_STRING :
						if ( !UNKNOWN_VALUE_STRINGS.contains(row[j]) ) {
							col = colStringToIntMap[j].add(colValue);
						}
						break;
					case NUMERICAL_FLOAT :
					case UNUSED_FLOAT :
						if ( !UNKNOWN_VALUE_STRINGS.contains(row[j]) ) {
							col = Float.floatToIntBits(Float.valueOf(colValue));
						}
						break;
				}
				rowAsInts[j] = col;
			}
			rows2.add(rowAsInts);
		}
		DataTable t = new DataTable(rows2, colTypes, colNames, null);
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
			if ( formatType==null ) {
				format = hasHeaderRow ? CSVFormat.RFC4180.withHeader() : CSVFormat.RFC4180;
			}
			else {
				switch ( formatType.toLowerCase() ) {
					case "tsv":
						format = hasHeaderRow ? CSVFormat.TDF.withHeader() : CSVFormat.TDF;
						break;
					case "mysql":
						format = hasHeaderRow ? CSVFormat.MYSQL.withHeader() : CSVFormat.MYSQL;
						break;
					case "excel":
						format = hasHeaderRow ? CSVFormat.EXCEL.withHeader() : CSVFormat.EXCEL;
						break;
					case "rfc4180":
					default:
						format = hasHeaderRow ? CSVFormat.RFC4180.withHeader() : CSVFormat.RFC4180;
						break;
				}
			}
			final CSVParser parser = new CSVParser(reader, format);
			List<String[]> rows = new ArrayList<>();
			int numHeaderNames = parser.getHeaderMap().size();
			try {
				for (final CSVRecord record : parser) {
					String[] row = new String[record.size()];
					for (int j = 0; j<record.size(); j++) {
						row[j] = record.get(j);
					}
					rows.add(row);
				}
			}
			finally {
				parser.close();
				reader.close();
			}

			VariableType[] actualTypes = computeColTypes(rows, numHeaderNames);

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

	private static VariableType[] computeColTypes(List<String[]> rows, int numCols) {
		VariableType[] actualTypes = new VariableType[numCols];
		for (int j = 0; j<numCols; j++) {
			actualTypes[j] = INVALID;
		}
		for (String[] row : rows) {
			for (int j = 0; j<numCols; j++) {
				if ( StringUtils.isNumeric(row[j]) ) {
					if ( actualTypes[j]==INVALID ) { // only choose int if first type seen
						actualTypes[j] = NUMERICAL_INT;
					}
				}
				else if ( floatPattern.matcher(row[j]).find() ) { // let int become float but not vice versa
					if ( actualTypes[j]==INVALID || actualTypes[j]==NUMERICAL_INT ) {
						actualTypes[j] = NUMERICAL_FLOAT;
					}
				}
				else { // anything else is a string
					if ( !UNKNOWN_VALUE_STRINGS.contains(row[j]) ) { // if NA, N/A don't know type
						// if we ever see a string, convert and don't change back
						if ( actualTypes[j]==INVALID || actualTypes[j]==NUMERICAL_INT ) {
							if ( j==row.length-1 ) { // assume last column is predicted var
								actualTypes[j] = TARGET_CATEGORICAL_STRING;
							}
							else {
								actualTypes[j] = CATEGORICAL_STRING;
							}
						}
					}
				}
			}
		}
		return actualTypes;
	}

	public void computeColMaxes() {
		if ( colTypes==null ) return;;
		this.colMaxes = new int[colTypes.length];
		for (int j = 0; j<getNumberOfColumns(); j++) {
			VariableType colType = colTypes[j];
			int max = 0;
			for (int i = 0; i<size(); i++) {
				int[] row = getRow(i);
				if ( compare(row[j], max, colType)==1 ) {
					max = row[j];
				}
			}
			colMaxes[j] = max;
		}
	}

	public int getMaxPredictionCategoryValue() {
		if ( cachedMaxPredictionCategoryValue == -1 ) {
			cachedMaxPredictionCategoryValue = max(getPredictionCategories());
		}
		return cachedMaxPredictionCategoryValue;
	}

	public Set<Integer> getPredictionCategories() {
		if ( cachedPredictionCategories==null ) {
			cachedPredictionCategories = getUniqueValues(getPredictedCol());
		}
		return cachedPredictionCategories;
	}

	public Set<Integer> getUniqueValues(int colIndex) {
		DenseIntSet values = new DenseIntSet(colMaxes[colIndex]);
		for (int i = 0; i<size(); i++) { // for each row, count different values for col splitVariable
			values.add( getAsInt(i,colIndex) ); // pretend everything is an int
		}
		return values;
	}

	public DataTable filter(Predicate<int[]> pred) {
		List<int[]> filtered = ParrtCollections.filter(rows, pred);
		return new DataTable(this, filtered);
	}

	public double entropy(int colIndex) {
		CountingSet<Integer> valueCounts = valueCountsInColumn(colIndex);
		return valueCounts.entropy();
	}

	public List<Integer> getSubsetOfVarIndexes(int m, Random random) {
		// create set of all predictor vars
		List<Integer> indexes = new ArrayList<>(colTypes.length);
		for (int i = 0; i<colTypes.length; i++) {
			if ( isPredictorVar(colTypes[i]) ) {
				indexes.add(i);
			}
		}
		int M = indexes.size(); // number of usable predictor variables M
		if ( m<=0 ) m = M;
		if ( m>M ) m = M;
		if ( m==M ) {
			// don't bother to shuffle then sort
			return indexes;
		}
		if ( random==null ) {
			random = new Random();
		}
		Collections.shuffle(indexes, random);
		indexes = indexes.subList(0, m);
		Collections.sort(indexes);
		return indexes;
	}

	/** Return new table with [i1..i2] inclusive in new table */
	public DataTable subset(int i1, int i2) {
		return new DataTable(this, rows.subList(i1, i2+1));
	}

	/** Return new table with all data except [i1..i2] inclusive in new table */
	public DataTable subsetNot(int i1, int i2) {
		List<int[]> missingChunk = new ArrayList<>();
		for (int i = 0; i<i1; i++) {
			missingChunk.add(rows.get(i));
		}
		for (int i = i2+1; i<rows.size(); i++) {
			missingChunk.add(rows.get(i));
		}
		return new DataTable(this, missingChunk);
	}

	/** Return new table with row i missing from table; makes shallow copy to do so. */
	public DataTable subsetNot(int i) {
		List<int[]> lessOne = new ArrayList<>();
		lessOne.addAll(rows);
		lessOne.remove(i);
		return new DataTable(this, lessOne);
	}

	public int getNumberOfPredictorVar() { return getSubsetOfVarIndexes(getNumberOfColumns(), null).size(); }

	public int getNumberOfColumns() { return rows.get(0).length; }

	public static boolean isPredictorVar(VariableType colType) {
		return
			!(
				colType==UNUSED_INT ||
				colType==UNUSED_FLOAT ||
				colType==UNUSED_STRING ||
				colType==TARGET_CATEGORICAL_INT ||
				colType==TARGET_CATEGORICAL_STRING
			);
	}

	public static boolean isCategoricalVar(VariableType colType) {
		return
			colType==DataTable.VariableType.CATEGORICAL_INT ||
			colType==DataTable.VariableType.CATEGORICAL_STRING;
	}

	/** Create a set that counts how many of each value in colIndex there is. Only
	 *  works on int-valued columns.
	 */
	public CountingSet<Integer> valueCountsInColumn(int colIndex) {
		CountingSet<Integer> valueCounts = new CountingDenseIntSet(colMaxes[colIndex]);
		if ( !(colTypes[colIndex]==NUMERICAL_INT ||
			colTypes[colIndex]==CATEGORICAL_INT ||
			colTypes[colIndex]==CATEGORICAL_STRING ||
			colTypes[colIndex]==TARGET_CATEGORICAL_INT ||
			colTypes[colIndex]==TARGET_CATEGORICAL_STRING) )
		{
			throw new IllegalArgumentException(colNames[colIndex]+" is not an int-based column; type is "+colTypes[colIndex]);
		}
		for (int i = 0; i<size(); i++) { // for each row, count different values for col splitVariable
			int[] row = getRow(i);
			int col = row[colIndex];
			valueCounts.add(col);
		}
		return valueCounts;
	}

	public void sortBy(int colIndex) {
		switch ( colTypes[colIndex] ) {
			case CATEGORICAL_INT :
			case NUMERICAL_INT :
			case CATEGORICAL_STRING : // strings are encoded as ints
			case TARGET_CATEGORICAL_STRING:
			case TARGET_CATEGORICAL_INT:
			case UNUSED_INT :
			case UNUSED_STRING :
				Collections.sort(rows, (ra, rb) -> {
					return Integer.compare(ra[colIndex], rb[colIndex]);
				});
				break;
			case NUMERICAL_FLOAT :
			case UNUSED_FLOAT :
				Collections.sort(rows, (ra, rb) -> {
					return Float.compare(Float.intBitsToFloat(ra[colIndex]),
					                     Float.intBitsToFloat(rb[colIndex]));
				});
				break;
		}
	}

	public void shuffle(Random random) {
		Collections.shuffle(rows, random);
	}

	public int size() { return rows.size(); }

	/** Return the data[i,j] item as an appropriate object: Integer, Float, String */
	public Object get(int i, int j) {
		return getValue(i,j);
	}

	public int getAsInt(int i, int j) {
		return rows.get(i)[j];
	}

	public float getAsFloat(int i, int j) {
		return getAsFloat(rows.get(i)[j]);
	}

	public static float getAsFloat(int a) {
		return Float.intBitsToFloat(a);
	}

	public int[] getRow(int i) { return rows.get(i); }
	public void removeRow(int i) { rows.remove(i); }

	public List<int[]> getRows() { return rows; }

	public String[] getColNames() {
		return colNames;
	}

	public VariableType[] getColTypes() {
		return colTypes;
	}

	public void setColTypes(VariableType[] colTypes) {
		this.colTypes = colTypes;
	}

	public void setColType(int colIndex, VariableType colType) {
		this.colTypes[colIndex] = colType;
	}

	public void setColType(String colName, VariableType colType) {
		int j = indexOf(colNames, colName);
		if ( j>=0 && j<colTypes.length ) {
			this.colTypes[j] = colType;
		}
		else {
			throw new IllegalArgumentException("Column "+colName+" unknown");
		}
	}

	public Number getColMax(int j) {
		if ( colTypes[j]==NUMERICAL_FLOAT ) {
			return getAsFloat(colMaxes[j]);
		}
		return colMaxes[j];
	}

	public Object getValue(int rowi, int colj) {
		int[] row = this.rows.get(rowi);
		return getValue(this, row[colj], colj);
	}

	/** Return an object representing the true value of 'value'
	 *  relative to colj in table 'data'.
	 */
	public static Object getValue(DataTable data, int value, int colj) {
		switch ( data.colTypes[colj] ) {
			case CATEGORICAL_INT :
			case NUMERICAL_INT :
			case TARGET_CATEGORICAL_INT:
			case UNUSED_INT:
				return value;
			case CATEGORICAL_STRING :
			case TARGET_CATEGORICAL_STRING:
			case UNUSED_STRING :
				return data.colStringToIntMap[colj].get(value);
			case NUMERICAL_FLOAT :
			case UNUSED_FLOAT :
				return Float.intBitsToFloat(value);
			default :
				throw new IllegalArgumentException(data.colNames[colj]+" has invalid type: "+data.colTypes[colj]);
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

	public int compare(int rowi, int rowj, int colIndex) {
		VariableType colType = colTypes[colIndex];
		switch ( colType ) {
			case CATEGORICAL_INT:
			case NUMERICAL_INT:
			case CATEGORICAL_STRING: // strings are encoded as ints
			case TARGET_CATEGORICAL_STRING:
			case TARGET_CATEGORICAL_INT:
			case UNUSED_INT:
			case UNUSED_STRING :
				return Integer.compare(getAsInt(rowi, colIndex), getAsInt(rowj, colIndex));
			case NUMERICAL_FLOAT:
			case UNUSED_FLOAT :
				float a = getAsFloat(rowi, colIndex);
				float b = getAsFloat(rowj, colIndex);
				return Float.compare(a, b);
			default :
				throw new IllegalArgumentException(colNames[colIndex]+" has invalid type: "+colType);
		}
	}

	public int compare(int a, int b, VariableType colType) {
		switch ( colType ) {
			case CATEGORICAL_INT:
			case NUMERICAL_INT:
			case CATEGORICAL_STRING: // strings are encoded as ints
			case TARGET_CATEGORICAL_STRING:
			case TARGET_CATEGORICAL_INT:
			case UNUSED_INT:
			case UNUSED_STRING :
				return Integer.compare(a, b);
			case NUMERICAL_FLOAT:
			case UNUSED_FLOAT :
				float af = getAsFloat(a);
				float bf = getAsFloat(b);
				return Float.compare(af, bf);
			default :
				throw new IllegalArgumentException("invalid type: "+colType);
		}
	}

	public int getPredictedCol() {
		int firstCol = indexOf(colTypes, t -> t==TARGET_CATEGORICAL_STRING || t==TARGET_CATEGORICAL_INT);
		return firstCol>=0 ? firstCol : getNumberOfColumns() - 1; // default to last column
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
			if ( colTypes[i]==TARGET_CATEGORICAL_INT || colTypes[i]==TARGET_CATEGORICAL_STRING ) {
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
		colTypes[dim-1] = TARGET_CATEGORICAL_INT;
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
