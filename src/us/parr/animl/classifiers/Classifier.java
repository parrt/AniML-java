/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

import us.parr.animl.data.DataTable;

/** A classifier can be trained and can classify unknown X vectors.
 *  The fundamental "row" data type is int[] as we can encode strings
 *  as indexes and floats as the raw IEEE-754 floating-point bits.
 */
public interface Classifier {
	void train(DataTable data);
	int classify(int[] X);
}
