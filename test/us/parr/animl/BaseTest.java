package us.parr.animl;

import us.parr.animl.classifiers.Classifier;
import us.parr.animl.classifiers.trees.DecisionTree;
import us.parr.animl.classifiers.trees.RandomForest;
import us.parr.animl.data.DataTable;
import us.parr.animl.validation.Validation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BaseTest {
	public static final String tmpdir = System.getProperty("java.io.tmpdir")+"/animl";

	public static String toTestString(DecisionTree tree) {
		if ( tree==null ) return "{}";
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
			int prediction = classifier.classify(data.getRow(i));
			if ( prediction!=data.getAsInt(i, data.getPredictedCol()) ) {
				System.out.println("oops");
			}
			assertEquals(prediction, data.getAsInt(i, data.getPredictedCol()));
		}
	}

	protected int numberMisclassifications(DataTable data, Classifier classifier) {
		int miss = 0;
		for (int i = 0; i<data.size(); i++) {
			int prediction = classifier.classify(data.getRow(i));
			if ( prediction!=data.getAsInt(i, data.getPredictedCol()) ) {
				miss++;
			}
		}
		return miss;
	}

	protected int[] trainingDataMisclassifications(DataTable data, int numEstimators, int minLeafSize) {
		int[] missed = new int[numEstimators];
		for (int k = 1; k<=numEstimators; k++) {
			RandomForest rf = new RandomForest(k, minLeafSize);
			rf.train(data);
			int miss = numberMisclassifications(data, rf);
			missed[k-1] = miss;
		}
		return missed;
	}

	/** For 1..maxEstimators (num trees), compute leave one out errors */
	protected int[] RF_leaveOneOutErrors(DataTable data, int minEstimators, int maxEstimators, int minLeafSize) {
		int[] missed = new int[maxEstimators-minEstimators+1];
		int i = 0;
		for (int k = minEstimators; k<=maxEstimators; k++) {
			RandomForest rf = new RandomForest(k, minLeafSize);
			missed[i] = Validation.leaveOneOut(rf, data);
			System.out.println(missed[i]);
			i++;
		}
		return missed;
	}

	/** For 1..n (num trees), compute k-fold errors */
	protected double[] RF_kFoldCrossErrors(DataTable data, int minEstimators, int maxEstimators, int folds, int minLeafSize) {
		double[] errors = new double[maxEstimators-minEstimators+1];
		int i = 0;
		for (int k = minEstimators; k<=maxEstimators; k++) {
			RandomForest rf = new RandomForest(k, minLeafSize);
			rf.train(data);
			errors[i] = Validation.kFoldCross(rf, folds, data);
			System.out.println(errors[i]);
			i++;
		}
		return errors;
	}
}
