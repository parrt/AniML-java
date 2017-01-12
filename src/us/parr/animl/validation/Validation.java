/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.validation;

import us.parr.animl.classifiers.Classifier;
import us.parr.animl.data.DataTable;

import java.util.List;
import java.util.Random;

public class Validation {
	public static final int SEED = 333888333; // need randomness but use same seed to get reproducibility
	public static final Random random = new Random(SEED);

	public static int leaveOneOut(DataTable data, Classifier classifier) {
		int miss = 0;
		for (int whichToLeaveOut = 0; whichToLeaveOut<data.size(); whichToLeaveOut++) {
			DataTable subset = new DataTable(data); // shallow copy data set
			List<int[]> rows = subset.getRows();
			int[] leaveOut = rows.get(whichToLeaveOut);
			rows.remove(whichToLeaveOut);
			int cat = classifier.classify(leaveOut);
			int trueCat = leaveOut[data.getPredictedCol()];
			if ( cat!=trueCat ) {
				miss++;
			}
		}
		return miss;
	}

	public static int kFoldCross(int k, DataTable data, Classifier classifier) {
		int n = data.size();
		int foldSize = n / k;
		int[] indexes = new int[k];
		int miss = 0;
		for (int i = 0; i<indexes.length; i++) {
			int start = i * foldSize;
			int stop = start + foldSize - 1;
			DataTable subset = data.subset(start, stop);
//			int cat = classifier.classify(leaveOut);
//			int trueCat = leaveOut[data.getPredictedCol()];
//			if ( cat!=trueCat ) {
//				miss++;
//			}
		}
		int remainder = n % k;
		assert remainder + foldSize * k == n;
		return 0;
	}
}
