/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl;

import org.junit.Test;
import us.parr.animl.classifiers.DecisionTree;
import us.parr.animl.classifiers.RandomForest;
import us.parr.animl.data.DataTable;

import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static us.parr.animl.TestRFBasics.MIN_NODE_SIZE;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;

public class TestRFDataSets extends BaseTest {
	@Test public void testRestaurantOnTrainingSet() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.restaurant));
		int N = 100; // try from 1 to 100 estimators
		int[] missed = new int[N];
		for (int k = 1; k<=N; k++) {
			RandomForest rf = RandomForest.train(data, k, MIN_NODE_SIZE);
			int miss = numberMisclassifications(data, rf);
			missed[k-1] = miss;
		}
		// randomness is reproducible via same seed in various classes
		int[] expected = new int[] {
			4, 2, 4, 1, 1, 1, 0, 0, 2, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		assertArrayEquals(expected, missed);
	}

	@Test public void testRestaurantOOBError() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.restaurant));
//		RandomForest rf = RandomForest.train(data, 200, MIN_NODE_SIZE);
//		double result = rf.getErrorEstimate(data);
//		System.out.println(result);
		int N = 100; // try from 1 to 100 estimators
		for (int k = 1; k<=N; k++) {
			RandomForest rf = RandomForest.train(data, k, MIN_NODE_SIZE);
			double result = rf.getErrorEstimate(data);
			System.out.println(result);
		}
	}

	@Test public void testWebsiteSignups() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.signups));
		DecisionTree tree = DecisionTree.build(data,0, MIN_NODE_SIZE);
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
//		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	@Test public void testHeartOnTrainingSet() {
		URL url = this.getClass().getClassLoader().getResource("Heart.csv");
		DataTable data = DataTable.loadCSV(url.getFile().toString(), "excel", null, null, true);
		int N = 100; // try from 1 to 100 estimators
		int[] missed = new int[N];
		for (int k = 1; k<=N; k++) {
			RandomForest rf = RandomForest.train(data, k, MIN_NODE_SIZE);
			int miss = numberMisclassifications(data, rf);
			missed[k-1] = miss;
		}
		// randomness is reproducible via same seed in various classes
		int[] expected = new int[] {
			32, 32, 12, 11, 8, 12, 3, 4, 3, 2, 4, 3, 2, 2, 4, 3, 5, 4, 2, 1,
			0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 1, 3, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		assertArrayEquals(expected, missed);
	}

	@Test public void testHeartOOBError() {
		URL url = this.getClass().getClassLoader().getResource("Heart.csv");
		DataTable data = DataTable.loadCSV(url.getFile().toString(), "excel", null, null, true);
		int N = 100; // try from 1 to 100 estimators
		for (int k = 1; k<=N; k++) {
			RandomForest rf = RandomForest.train(data, k, 1);
			double result = rf.getErrorEstimate(data);
			System.out.println(result);
		}
	}
}
