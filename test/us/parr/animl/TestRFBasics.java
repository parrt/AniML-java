package us.parr.animl;

import org.junit.Assert;
import org.junit.Test;
import us.parr.animl.classifiers.trees.RandomForest;
import us.parr.animl.data.DataTable;
import us.parr.animl.validation.Validation;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class TestRFBasics extends BaseTest {
	public static final int MIN_NODE_SIZE = 1;

	@Test public void testEmpty() {
		RandomForest rf = new RandomForest(1, MIN_NODE_SIZE);
		rf.train(DataTable.empty(null,null));
		Assert.assertEquals(null, rf);
	}

	@Test public void testOneRow() {
		List<int[]> rows = new ArrayList<>();
		rows.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		DataTable data = DataTable.fromInts(rows, null, null);
		RandomForest rf = new RandomForest(1, MIN_NODE_SIZE);
		rf.train(data);
		String expecting = "{'predict':99,'n':1}";
		String result = toTestString(rf.getTree(0));
		Assert.assertEquals(expecting, result);
		checkPredictions(data, rf);
	}

	@Test public void testNoiseAndGoodPredictor() {
		List<int[]> rows = new ArrayList<>();
		rows.add(new int[]{1, 9, 1}); // x0 is crappy but x1 is perfect predictor of y
		rows.add(new int[]{1, 9, 1});
		rows.add(new int[]{2, 9, 1});
		rows.add(new int[]{1, 9, 1});
		rows.add(new int[]{2, 7, 2});
		rows.add(new int[]{1, 7, 2});
		rows.add(new int[]{2, 7, 2});
		rows.add(new int[]{0, 7, 2});
		DataTable data = DataTable.fromInts(rows, null, null);
		RandomForest rf = new RandomForest(10, MIN_NODE_SIZE);
		rf.train(data);
		checkPredictions(data, rf);
	}

	@Test public void testOutOfBagSets() {
		List<int[]> rows = new ArrayList<>();
		rows.add(new int[]{1, 9, 1}); // x0 is crappy but x1 is perfect predictor of y
		rows.add(new int[]{1, 9, 1});
		rows.add(new int[]{2, 9, 1});
		rows.add(new int[]{1, 9, 1});
		rows.add(new int[]{2, 7, 2});
		rows.add(new int[]{1, 7, 2});
		rows.add(new int[]{2, 7, 2});
		rows.add(new int[]{0, 7, 2});
		rows.add(new int[]{1, 9, 1}); // dup
		rows.add(new int[]{1, 9, 1});
		rows.add(new int[]{2, 9, 1});
		rows.add(new int[]{1, 9, 1});
		rows.add(new int[]{2, 7, 2});
		rows.add(new int[]{1, 7, 2});
		rows.add(new int[]{2, 7, 2});
		rows.add(new int[]{0, 7, 2});
		DataTable data = DataTable.fromInts(rows, null, null);
//		DecisionTree.debug = true;
		RandomForest rf = new RandomForest(12, MIN_NODE_SIZE);
		rf.train(data);

		int missed = Validation.leaveOneOut(data, rf);
		assertEquals(0, missed, 0.00000001);

		double error = rf.getErrorEstimate(data);
		assertEquals(0.0, error, 0.00000001);
	}
}
