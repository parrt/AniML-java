package us.parr.animl;

import org.junit.Assert;
import org.junit.Test;
import us.parr.animl.classifiers.RandomForest;
import us.parr.animl.data.DataTable;
import us.parr.animl.validation.LeaveOneOutValidator;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class TestRFBasics extends BaseTest {
	public static final int MIN_NODE_SIZE = 1;

	@Test public void testEmpty() {
		RandomForest rf = RandomForest.train(DataTable.empty(null,null), 1, MIN_NODE_SIZE);
		Assert.assertEquals(null, rf);
	}

	@Test public void testOneRow() {
		List<int[]> rows = new ArrayList<>();
		rows.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		DataTable data = DataTable.fromInts(rows, null, null);
		RandomForest rf = RandomForest.train(data, 1, MIN_NODE_SIZE);
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
		RandomForest rf = RandomForest.train(data, 10, MIN_NODE_SIZE);
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
		RandomForest rf = RandomForest.train(data, 12, MIN_NODE_SIZE);

		LeaveOneOutValidator validator = new LeaveOneOutValidator(data, rf);
		int missed = validator.validate();
		assertEquals(0, missed, 0.00000001);

		double error = rf.getErrorEstimate(data);
		assertEquals(0.0, error, 0.00000001);
	}
}
