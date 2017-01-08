package us.parr.animl;

import org.junit.Test;
import us.parr.animl.data.DataTable;

import static org.junit.Assert.assertEquals;

public class TestTable {
	public static final String[] colNames3 = {"a", "b", "c"};
	public static final DataTable.VariableType[] colTypes3 = {
		DataTable.VariableType.CATEGORICAL_INT,
		DataTable.VariableType.NUMERICAL_FLOAT,
		DataTable.VariableType.PREDICTED_CATEGORICAL_INT
	};

	@Test public void testEmpty() {
		DataTable t = DataTable.empty(null, null);
		assertEquals("", t.toTestString());
	}

	@Test public void testEmptyWithNames() {
		DataTable t = DataTable.empty(null, colNames3);
		assertEquals("a, b, c\n", t.toTestString());
	}

	@Test public void testEmptyWithTypes() {
		DataTable t = DataTable.empty(colTypes3, null);
		assertEquals("", t.toTestString());
	}

	@Test public void testEmptyWithNamesAndTypes() {
		DataTable t = DataTable.empty(colTypes3, colNames3);
		assertEquals("a, b, c\n", t.toTestString());
	}
}
