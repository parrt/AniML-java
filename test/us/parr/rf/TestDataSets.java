package us.parr.rf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestDataSets extends BaseTest {
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
	public static final int slashdot = 1;
	public static final int google = 2;
	public static final int digg = 3;
	public static final int kiwitobes = 4;
	public static final int direct = 5;
	public static final int USA = 1;
	public static final int France = 2;
	public static final int UK = 3;
	public static final int NewZealand = 4;
	public static final int NoSignUp = 1;
	public static final int Basic = 2;
	public static final int Premium = 3;
	public static final String[] signups_varnames =
		{"referrer", "country", "readfaq", "pageviews", "subscription"};
	public static final String[] signups_catnames =
		{"", "none","basic","premium"};
	public static int[][] signups = {
		{slashdot,  USA,        1, 18,NoSignUp},
		{google,    France,     1, 23,Premium},
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

	@Test
	public void testRestaurant() {
		List<Integer> pred = new ArrayList<>();
		List<int[]> data = new ArrayList<>();
		for (int[] row : restaurant) {
			data.add(row);
			pred.add(row[row.length-1]);
		}
		System.out.println(pred);
		DecisionTree tree = DecisionTree.build(data, restaurant_varnames);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'Hungry','val':1,'n':12,'E':'1.00','left':{'var':'Bar','val':1,'n':5,'E':'0.72','left':{'predict':'no','n':2},'right':{'var':'Raining','val':1,'n':3,'E':'0.92','left':{'predict':'yes','n':1},'right':{'predict':'no','n':2}}},'right':{'var':'Patrons','val':2,'n':7,'E':'0.86','left':{'predict':'yes','n':3},'right':{'var':'Fri&Sat','val':1,'n':4,'E':'1.00','left':{'predict':'no','n':1},'right':{'var':'Price','val':3,'n':3,'E':'0.92','left':{'predict':'yes','n':2},'right':{'predict':'no','n':1}}}}}";
		String result = toTestString(tree, restaurant_varnames, restaurant_catnames);
		System.out.println(tree.toDOT(restaurant_varnames, restaurant_catnames));
		assertEquals(expecting, result);
	}

	@Test public void testWebsiteSignups() {
		List<int[]> data = new ArrayList<>();
		for (int[] row : signups) {
			data.add(row);
		}
		DecisionTree tree = DecisionTree.build(data, signups_varnames);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'pageviews','val':21,'n':16,'E':'1.51','left':{'var':'readfaq','val':1,'n':9,'E':'0.92','left':{'predict':'none','n':4},'right':{'var':'referrer','val':2,'n':5,'E':'0.97','left':{'predict':'none','n':2},'right':{'predict':'basic','n':3}}},'right':{'var':'referrer','val':3,'n':7,'E':'1.45','left':{'var':'referrer','val':2,'n':4,'E':'0.81','left':{'predict':'none','n':1},'right':{'predict':'premium','n':3}},'right':{'predict':'basic','n':3}}}";
		String result = toTestString(tree, signups_varnames, signups_catnames);
//		System.out.println(tree.toDOT(signups_varnames, signups_catnames));
		assertEquals(expecting, result);
	}
}
