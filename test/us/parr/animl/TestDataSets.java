package us.parr.animl;

import org.junit.Test;
import us.parr.animl.classifiers.DecisionTree;
import us.parr.animl.data.DataTable;

import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TestDataSets extends BaseTest {
	// Figure 18.3 Examples for the restaurant domain. from Russell and Norvig
	// has picture of tree: https://people.eecs.berkeley.edu/~russell/classes/cs194/f11/lectures/CS194%20Fall%202011%20Lecture%2008.pdf
	public static final String[][] restaurant = {
		{"Alt", "Bar", "Fri&Sat", "Hungry", "Patrons", "Price", "Raining", "MadeRez", "Type", "WaitEstimate", "WillWait"},
		{"Yes", "No",  "No",      "Yes",     "Some", "$$$", "No", "Yes", "French", "0–10", "Yes"},
		{"Yes", "No",  "No",      "Yes",     "Full", "$", "No", "No", "Thai", "30–60", "No"},
		{"No",  "Yes", "No",      "No",      "Some", "$", "No", "No", "Burger", "0–10", "Yes"},
		{"Yes", "No",  "Yes",     "Yes",     "Full", "$", "Yes", "No", "Thai", "10–30", "Yes"},
		{"Yes", "No",  "Yes",     "No",      "Full", "$$$", "No", "Yes", "French", ">60", "No"},
		{"No",  "Yes", "No",      "Yes",     "Some", "$$", "Yes", "Yes", "Italian", "0–10", "Yes"},
		{"No",  "Yes", "No",      "No",      "None", "$", "Yes", "No", "Burger", "0–10", "No"},
		{"No",  "No",  "No",      "Yes",     "Some", "$$", "Yes", "Yes", "Thai", "0–10", "Yes"},
		{"No",  "Yes", "Yes",     "No",      "Full", "$", "Yes", "No", "Burger", ">60", "No"},
		{"Yes", "Yes", "Yes",     "Yes",     "Full", "$$$", "No", "Yes", "Italian", "10–30", "No"},
		{"No",  "No",  "No",      "No",      "None", "$", "No", "No", "Thai", "0–10", "No"},
		{"Yes", "Yes", "Yes",     "Yes",     "Full", "$", "No", "No", "Burger", "30–60", "Yes"},
	};

	// data from chap 7: http://shop.oreilly.com/product/9780596529321.do
	public static final String[][] signups =
	{
		{"referrer", "country", "readfaq", "pageviews", "subscription"},
		{"slashdot","USA","yes","18","None"},
		{"google","France","yes","23","Premium"},
		{"digg","USA","yes","24","Basic"},
		{"kiwitobes","France","yes","23","Basic"},
		{"google","UK","no","21","Premium"},
		{"(direct)","New Zealand","no","12","None"},
		{"(direct)","UK","no","21","Basic"},
		{"google","USA","no","24","Premium"},
		{"slashdot","France","yes","19","None"},
		{"digg","USA","no","18","None"},
		{"google","UK","no","18","None"},
		{"kiwitobes","UK","no","19","None"},
		{"digg","New Zealand","yes","12","Basic"},
		{"slashdot","UK","no","21","None"},
		{"google","UK","yes","18","Basic"},
		{"kiwitobes","France","yes","19","Basic"}
	};

	@Test
	public void testRestaurant() {
		DataTable data = DataTable.fromStrings(Arrays.asList(restaurant));
		DecisionTree tree = DecisionTree.build(data, 0);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'Patrons','val':'Full','n':12,'E':'1.00','left':{'predict':'Yes','n':4},'right':{'var':'Hungry','val':'No','n':8,'E':'0.81','left':{'var':'Fri&Sat','val':'Yes','n':4,'E':'1.00','left':{'predict':'No','n':1},'right':{'var':'Price','val':'$','n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}},'right':{'predict':'No','n':4}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	@Test public void testWebsiteSignups() {
		DataTable data = DataTable.fromStrings(Arrays.asList(signups));
		DecisionTree tree = DecisionTree.build(data,0);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'pageviews','val':21,'n':16,'E':'1.51','left':{'var':'readfaq','val':'no','n':9,'E':'0.92','left':{'var':'referrer','val':'google','n':5,'E':'0.97','left':{'predict':'None','n':2},'right':{'predict':'Basic','n':3}},'right':{'predict':'None','n':4}},'right':{'var':'referrer','val':'digg','n':7,'E':'1.45','left':{'var':'referrer','val':'google','n':4,'E':'0.81','left':{'predict':'None','n':1},'right':{'predict':'Premium','n':3}},'right':{'predict':'Basic','n':3}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	@Test public void testHeartDataSenseTypes() {
		URL url = this.getClass().getClassLoader().getResource("Heart.csv");
		DataTable data = DataTable.loadCSV(url.getFile().toString(), "excel", null, null, true);
		DecisionTree tree = DecisionTree.build(data);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'pageviews','val':21,'n':16,'E':'1.51','left':{'var':'readfaq','val':1,'n':9,'E':'0.92','left':{'predict':'none','n':4},'right':{'var':'referrer','val':2,'n':5,'E':'0.97','left':{'predict':'none','n':2},'right':{'predict':'basic','n':3}}},'right':{'var':'referrer','val':3,'n':7,'E':'1.45','left':{'var':'referrer','val':2,'n':4,'E':'0.81','left':{'predict':'none','n':1},'right':{'predict':'premium','n':3}},'right':{'predict':'basic','n':3}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
//		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}
}
