package us.parr.rf;

import java.util.ArrayList;
import java.util.List;

import static us.parr.rf.RandomForest.INVALID_CATEGORY;

public class DecisionTree {
	/** This node is split on which variable? */
	protected int splitVariable;

	/** Split at what variable value? */
	protected int splitValue;

	/** List of child nodes if not a leaf node; non-null implies not a leaf node. */
	protected List<DecisionTree> children;

	/** The predicted category if this is a leaf node; non-leaf by default */
	protected int category = INVALID_CATEGORY;

	public DecisionTree() {
	}

	public DecisionTree(int splitVariable, int splitValue) {
		this.splitVariable = splitVariable;
		this.splitValue = splitValue;
		this.children = new ArrayList<>();
	}

	public boolean isLeaf() { return children==null || category==INVALID_CATEGORY; }

	public void makeLeaf() { children=null; category=INVALID_CATEGORY; }
}
