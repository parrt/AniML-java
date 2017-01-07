package us.parr.rf.data;

import us.parr.rf.misc.FrequencySet;
import us.parr.rf.misc.RFUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import static us.parr.rf.data.DataFrame.VariableType.CATEGORICAL_INT;
import static us.parr.rf.data.DataFrame.VariableType.CATEGORICAL_STRING;
import static us.parr.rf.data.DataFrame.VariableType.NUMERICAL_INT;
import static us.parr.rf.data.DataFrame.VariableType.PREDICTED_CATEGORICAL_INT;

public class DataFrame implements Iterable<int[]> {
	enum VariableType {
		CATEGORICAL_INT, CATEGORICAL_STRING, NUMERICAL_INT, NUMERICAL_FLOAT,
		PREDICTED_CATEGORICAL_INT,
		UNUSED_INT
	}

	protected List<int[]> rows;
	protected String[] colNames;
	protected VariableType[] colTypes;
	protected StringTable[] colStringToIntMap;

	public DataFrame() {
	}

	public static DataFrame fromInts(List<int[]> rows, VariableType[] colTypes, String[] colNames) {
		DataFrame table = new DataFrame();
		table.rows = rows;
		table.colTypes = colTypes;
		if ( colNames==null ) {
			colNames = new String[colTypes.length];
			for (int i = 0; i < colNames.length; i++) {
				colNames[i] = "x"+i;
			}
		}
		table.colNames = colNames;
		return table;
	}

	public static DataFrame fromStrings(List<String[]> rows, VariableType[] colTypes, String[] colNames) {
		DataFrame table = new DataFrame();
		table.rows = new ArrayList<>();
		table.colTypes = colTypes;
		if ( colNames==null ) {
			colNames = new String[colTypes.length];
			for (int i = 0; i < colNames.length; i++) {
				if ( colTypes[i]==PREDICTED_CATEGORICAL_INT ) {
					colNames[i] = "y";
				}
				else {
					colNames[i] = "x" + i;
				}
			}
		}
		table.colNames = colNames;
		table.colStringToIntMap = new StringTable[colTypes.length];
		// don't waste space on string tables unless we need to
		for (int j = 0; j < colTypes.length; j++) {
			if ( colTypes[j]== CATEGORICAL_STRING ) {
				table.colStringToIntMap[j] = new StringTable();
			}
		}
		for (int i = 0; i < rows.size(); i++) {
			String[] row = rows.get(i);
			int[] rowI = new int[row.length];
			for (int j = 0; j < row.length; j++) {
				int col=0;
				switch ( colTypes[j] ) {
					case CATEGORICAL_INT :
					case NUMERICAL_INT :
					case UNUSED_INT :
						col = Integer.valueOf(row[j]);
						break;
					case CATEGORICAL_STRING :
						col = table.colStringToIntMap[j].add(row[j]);
						break;
					case NUMERICAL_FLOAT :
						col = Float.floatToIntBits(Float.valueOf(row[j]));
						break;
				}
				rowI[i] = col;
			}
		}
		table.colNames = colNames;
		return table;
	}

	public Set<Integer> uniqueValues(int colIndex) {
		FrequencySet<Integer> valueCounts = valueCountsInColumn(colIndex);
		return valueCounts.keySet();
	}

	public double entropy(int colIndex) {
		FrequencySet<Integer> valueCounts = valueCountsInColumn(colIndex);
		return RFUtils.entropy(valueCounts.counts());
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
		return new DataFrameIterator(this);
	}

	@Override
	public void forEach(Consumer<? super int[]> action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Spliterator<int[]> spliterator() {
		throw new UnsupportedOperationException();
	}
}
