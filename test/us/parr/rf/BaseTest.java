package us.parr.rf;

public class BaseTest {
	public static String toTestString(DecisionTree tree) {
		return tree.toJSON().toString().replaceAll("\"", "'");
	}

	public static String toTestString(DecisionTree tree, String[] varnames, String[] catnames) {
		return tree.toJSON(varnames,catnames).toString().replaceAll("\"", "'");
	}
}
