package us.parr.animl;

import us.parr.animl.classifiers.DecisionTree;
import us.parr.animl.classifiers.RandomForest;
import us.parr.animl.data.DataTable;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BaseTest {
	public static final String tmpdir = System.getProperty("java.io.tmpdir")+"/animl";

	public static String toTestString(DecisionTree tree) {
		return tree.toJSON().toString().replaceAll("\"", "'");
	}

	public void checkPredictions(List<int[]> data, DecisionTree tree) {
		for (int[] X : data) {
			int resultCat = tree.classify(X);
			int expectedCat = X[X.length-1];
			assertEquals(expectedCat, resultCat);
		}
	}

	public void checkPredictions(DataTable data, RandomForest rf) {
		for (int i = 0; i<data.size(); i++) {
			int prediction = rf.classify(data.get(i));
			assertEquals(prediction, data.get(i, data.getPredictedCol()));
		}
	}

	protected int numberMisclassifications(DataTable data, RandomForest rf) {
		int miss = 0;
		for (int i = 0; i<data.size(); i++) {
			int prediction = rf.classify(data.get(i));
			if ( prediction!=data.get(i, data.getPredictedCol()) ) {
				miss++;
			}
		}
		return miss;
	}
}
