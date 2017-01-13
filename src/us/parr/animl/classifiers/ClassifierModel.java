/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

import us.parr.animl.data.DataTable;

public interface ClassifierModel extends Classifier {
	/** A classifier object's constructor identifies the model parameters.
	 *  In an effort not to store datasets in each model, we use a separate
	 *  train() method to actually fit the model to the data.
	 *
	 *  Repeatedly calling this method, clears the model before training
	 *  each time.
	 */
	void train(DataTable data);
}
