package us.parr.rf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestBasics {
	// Figure 18.3 Examples for the restaurant domain. from Russell and Norvig
	public static final int French = 0;
	public static final int Thai = 1;
	public static final int Burger = 2;
	public static final int Italian = 3;
	public static final int $ = 1;
	public static final int $$ = 2;
	public static final int $$$ = 3;
	public static final int None = 0;
	public static final int Some = 1;
	public static final int Full = 2;
	public static final int delay_0_10 = 0;
	public static final int delay_10_30 = 1;
	public static final int delay_30_60 = 2;
	public static final int delay_gt_60 = 3;
	public static final String[] restaurant_varnames =
		{"Alternate", "Bar", "Fri&Sat", "Hungry", "Patrons", "Price", "Raining", "MadeRez", "Type", "WaitEstimate", "WillWait"};
	public static final String[] restaurant_catnames = {"no", "yes"};
	public static int[][] restaurant = {
		{1, 0, 0, 1, Some, $$$, 0, 1, French,   delay_0_10,  1},
		{1, 0, 0, 1, Full, $,   0, 0, Thai,     delay_30_60, 0},
		{0, 1, 0, 0, Some, $,   0, 0, Burger,   delay_0_10,  1},
		{1, 0, 1, 1, Full, $,   1, 0, Thai,     delay_10_30, 1},
		{1, 0, 1, 0, Full, $$$, 0, 1, French,   delay_gt_60, 0},
		{0, 1, 0, 1, Some, $$,  1, 1, Italian,  delay_0_10,  1},
		{0, 1, 0, 0, None, $,   1, 0, Burger,   delay_0_10,  0},
		{0, 0, 0, 1, Some, $$,  1, 1, Thai,     delay_0_10,  1},
		{0, 1, 1, 0, Full, $,   1, 0, Burger,   delay_gt_60, 0},
		{1, 1, 1, 1, Full, $$$, 0, 1, Italian,  delay_10_30, 0},
		{0, 0, 0, 0, None, $,   0, 0, Thai,     delay_0_10,  0},
		{1, 1, 1, 1, Full, $,   0, 0, Burger,   delay_30_60, 1}
	};

	// data from chap 7: http://shop.oreilly.com/product/9780596529321.do
	public static final int slashdot = 0;
	public static final int google = 0;
	public static final int digg = 0;
	public static final int kiwitobes = 0;
	public static final int direct = 0;
	public static final int USA = 0;
	public static final int France = 0;
	public static final int UK = 0;
	public static final int NewZealand = 0;
	public static final int NoSignUp = 0;
	public static final int Basic = 0;
	public static final int Premium = 0;
	public static int[][] signups = {
		{slashdot,  USA,        1, 18,NoSignUp},
		{google,    France,     1, 23,NoSignUp},
		{digg,      USA,        1, 24,Basic},
		{kiwitobes, France,     1, 23,Basic},
		{google,    UK,         0, 21,Premium},
		{(direct),  NewZealand, 0, 12,NoSignUp},
		{(direct),  UK,         0, 21,Basic},
		{google,    USA,        0, 24,Premium},
		{slashdot,  France,     1, 19,NoSignUp},
		{digg,      USA,        0, 18,NoSignUp},
		{google,    UK,         0, 18,NoSignUp},
		{kiwitobes, UK,         0, 19,NoSignUp},
		{digg,      NewZealand, 1, 12,Basic},
		{slashdot,  UK,         0, 21,NoSignUp},
		{google,    UK,         1, 18,Basic},
		{kiwitobes, France,     1, 19,Basic}
	};

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

	@Test public void testRestaurant() {
		List<int[]> data = new ArrayList<>();
		for (int[] row : restaurant) {
			data.add(row);
		}
		DecisionTree tree = DecisionTree.build(data, restaurant_varnames);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'Hungry','val':1,'left':{'var':'Bar','val':1,'left':{'predict':'no'},'right':{'var':'Raining','val':1,'left':{'predict':'yes'},'right':{'predict':'no'}}},'right':{'var':'Patrons','val':2,'left':{'predict':'yes'},'right':{'var':'Fri&Sat','val':1,'left':{'predict':'no'},'right':{'var':'Price','val':3,'left':{'predict':'yes'},'right':{'predict':'no'}}}}}";
		String result = toTestString(tree, restaurant_varnames, restaurant_catnames);
		assertEquals(expecting, result);
	}

	public static String toTestString(DecisionTree tree) {
		return tree.toJSON().toString().replaceAll("\"", "'");
	}

	public static String toTestString(DecisionTree tree, String[] varnames, String[] catnames) {
		return tree.toJSON(varnames,catnames).toString().replaceAll("\"", "'");
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
