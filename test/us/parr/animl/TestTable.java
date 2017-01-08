package us.parr.animl;

import org.junit.Test;
import us.parr.animl.data.DataTable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestTable {
	public static final String[] colNames3 = {"a", "b", "y"};
	public static final DataTable.VariableType[] colTypes3 = {
		DataTable.VariableType.CATEGORICAL_INT,
		DataTable.VariableType.NUMERICAL_INT,
		DataTable.VariableType.PREDICTED_CATEGORICAL_INT
	};
	public static final List<int[]> rawData1x3 = new ArrayList<int[]>() {{
		add(new int[] {1,2,3});
	}};

	@Test public void testEmpty() {
		DataTable t = DataTable.empty(null, null);
		assertEquals("", t.toTestString());
	}

	@Test public void testEmptyWithNames() {
		DataTable t = DataTable.empty(null, colNames3);
		assertEquals("a, b, y\n", t.toTestString());
	}

	@Test public void testEmptyWithTypes() {
		DataTable t = DataTable.empty(colTypes3, null);
		assertEquals("", t.toTestString());
	}

	@Test public void testEmptyWithNamesAndTypes() {
		DataTable t = DataTable.empty(colTypes3, colNames3);
		assertEquals("a, b, y\n", t.toTestString());
	}

	@Test public void test1x3Row() {
		DataTable t = DataTable.fromInts(rawData1x3, null, null);
		String expected =
			"x0, x1, y\n"+
			"1, 2, 3\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test1x3RowWithTypes() {
		DataTable t = DataTable.fromInts(rawData1x3, colTypes3, null);
		String expected =
			"x0, x1, y\n"+
			"1, 2, 3\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test1x3RowWithNames() {
		DataTable t = DataTable.fromInts(rawData1x3, null, colNames3);
		String expected =
			"a, b, y\n"+
			"1, 2, 3\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test1x3RowWithNamesAndTypes() {
		DataTable t = DataTable.fromInts(rawData1x3, colTypes3, colNames3);
		String expected =
			"a(cat), b(int), y(predicted)\n"+
			"1, 2, 3\n";
		assertEquals(expected, t.toTestString());
	}

}
