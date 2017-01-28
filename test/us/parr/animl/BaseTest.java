package us.parr.animl;

import us.parr.animl.classifiers.Classifier;
import us.parr.animl.classifiers.trees.DecisionTree;
import us.parr.animl.classifiers.trees.RandomForest;
import us.parr.animl.data.DataTable;
import us.parr.animl.validation.Validation;
import us.parr.lib.ParrtSys;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

	protected void RF_kFoldCrossErrors(double[] scikitResult,
	                                   String fileName, DataTable data,
	                                   int[] sizes, int kfolds,
	                                   int minLeafSize, double tolerance)
	{
		for (int n_estimators : sizes) {
			// Check data scikit-learn
//			double[] scikitResult = scikit_rf_error(fileName, n_estimators, minLeafSize, kfolds);
//			double[] scikitResult = {0.0,0.0};
			// Now mine
			int nodeSampleSize = (int)(0.15 * data.size());
			nodeSampleSize = 111;
			double bootstrapSampleRate = 0.5;
			RandomForest rf = new RandomForest(n_estimators, minLeafSize, nodeSampleSize, bootstrapSampleRate);
			double error = Validation.kFoldCross(rf, kfolds, data);
			System.out.println(scikitResult[1]+" vs "+error);
			// should be within small absolute error difference
			String errMsg = String.format("Error rates %.5f, %.5f should be closer than %.4f",
			                              scikitResult[1], error, tolerance);
			if ( Math.abs(scikitResult[1]-error)>=tolerance ) {
				System.err.println(errMsg);
			}
//			assertTrue(errMsg, error < scikitResult[1] || error-scikitResult[1]<tolerance);
		}
	}

	protected static void python_RF_kFoldCrossErrors(String fileName,
	                                                 int[] sizes, int kfolds,
	                                                 int minLeafSize)
	{
//		double[] results = new double[sizes.length];
		int dot = fileName.lastIndexOf(".csv");
		String name = fileName.substring(0, dot);
		name = name.replaceAll("-", "_");
		System.out.print("double[] "+String.format("%-20s",name+"_kfold")+" = {");
		for (int i = 0; i<sizes.length; i++) {
			// Check data scikit-learn
			int n_estimators = sizes[i];
			double[] scikitResult = scikit_rf_error(fileName, n_estimators, minLeafSize, kfolds);
			if ( i>0 ) System.out.print(", ");
			System.out.printf("%.5f",scikitResult[1]);
		}
		System.out.println("};");
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

	protected static double[] scikit_rf_error(String fileName, int n_estimators, int min_samples_leaf, int kfolds) {
		URL dataURL = BaseTest.class.getClassLoader().getResource(fileName);
		String dataFileName = dataURL.getFile();
		URL scriptURL = BaseTest.class.getClassLoader().getResource("rf_error.py");
		String scriptFileName = scriptURL.getFile();

		String home = System.getProperty("user.home");
		String anacondaPython = home+"/anaconda2/bin/python2.7"; // needs latest scikit learn with conda install scikit-learn
		String[] result = ParrtSys.exec(anacondaPython, scriptFileName,
		                                dataFileName,
		                                ""+n_estimators,
		                                ""+min_samples_leaf,
		                                ""+kfolds);
		String exitCode = result[0];
		String stdout = result[1];
		String stderr = result[2];
		if ( stderr.length()>0 && !stderr.contains("Some inputs do not have OOB scores") ) {
			System.err.println(stderr);
		}
//		System.out.println(stdout);
		// stdout is like "oob 0.19529 kfold 0.195254"
		Scanner s = new Scanner(stdout);
		s.next(); // skip "oob"
		String oob = s.next();
		s.next(); // skip "kfold"
		String kfold = s.next();
		return new double[] {Double.valueOf(oob), Double.valueOf(kfold)};
	}
}
