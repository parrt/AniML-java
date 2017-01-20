package us.parr.animl;

import org.junit.Test;
import us.parr.animl.data.DataTable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static us.parr.animl.AniUtils.join;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;

public class TestTable extends BaseTest {
	public static final String[] colNames3 = {"a", "b", "y"};
	public static final DataTable.VariableType[] colTypes3 = {
		CATEGORICAL_INT,
		NUMERICAL_INT,
		TARGET_CATEGORICAL_INT
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
			TARGET_CATEGORICAL_STRING
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
			TARGET_CATEGORICAL_INT
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

	@Test public void testPrintMultiRows() {
		URL url = this.getClass().getClassLoader().getResource("quoted-values.csv");
		final DataTable.VariableType[] colTypes = {
			CATEGORICAL_INT,
			NUMERICAL_FLOAT,
			TARGET_CATEGORICAL_STRING
		};
		DataTable t = DataTable.loadCSV(url.getFile().toString(), "excel", colTypes, null, true);
		String expected =
			"  Age  Sex\n"+
			"1 63.0  1 \n"+
			"2 67.0  1 \n"+
			"3 67.0  1 \n"+
			"4 37.0  1 \n"+
			"5 41.0  0 \n";
		assertEquals(expected, t.toString());
	}

	@Test public void testHeartDataSenseTypes() {
		URL url = this.getClass().getClassLoader().getResource("Heart.csv");
		DataTable t = DataTable.loadCSV(url.getFile().toString(), "excel", null, null, true);
		t.setColType(0, UNUSED_INT); // first column is ID
		final DataTable.VariableType[] expectedColTypes = {
			UNUSED_INT,
			NUMERICAL_INT,
			NUMERICAL_INT,
			CATEGORICAL_STRING,
			NUMERICAL_INT,
			NUMERICAL_INT,
			NUMERICAL_INT,
			NUMERICAL_INT,
			NUMERICAL_INT,
			NUMERICAL_INT,
			NUMERICAL_FLOAT,
			NUMERICAL_INT,
			NUMERICAL_INT,
			CATEGORICAL_STRING,
			TARGET_CATEGORICAL_STRING
		};
		assertArrayEquals(expectedColTypes, t.getColTypes());

		String expected = // first 20 records
			"    Age Sex  ChestPain   RestBP Chol Fbs RestECG MaxHR ExAng Oldpeak Slope Ca    Thal    AHD\n"+
			"  1  63   1   typical       145  233   1       2   150     0     2.3     3  0   fixed    No \n"+
			"  2  67   1 asymptomatic    160  286   0       2   108     1     1.5     2  3   normal   Yes\n"+
			"  3  67   1 asymptomatic    120  229   0       2   129     1     2.6     2  2 reversable Yes\n"+
			"  4  37   1  nonanginal     130  250   0       0   187     0     3.5     3  0   normal   No \n"+
			"  5  41   0  nontypical     130  204   0       2   172     0     1.4     1  0   normal   No \n"+
			"  6  56   1  nontypical     120  236   0       0   178     0     0.8     1  0   normal   No \n"+
			"  7  62   0 asymptomatic    140  268   0       2   160     0     3.6     3  2   normal   Yes\n"+
			"  8  57   0 asymptomatic    120  354   0       0   163     1     0.6     1  0   normal   No \n"+
			"  9  63   1 asymptomatic    130  254   0       2   147     0     1.4     2  1 reversable Yes\n"+
			" 10  53   1 asymptomatic    140  203   1       2   155     1     3.1     3  0 reversable Yes\n"+
			" 11  57   1 asymptomatic    140  192   0       0   148     0     0.4     2  0   fixed    No \n"+
			" 12  56   0  nontypical     140  294   0       2   153     0     1.3     2  0   normal   No \n"+
			" 13  56   1  nonanginal     130  256   1       2   142     1     0.6     2  1   fixed    Yes\n"+
			" 14  44   1  nontypical     120  263   0       0   173     0     0.0     1  0 reversable No \n"+
			" 15  52   1  nonanginal     172  199   1       0   162     0     0.5     1  0 reversable No \n"+
			" 16  57   1  nonanginal     150  168   0       0   174     0     1.6     1  0   normal   No \n"+
			" 17  48   1  nontypical     110  229   0       0   168     0     1.0     3  0 reversable Yes\n"+
			" 18  54   1 asymptomatic    140  239   0       0   160     0     1.2     1  0   normal   No \n"+
			" 19  48   0  nonanginal     130  275   0       0   139     0     0.2     1  0   normal   No \n"+
			" 20  49   1  nontypical     130  266   0       0   171     0     0.6     1  0   normal   No \n";
		String result = join(Arrays.asList(t.toString().split("\n")).subList(0,21), "\n")+"\n";
		assertEquals(expected, result);
	}
}
