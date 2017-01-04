package us.parr.rf;

public class DecisionTree {
	protected int splitVariable;
	protected int splitValue;

	public DecisionTree(int splitVariable, int splitValue) {
		this.splitVariable = splitVariable;
		this.splitValue = splitValue;
	}
}
