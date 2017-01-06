package us.parr.rf;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BaseTest {
	public static String toTestString(DecisionTree tree) {
		return tree.toJSON().toString().replaceAll("\"", "'");
	}

	public static String toTestString(DecisionTree tree, String[] varnames, String[] catnames) {
		return tree.toJSON(varnames,catnames).toString().replaceAll("\"", "'");
	}

	public void checkPredictions(List<int[]> data, DecisionTree tree) {
		for (int[] X : data) {
			int resultCat = tree.classify(X);
			int expectedCat = X[X.length-1];
			assertEquals(expectedCat, resultCat);
		}
	}

	public void checkPredictions(List<int[]> data, RandomForest forest) {
		for (int[] X : data) {
			int resultCat = forest.classify(X);
			int expectedCat = X[X.length-1];
			assertEquals(expectedCat, resultCat);
		}
	}
}
