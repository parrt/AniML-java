package us.parr.animl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import us.parr.animl.classifiers.trees.DecisionTree;
import us.parr.animl.classifiers.trees.RandomForest;
import us.parr.animl.data.DataTable;
import us.parr.animl.validation.Validation;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class TestRFBasics extends BaseTest {
	public static final int MIN_LEAF_SIZE = 1;

	@Test public void testEmpty() {
		RandomForest rf = new RandomForest(1, MIN_LEAF_SIZE);
		rf.train(DataTable.empty(null,null));
		String expecting = "{}";
		String result = toTestString(rf.getTree(0));
		Assert.assertEquals(expecting, result);
	}

	@Test public void testOneRow() {
		List<int[]> rows = new ArrayList<>();
		rows.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		DataTable data = DataTable.fromInts(rows, null, null);
		RandomForest rf = new RandomForest(1, MIN_LEAF_SIZE);
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
		data.setColType(0, DataTable.VariableType.CATEGORICAL_INT);
		data.setColType(1, DataTable.VariableType.CATEGORICAL_INT);
		RandomForest rf = new RandomForest(15, MIN_LEAF_SIZE);
		rf.train(data);
		checkPredictions(data, rf);

		int N = 20;
		int[] missed = RF_leaveOneOutErrors(data, 1, N, MIN_LEAF_SIZE);
//		System.out.println(Arrays.toString(missed));
		int[] expected = new int[] {
			2, 3, 1, 3, 4, 2, 2, 3, 2, 2, 1, 2, 1, 1, 1, 2, 1, 1, 2, 1
		};
		assertArrayEquals(expected, missed);
	}

	@Ignore
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
		data.setColType(0, DataTable.VariableType.CATEGORICAL_INT);
		data.setColType(1, DataTable.VariableType.CATEGORICAL_INT);
		DecisionTree.debug = true;
		RandomForest rf = new RandomForest(12, MIN_LEAF_SIZE);
		rf.train(data);

		int missed = Validation.leaveOneOut(rf, data);
		assertEquals(0, missed, 0.00000001);

		double error = rf.getErrorEstimate(data);
		assertEquals(0.0, error, 0.00000001);
	}
}
