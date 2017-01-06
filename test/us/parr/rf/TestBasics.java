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
		String expecting = "{'predict':99}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsSameCat() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		data.add(new int[] {2,99}); // 2nd row with 1 var of value 2 predicting category 99
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'predict':99}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsSameCatMultipleIndepVars() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,2,3,99});
		data.add(new int[] {2,4,6,99});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'predict':99}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsDiffCat() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99});  // 1 row with 1 var of value 1 predicting category 99
		data.add(new int[] {2,100}); // 2nd row with 1 var of value 2 predicting category 50
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':0,'val':2,'left':{'predict':99},'right':{'predict':100}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsDiffCatMultipleIndepVars() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,2,3,99});
		data.add(new int[] {2,4,6,100});
		DecisionTree tree = DecisionTree.build(data);
		String expecting = "{'var':0,'val':2,'left':{'predict':99},'right':{'predict':100}}";
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
		String expecting = "{'var':0,'val':2,'left':{'predict':99},'right':{'predict':100}}";
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
		String expecting = "{'var':1,'val':2,'left':{'predict':99},'right':{'predict':100}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

//
//	@Test public void testEmpty() throws Exception {
//		RandomForest rf = new RandomForest(10);
//		List<int[]> X = new ArrayList<>();
//		List<Integer> Y = new ArrayList<>();
//		int[] variables = new int[5];
//		rf.train(X,variables,Y, 2);
//
//		int[] test = new int[5];
//		int result = rf.classify(test);
//		int expecting = INVALID_CATEGORY;
//		assertEquals(expecting, result);
//	}
}
