/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

public abstract class DecisionSplitNode extends DecisionTree {
	/** This node is split on which variable? */
	protected int splitVariable;

	protected DecisionTree left;
	protected DecisionTree right;
}
