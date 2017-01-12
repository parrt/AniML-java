package us.parr.animl;

import org.junit.Test;
import us.parr.animl.classifiers.trees.DecisionTree;
import us.parr.animl.data.DataTable;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
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
		DecisionTree.debug = true;
		DecisionTree tree = new DecisionTree();
		tree.train(data);
		// I verified this string by looking at DOT output, but similar to Russell and Norvig AI book
		String expecting = "{'var':'Patrons','cat':'Some','n':12,'E':'1.00','left':{'predict':'Yes','n':4},'right':{'var':'Hungry','cat':'Yes','n':8,'E':'0.81','left':{'var':'Fri&Sat','cat':'No','n':4,'E':'1.00','left':{'predict':'No','n':1},'right':{'var':'Price','cat':'$$$','n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}},'right':{'predict':'No','n':4}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	@Test public void testWebsiteSignups() {
		DataTable data = DataTable.fromStrings(Arrays.asList(signups));
		DecisionTree tree = new DecisionTree();
		tree.train(data);
		// Same tree as shown here; http://www.patricklamle.com/Tutorials/Decision%20tree%20python/tuto_decision%20tree.html
		String expecting = "{'var':'referrer','cat':'google','n':16,'E':'1.51','left':{'var':'pageviews','val':19.5,'n':5,'E':'1.37','left':{'var':'readfaq','cat':'yes','n':2,'E':'1.00','left':{'predict':'Basic','n':1},'right':{'predict':'None','n':1}},'right':{'predict':'Premium','n':3}},'right':{'var':'referrer','cat':'slashdot','n':11,'E':'0.99','left':{'predict':'None','n':3},'right':{'var':'readfaq','cat':'yes','n':8,'E':'0.95','left':{'predict':'Basic','n':4},'right':{'var':'pageviews','val':20.0,'n':4,'E':'0.81','left':{'predict':'None','n':3},'right':{'predict':'Basic','n':1}}}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	@Test public void testHeartDataSenseTypes() {
		URL url = this.getClass().getClassLoader().getResource("Heart-wo-NA.csv");
		DataTable data = DataTable.loadCSV(url.getFile().toString(), "excel", null, null, true);
		data.setColType(0, UNUSED_INT); // first column is ID
		DecisionTree.debug = true;
		DecisionTree tree = new DecisionTree(0, 1);
		tree.train(data);
		// The DOT tree looks pretty good. Similar to first two levels in ISL book's tree; diff than scikit-learn though.
		String expecting = "{'var':'Thal','cat':'normal','n':297,'E':'1.00','left':{'var':'Ca','val':0.5,'n':164,'E':'0.77','left':{'var':'Age','val':57.5,'n':115,'E':'0.51','left':{'var':'Oldpeak','val':3.200000047683716,'n':80,'E':'0.23','left':{'var':'RestBP','val':109.0,'n':77,'E':'0.10','left':{'var':'Age','val':46.5,'n':8,'E':'0.54','left':{'predict':'No','n':5},'right':{'var':'Age','val':49.0,'n':3,'E':'0.92','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':2}}},'right':{'predict':'No','n':69}},'right':{'var':'Age','val':41.5,'n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}},'right':{'var':'Fbs','val':0.5,'n':35,'E':'0.86','left':{'var':'Age','val':67.5,'n':29,'E':'0.93','left':{'var':'MaxHR','val':170.0,'n':25,'E':'0.97','left':{'var':'RestBP','val':115.0,'n':21,'E':'1.00','left':{'predict':'No','n':2},'right':{'var':'Chol','val':347.5,'n':19,'E':'1.00','left':{'var':'Age','val':63.5,'n':17,'E':'0.98','left':{'var':'Chol','val':232.5,'n':11,'E':'0.85','left':{'var':'Age','val':59.5,'n':5,'E':'0.97','left':{'predict':'No','n':2},'right':{'var':'Chol','val':203.0,'n':3,'E':'0.92','left':{'predict':'Yes','n':2},'right':{'predict':'No','n':1}}},'right':{'predict':'Yes','n':6}},'right':{'var':'Sex','val':0.5,'n':6,'E':'0.92','left':{'predict':'No','n':3},'right':{'var':'RestECG','val':1.0,'n':3,'E':'0.92','left':{'predict':'Yes','n':2},'right':{'predict':'No','n':1}}}},'right':{'predict':'No','n':2}}},'right':{'predict':'No','n':4}},'right':{'predict':'No','n':4}},'right':{'predict':'No','n':6}}},'right':{'var':'ChestPain','cat':'asymptomatic','n':49,'E':'1.00','left':{'var':'Sex','val':0.5,'n':20,'E':'0.61','left':{'var':'Age','val':63.5,'n':6,'E':'1.00','left':{'var':'Age','val':59.5,'n':4,'E':'0.81','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':3}},'right':{'predict':'No','n':2}},'right':{'predict':'Yes','n':14}},'right':{'var':'Age','val':65.5,'n':29,'E':'0.80','left':{'var':'Age','val':55.5,'n':22,'E':'0.90','left':{'var':'Chol','val':173.5,'n':13,'E':'0.39','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':12}},'right':{'var':'ChestPain','cat':'nonanginal','n':9,'E':'0.92','left':{'predict':'No','n':2},'right':{'var':'Chol','val':199.5,'n':7,'E':'0.59','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':6}}}},'right':{'predict':'No','n':7}}}},'right':{'var':'Ca','val':0.5,'n':133,'E':'0.81','left':{'var':'ExAng','val':0.5,'n':59,'E':'0.99','left':{'var':'Age','val':51.0,'n':33,'E':'0.92','left':{'var':'RestECG','val':1.0,'n':13,'E':'0.96','left':{'var':'Oldpeak','val':0.8999999761581421,'n':9,'E':'0.99','left':{'var':'Age','val':40.5,'n':6,'E':'0.65','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':5}},'right':{'predict':'Yes','n':3}},'right':{'predict':'Yes','n':4}},'right':{'var':'Age','val':56.5,'n':20,'E':'0.61','left':{'predict':'No','n':8},'right':{'var':'Oldpeak','val':0.3500000238418579,'n':12,'E':'0.81','left':{'var':'Age','val':61.5,'n':3,'E':'0.92','left':{'predict':'Yes','n':2},'right':{'predict':'No','n':1}},'right':{'var':'Age','val':66.5,'n':9,'E':'0.50','left':{'predict':'No','n':7},'right':{'var':'Sex','val':0.5,'n':2,'E':'1.00','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':1}}}}}},'right':{'var':'Oldpeak','val':1.5499999523162842,'n':26,'E':'0.71','left':{'var':'Chol','val':240.5,'n':11,'E':'0.99','left':{'var':'RestBP','val':143.0,'n':6,'E':'0.65','left':{'predict':'No','n':5},'right':{'predict':'Yes','n':1}},'right':{'predict':'Yes','n':5}},'right':{'predict':'Yes','n':15}}},'right':{'var':'RestECG','val':0.5,'n':74,'E':'0.41','left':{'var':'MaxHR','val':145.0,'n':34,'E':'0.67','left':{'var':'Oldpeak','val':0.7000000476837158,'n':20,'E':'0.29','left':{'var':'Age','val':65.0,'n':2,'E':'1.00','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':1}},'right':{'predict':'Yes','n':18}},'right':{'var':'MaxHR','val':155.0,'n':14,'E':'0.94','left':{'predict':'No','n':4},'right':{'var':'Chol','val':151.5,'n':10,'E':'0.47','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':9}}}},'right':{'predict':'Yes','n':40}}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	/*
	def check_iris_criterion(name, criterion):
	    # Check consistency on dataset iris.
	    ForestClassifier = FOREST_CLASSIFIERS[name]

	    clf = ForestClassifier(n_estimators=10, criterion=criterion,
	                           random_state=1)
	    clf.fit(iris.data, iris.target)
	    score = clf.score(iris.data, iris.target)
	    assert_greater(score, 0.9, "Failed with criterion %s and score = %f"
	                               % (criterion, score))

	    clf = ForestClassifier(n_estimators=10, criterion=criterion,
	                           max_features=2, random_state=1)
	    clf.fit(iris.data, iris.target)
	    score = clf.score(iris.data, iris.target)
	    assert_greater(score, 0.5, "Failed with criterion %s and score = %f"
	                               % (criterion, score))
	 */
	@Test public void testIris() {
		URL url = this.getClass().getClassLoader().getResource("iris.csv");
		DataTable data = DataTable.loadCSV(url.getFile().toString(), null, null, null, true);
		DecisionTree tree = new DecisionTree(0, 1);
		tree.train(data);
		// This is exact same tree as shown here http://scikit-learn.org/stable/modules/tree.html
		String expecting = "{'var':' petal len','val':2.450000047683716,'n':150,'E':'1.58','left':{'predict':'Iris-setosa','n':50},'right':{'var':' petal wid','val':1.75,'n':100,'E':'1.00','left':{'var':' petal len','val':4.949999809265137,'n':54,'E':'0.45','left':{'var':' petal wid','val':1.6500000953674316,'n':48,'E':'0.15','left':{'predict':'Iris-versicolor','n':47},'right':{'predict':'Iris-virginica','n':1}},'right':{'var':' petal wid','val':1.5499999523162842,'n':6,'E':'0.92','left':{'predict':'Iris-virginica','n':3},'right':{'var':'sepal len','val':6.949999809265137,'n':3,'E':'0.92','left':{'predict':'Iris-versicolor','n':2},'right':{'predict':'Iris-virginica','n':1}}}},'right':{'var':' petal len','val':4.850000381469727,'n':46,'E':'0.15','left':{'var':'sepal len','val':5.949999809265137,'n':3,'E':'0.92','left':{'predict':'Iris-versicolor','n':1},'right':{'predict':'Iris-virginica','n':2}},'right':{'predict':'Iris-virginica','n':43}}}}";
		String result = toTestString(tree);
//		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		List<Integer> p = predictions(data.getRows(), tree);
		Integer[] expectedPredictions = new Integer[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2
		};
		System.out.println(p);
		assertArrayEquals(expectedPredictions, p.toArray());
	}
}
