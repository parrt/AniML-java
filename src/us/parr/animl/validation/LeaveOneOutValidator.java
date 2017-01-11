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

public class LeaveOneOutValidator {
	public static final int SEED = 333888333; // need randomness but use same seed to get reproducibility
	public static final Random random = new Random(SEED);

	protected DataTable data;
	protected Classifier classifier;

	public LeaveOneOutValidator(DataTable data, Classifier classifier) {
		this.data = data;
		this.classifier = classifier;
	}

	public int validate() {
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
}
