package us.parr.animl;

import org.junit.Test;
import us.parr.animl.classifiers.DecisionTree;
import us.parr.animl.data.DataTable;

import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;

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
		DecisionTree tree = DecisionTree.build(data);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'Patrons','val':'Full','n':12,'E':'1.00','left':{'predict':'Yes','n':4},'right':{'var':'Hungry','val':'No','n':8,'E':'0.81','left':{'var':'Fri&Sat','val':'Yes','n':4,'E':'1.00','left':{'predict':'No','n':1},'right':{'var':'Price','val':'$','n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}},'right':{'predict':'No','n':4}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	@Test public void testWebsiteSignups() {
		DataTable data = DataTable.fromStrings(Arrays.asList(signups));
		DecisionTree tree = DecisionTree.build(data);
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
		data.setColType(0, UNUSED_INT); // first column is ID
		DecisionTree tree = DecisionTree.build(data);
		// I verified this string by looking at DOT output
		String expecting = "{'var':'Thal','val':'reversable','n':303,'E':'1.00','left':{'var':'Ca','val':1,'n':186,'E':'0.84','left':{'var':'MaxHR','val':162,'n':127,'E':'0.55','left':{'var':'Oldpeak','val':2.799999952316284,'n':64,'E':'0.79','left':{'var':'Chol','val':237,'n':59,'E':'0.69','left':{'var':'ExAng','val':1,'n':30,'E':'0.35','left':{'predict':'No','n':23},'right':{'var':'Oldpeak','val':1.2000000476837158,'n':7,'E':'0.86','left':{'var':'Age','val':52,'n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}},'right':{'predict':'No','n':4}}},'right':{'var':'Oldpeak','val':1.7999999523162842,'n':29,'E':'0.89','left':{'var':'Age','val':59,'n':27,'E':'0.83','left':{'var':'RestBP','val':110,'n':14,'E':'0.37','left':{'var':'Sex','val':1,'n':3,'E':'0.92','left':{'predict':'No','n':2},'right':{'predict':'Yes','n':1}},'right':{'predict':'No','n':11}},'right':{'var':'Age','val':61,'n':13,'E':'1.00','left':{'predict':'Yes','n':3},'right':{'var':'RestBP','val':155,'n':10,'E':'0.88','left':{'var':'RestECG','val':2,'n':6,'E':'1.00','left':{'var':'Age','val':62,'n':4,'E':'0.81','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':3}},'right':{'predict':'No','n':2}},'right':{'predict':'No','n':4}}}},'right':{'predict':'Yes','n':2}}},'right':{'var':'RestBP','val':120,'n':5,'E':'0.72','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':4}}},'right':{'var':'Age','val':61,'n':63,'E':'0.12','left':{'predict':'No','n':60},'right':{'var':'Age','val':62,'n':3,'E':'0.92','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':2}}}},'right':{'var':'ChestPain','val':'nonanginal','n':59,'E':'0.98','left':{'var':'ChestPain','val':'asymptomatic','n':34,'E':'0.73','left':{'var':'RestBP','val':140,'n':7,'E':'0.99','left':{'var':'Age','val':59,'n':4,'E':'0.81','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':3}},'right':{'predict':'No','n':3}},'right':{'var':'Sex','val':1,'n':27,'E':'0.50','left':{'var':'RestBP','val':138,'n':7,'E':'0.99','left':{'var':'MaxHR','val':169,'n':4,'E':'0.81','left':{'predict':'No','n':3},'right':{'predict':'Yes','n':1}},'right':{'predict':'Yes','n':3}},'right':{'predict':'Yes','n':20}}},'right':{'var':'Thal','val':'normal','n':25,'E':'0.86','left':{'predict':'Yes','n':3},'right':{'var':'RestECG','val':2,'n':22,'E':'0.68','left':{'predict':'No','n':10},'right':{'var':'Chol','val':246,'n':12,'E':'0.92','left':{'predict':'Yes','n':3},'right':{'var':'Chol','val':319,'n':9,'E':'0.50','left':{'predict':'No','n':7},'right':{'var':'Age','val':65,'n':2,'E':'1.00','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':1}}}}}}}},'right':{'var':'Oldpeak','val':0.800000011920929,'n':117,'E':'0.79','left':{'var':'RestBP','val':124,'n':38,'E':'1.00','left':{'var':'Age','val':43,'n':11,'E':'0.68','left':{'predict':'Yes','n':1},'right':{'var':'RestBP','val':101,'n':10,'E':'0.47','left':{'var':'Age','val':58,'n':2,'E':'1.00','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':1}},'right':{'predict':'No','n':8}}},'right':{'var':'Age','val':52,'n':27,'E':'0.95','left':{'predict':'Yes','n':7},'right':{'var':'Chol','val':229,'n':20,'E':'1.00','left':{'var':'Age','val':59,'n':9,'E':'0.76','left':{'predict':'No','n':6},'right':{'var':'Age','val':64,'n':3,'E':'0.92','left':{'predict':'Yes','n':2},'right':{'predict':'No','n':1}}},'right':{'var':'Age','val':59,'n':11,'E':'0.85','left':{'predict':'Yes','n':6},'right':{'var':'MaxHR','val':159,'n':5,'E':'0.97','left':{'predict':'No','n':2},'right':{'var':'Chol','val':254,'n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}}}}}},'right':{'var':'MaxHR','val':145,'n':79,'E':'0.51','left':{'predict':'Yes','n':50},'right':{'var':'ChestPain','val':'asymptomatic','n':29,'E':'0.89','left':{'var':'Age','val':40,'n':5,'E':'0.72','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':4}},'right':{'var':'ChestPain','val':'nonanginal','n':24,'E':'0.74','left':{'predict':'Yes','n':13},'right':{'var':'Chol','val':231,'n':11,'E':'0.99','left':{'predict':'Yes','n':4},'right':{'var':'Age','val':68,'n':7,'E':'0.86','left':{'predict':'No','n':4},'right':{'var':'RestBP','val':140,'n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}}}}}}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}
}
