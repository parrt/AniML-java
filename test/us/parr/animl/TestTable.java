package us.parr.animl;

import org.junit.Test;
import us.parr.animl.data.DataTable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.PREDICTED_CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.PREDICTED_CATEGORICAL_STRING;

public class TestTable {
	public static final String[] colNames3 = {"a", "b", "y"};
	public static final DataTable.VariableType[] colTypes3 = {
		CATEGORICAL_INT,
		NUMERICAL_INT,
		PREDICTED_CATEGORICAL_INT
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
		assertEquals("a(cat), b(int), y(predicted)\n", t.toTestString());
	}

	@Test public void test1x3Row() {
		DataTable t = DataTable.fromInts(rawData1x3, null, null);
		String expected =
			"x0(int), x1(int), y(predicted)\n"+
			"1, 2, 3\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test1x3RowWithTypes() {
		DataTable t = DataTable.fromInts(rawData1x3, colTypes3, null);
		String expected =
			"x0(cat), x1(int), y(predicted)\n"+
			"1, 2, 3\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test1x3RowWithNames() {
		DataTable t = DataTable.fromInts(rawData1x3, null, colNames3);
		String expected =
			"a(int), b(int), y(predicted)\n"+
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

	@Test public void testFloatAsInt() {
		final DataTable.VariableType[] colTypes = {
			DataTable.VariableType.NUMERICAL_FLOAT,
			DataTable.VariableType.NUMERICAL_FLOAT,
		};
		final List<int[]> rawData = new ArrayList<int[]>() {{
			add(new int[] {Float.floatToIntBits(0.0f),Float.floatToIntBits(1234.560f)});
		}};
		DataTable t = DataTable.fromInts(rawData, colTypes, null);
		String expected =
			"x0(float), x1(float)\n"+
			"0.0, 1234.56\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void testEmptyRowFromString() {
		List<String[]> data = new ArrayList<>();
		DataTable t = DataTable.fromStrings(data, null, null, false);
		assertEquals("", t.toTestString());
	}

	@Test public void test1RowFromString() {
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"1", "9", "2"});
		DataTable t = DataTable.fromStrings(data, null, null, false);
		String expected =
			"x0(int), x1(int), y(predicted)\n"+
			"1, 9, 2\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test1RowFromStringWithHeader() {
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"A", "B", "Y"});
		data.add(new String[]{"1", "9", "2"});
		DataTable t = DataTable.fromStrings(data, null, null, true);
		String expected =
			"A(int), B(int), Y(predicted)\n"+
			"1, 9, 2\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void testStringCategories() {
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"A", "B", "Y"});
		data.add(new String[]{"yes", "CA", "go"});
		data.add(new String[]{"no", "CO", "stay"});
		data.add(new String[]{"yes", "CO", "stay"});
		data.add(new String[]{"no", "NV", "go"});
		final DataTable.VariableType[] colTypes = {
			CATEGORICAL_STRING,
			CATEGORICAL_STRING,
			PREDICTED_CATEGORICAL_STRING
		};
		DataTable t = DataTable.fromStrings(data, colTypes, null, true);
		String expected =
			"A(string), B(string), Y(predicted-string)\n"+
			"yes, CA, go\n"+
			"no, CO, stay\n"+
			"yes, CO, stay\n"+
			"no, NV, go\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test1RowFromStringWithHeaderAndNamesOverride() {
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"A", "B", "Y"});
		data.add(new String[]{"1", "9", "2"});
		DataTable t = DataTable.fromStrings(data, null, colNames3, true);
		String expected =
			"a(int), b(int), y(predicted)\n"+
			"1, 9, 2\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void test2RowsFromStringWithHeaderTypes() {
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"A", "B", "Y"});
		data.add(new String[]{"1000", "123.45", "2"});
		data.add(new String[]{"99", "0.123456", "2"});
		final DataTable.VariableType[] colTypes = {
			CATEGORICAL_INT,
			NUMERICAL_FLOAT,
			PREDICTED_CATEGORICAL_INT
		};
		DataTable t = DataTable.fromStrings(data, colTypes, null, true);
		String expected =
			"A(cat), B(float), Y(predicted)\n"+
			"1000, 123.45, 2\n"+
			"99, 0.123456, 2\n";
		assertEquals(expected, t.toTestString());
	}

	@Test public void testPrint1Row() {
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"First", "Second", "Dependent Variable"});
		data.add(new String[]{"1", "2", "3"});
		DataTable t = DataTable.fromStrings(data, null, null, true);
		String expected =
			"First Second Dependent Variable\n"+
			"    1      2                  3\n";
		assertEquals(expected, t.toString());
	}

	@Test public void testPrint2Rows() {
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"First", "Second", "Dependent Variable"});
		data.add(new String[]{"100", "123.45", "go"});
		data.add(new String[]{"10",  "23.45", "stay"});
		final DataTable.VariableType[] colTypes = {
			CATEGORICAL_INT,
			NUMERICAL_FLOAT,
			PREDICTED_CATEGORICAL_STRING
		};
		DataTable t = DataTable.fromStrings(data, colTypes, null, true);
		String expected =
			"First Second Dependent Variable\n"+
			"  100 123.45         go        \n"+
			"   10  23.45        stay       \n";
		assertEquals(expected, t.toString());
	}

}
