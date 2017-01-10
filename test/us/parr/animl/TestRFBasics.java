package us.parr.animl;

import org.junit.Test;
import us.parr.animl.classifiers.RandomForest;
import us.parr.animl.data.DataTable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestRFBasics extends BaseTest {
	@Test public void testEmpty() {
		RandomForest rf = RandomForest.train(DataTable.empty(null,null), 1);
		assertEquals(null, rf);
	}

	@Test public void testOneRow() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		DataTable table = DataTable.fromInts(data, null, null);
		RandomForest rf = RandomForest.train(table, 1);
		String expecting = "{'predict':99,'n':1}";
		String result = toTestString(rf.getTree(0));
		assertEquals(expecting, result);
		checkPredictions(table, rf);
	}

	@Test public void testNoiseAndGoodPredictor() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[]{1, 9, 1}); // x0 is crappy but x1 is perfect predictor of y
		data.add(new int[]{1, 9, 1});
		data.add(new int[]{2, 9, 1});
		data.add(new int[]{1, 9, 1});
		data.add(new int[]{2, 7, 2});
		data.add(new int[]{1, 7, 2});
		data.add(new int[]{2, 7, 2});
		data.add(new int[]{0, 7, 2});
		DataTable table = DataTable.fromInts(data, null, null);
		RandomForest rf = RandomForest.train(table, 10);
		checkPredictions(table, rf);
	}

	@Test public void testOutOfBagSets() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[]{1, 9, 1}); // x0 is crappy but x1 is perfect predictor of y
		data.add(new int[]{1, 9, 1});
		data.add(new int[]{2, 9, 1});
		data.add(new int[]{1, 9, 1});
		data.add(new int[]{2, 7, 2});
		data.add(new int[]{1, 7, 2});
		data.add(new int[]{2, 7, 2});
		data.add(new int[]{0, 7, 2});
		DataTable table = DataTable.fromInts(data, null, null);
		RandomForest rf = RandomForest.train(table, 10);
		checkPredictions(table, rf);
	}
}
