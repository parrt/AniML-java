package us.parr.animl;

import us.parr.animl.classifiers.Classifier;
import us.parr.animl.classifiers.DecisionTree;
import us.parr.animl.data.DataTable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BaseTest {
	public static final String tmpdir = System.getProperty("java.io.tmpdir")+"/animl";

	public static String toTestString(DecisionTree tree) {
		return tree.toJSON().toString().replaceAll("\"", "'");
	}

	public void checkPredictions(List<int[]> data, Classifier classifier) {
		for (int[] X : data) {
			int resultCat = classifier.classify(X);
			int expectedCat = X[X.length-1];
			assertEquals(expectedCat, resultCat);
		}
	}

	public List<Integer> predictions(List<int[]> data, Classifier classifier) {
		List<Integer> p = new ArrayList<>();
		for (int[] X : data) {
			int resultCat = classifier.classify(X);
			p.add(resultCat);
		}
		return p;
	}

	public void checkPredictions(DataTable data, Classifier classifier) {
		for (int i = 0; i<data.size(); i++) {
			int prediction = classifier.classify(data.getRowAsInts(i));
			assertEquals(prediction, data.getAsInt(i, data.getPredictedCol()));
		}
	}

	protected int numberMisclassifications(DataTable data, Classifier classifier) {
		int miss = 0;
		for (int i = 0; i<data.size(); i++) {
			int prediction = classifier.classify(data.getRowAsInts(i));
			if ( prediction!=data.getAsInt(i, data.getPredictedCol()) ) {
				miss++;
			}
		}
		return miss;
	}
}
