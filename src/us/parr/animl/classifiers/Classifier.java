/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

import java.util.Map;

/** A classifier can be trained and can classify unknown X vectors.
 *  It can also return the class probabilities, not just the most likely.
 *
 *  The fundamental "row" data type is int[] as we can encode strings
 *  as indexes and floats as the raw IEEE-754 floating-point bits.
 *  {@see Float#floatToIntBits()}.
 */
public interface Classifier {
	/** Given an unknown vector X of predictor variables (and usually
	 *  the predicted category, which is ignored), returned the
	 *  category predicted by the classifier model.
	 */
	int classify(int[] X);

	/** Return the set of class->class_probability items.
	 *  E.g., a decision tree uses the proportion of
	 *  instances in a leaf node associated with the predicted
	 *  category as a class probability. Given that category values
	 *  could be really large and sparse, use a map not double[]
	 *  as return value.
	 */
	Map<Integer, Double> classProbabilities(int[] X);
}
