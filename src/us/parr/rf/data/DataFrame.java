package us.parr.rf.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class DataFrame implements Iterable<int[]> {
	enum VariableType {CATEGORICAL_INT, CATEGORICAL_STRING, NUMERICAL_INT, NUMERICAL_FLOAT, UNUSED_INT}

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
		table.colNames = colNames;
		return table;
	}

	public static DataFrame fromStrings(List<String[]> rows, VariableType[] colTypes, String[] colNames) {
		DataFrame table = new DataFrame();
		table.rows = new ArrayList<>();
		table.colTypes = colTypes;
		table.colNames = colNames;
		table.colStringToIntMap = new StringTable[colTypes.length];
		// don't waste space on string tables unless we need to
		for (int j = 0; j < colTypes.length; j++) {
			if ( colTypes[j]==VariableType.CATEGORICAL_STRING ) {
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
