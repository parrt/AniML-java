package us.parr.rf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static us.parr.rf.RandomForest.INVALID_CATEGORY;

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
	public static int[][] restaurant = {
		// Alternate Bar Fri&Sat Hungry Patrons Price Raining MadeRez Type WaitEstimate WillWait
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

	@Test public void testEmpty() throws Exception {
		RandomForest rf = new RandomForest(10);
		List<int[]> X = new ArrayList<>();
		List<Integer> Y = new ArrayList<>();
		int[] variables = new int[5];
		rf.train(X,variables,Y, 2);

		int[] test = new int[5];
		int result = rf.classify(test);
		int expecting = INVALID_CATEGORY;
		assertEquals(expecting, result);
	}
}
