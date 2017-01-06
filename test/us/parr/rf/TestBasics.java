package us.parr.rf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestBasics extends BaseTest {
	@Test public void testEmptyData() {
		List<int[]> data = new ArrayList<>();
		DecisionTree tree = DecisionTree.build(data);
		assertEquals(null, tree);
	}

	@Test public void testOneRow() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'predict':99,'n':1}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsSameCat() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		data.add(new int[] {2,99}); // 2nd row with 1 var of value 2 predicting category 99
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'predict':99,'n':2}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testCannotPredict() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99});
		data.add(new int[] {1,100});
		data.add(new int[] {2,99});
		data.add(new int[] {2,100});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'predict':99,'n':4,'E':'1.00'}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsSameCatMultipleIndepVars() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,2,3,99});
		data.add(new int[] {2,4,6,99});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'predict':99,'n':2}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsDiffCat() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99});  // 1 row with 1 var of value 1 predicting category 99
		data.add(new int[] {2,100}); // 2nd row with 1 var of value 2 predicting category 50
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x0','val':2,'n':2,'E':'1.00','left':{'predict':99,'n':1},'right':{'predict':100,'n':1}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsDiffCatMultipleIndepVars() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,2,3,99});
		data.add(new int[] {2,4,6,100});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x0','val':2,'n':2,'E':'1.00','left':{'predict':99,'n':1},'right':{'predict':100,'n':1}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoVarsOneGoodOneBadSplitVar() {
		List<int[]> data = new ArrayList<>(); // 1st var is perfect splitter, 2nd is bad
		data.add(new int[] {1,4,99});
		data.add(new int[] {1,5,99});
		data.add(new int[] {2,4,100});
		data.add(new int[] {2,5,100});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x0','val':2,'n':4,'E':'1.00','left':{'predict':99,'n':2},'right':{'predict':100,'n':2}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoVarsOneGoodOneBadSplitVarFlippedOrder() {
		List<int[]> data = new ArrayList<>(); // 2nd var is perfect splitter, 1st is bad
		data.add(new int[] {4,1,99});
		data.add(new int[] {5,1,99});
		data.add(new int[] {4,2,100});
		data.add(new int[] {5,2,100});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x1','val':2,'n':4,'E':'1.00','left':{'predict':99,'n':2},'right':{'predict':100,'n':2}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testNoisePredictions() {
//		int[] randomCats = new int[] {1, 1, 2, 1, 2, 1, 2, 0}; // randint(8, 3, 999);
//		System.out.println(Arrays.toString(randomCats));
		List<int[]> data = new ArrayList<>();
		// nice split in sole independent var but predictions are random.
		data.add(new int[] {1,1});
		data.add(new int[] {1,1});
		data.add(new int[] {1,2});
		data.add(new int[] {1,1});
		data.add(new int[] {2,2});
		data.add(new int[] {2,1});
		data.add(new int[] {2,2});
		data.add(new int[] {2,0});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x0','val':2,'n':8,'E':'1.41','left':{'predict':1,'n':4,'E':'0.81'},'right':{'predict':2,'n':4,'E':'1.50'}}";
		System.out.println(tree.toDOT(null,null));
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testNoisePredictor() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,1});
		data.add(new int[] {1,1});
		data.add(new int[] {2,1});
		data.add(new int[] {1,1});
		data.add(new int[] {2,2});
		data.add(new int[] {1,2});
		data.add(new int[] {2,2});
		data.add(new int[] {0,2});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x0','val':1,'n':8,'E':'1.00','left':{'predict':2,'n':1},'right':{'var':'x0','val':2,'n':7,'E':'0.99','left':{'predict':1,'n':4,'E':'0.81'},'right':{'predict':2,'n':3,'E':'0.92'}}}";
		String result = toTestString(tree);
//		System.out.println(tree.toDOT(null,null));
		assertEquals(expecting, result);
	}

	@Test public void testNoiseAndGoodPredictor() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,9,1}); // x0 is crappy but x1 is perfect predictor of y
		data.add(new int[] {1,9,1});
		data.add(new int[] {2,9,1});
		data.add(new int[] {1,9,1});
		data.add(new int[] {2,7,2});
		data.add(new int[] {1,7,2});
		data.add(new int[] {2,7,2});
		data.add(new int[] {0,7,2});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x1','val':9,'n':8,'E':'1.00','left':{'predict':2,'n':4},'right':{'predict':1,'n':4}}";
		String result = toTestString(tree);
//		System.out.println(tree.toDOT(null,null));
		assertEquals(expecting, result);
	}

	@Test public void testFixedAndGoodPredictorWith4PredictorValues() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,9,1}); // x0 is crappy but x1 is perfect predictor of y
		data.add(new int[] {1,9,1});
		data.add(new int[] {1,12,1});
		data.add(new int[] {1,12,1});
		data.add(new int[] {1,7,2});
		data.add(new int[] {1,7,2});
		data.add(new int[] {1,11,2});
		data.add(new int[] {1,11,2});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x1','val':9,'n':8,'E':'1.00','left':{'predict':2,'n':2},'right':{'var':'x1','val':11,'n':6,'E':'0.92','left':{'predict':1,'n':2},'right':{'var':'x1','val':12,'n':4,'E':'1.00','left':{'predict':2,'n':2},'right':{'predict':1,'n':2}}}}";
		String result = toTestString(tree);
//		System.out.println(tree.toDOT(null,null));
		assertEquals(expecting, result);
	}

	@Test public void testTimestampAndGoodPredictorIgnoresTimestamp() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,9,2}); // x0 is crappy but x1 is perfect predictor of y
		data.add(new int[] {2,12,1});
		data.add(new int[] {3,7,2});
		data.add(new int[] {4,11,1});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':'x1','val':9,'n':8,'E':'1.00','left':{'predict':2,'n':2},'right':{'var':'x1','val':11,'n':6,'E':'0.92','left':{'predict':1,'n':2},'right':{'var':'x1','val':12,'n':4,'E':'1.00','left':{'predict':2,'n':2},'right':{'predict':1,'n':2}}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT(null,null));
		assertEquals(expecting, result);
	}
}
