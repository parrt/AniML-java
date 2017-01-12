/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.data.DataTable;

public abstract class DecisionSplitNode extends DecisionTreeNode {
	/** This node is split on which variable? */
	protected int splitVariable;
	protected DataTable.VariableType colType;

	protected DecisionTreeNode left;
	protected DecisionTreeNode right;

	public DecisionSplitNode(int splitVariable, DataTable.VariableType colType) {
		this.splitVariable = splitVariable;
		this.colType = colType;
	}

	public abstract String getDOTLeftEdge();
	public abstract String getDOTRightEdge();
}
