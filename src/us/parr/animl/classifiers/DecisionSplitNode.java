/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;

import static us.parr.animl.AniMath.isClose;

public class DecisionSplitNode extends DecisionTree {
	/** This node is split on which variable? */
	protected int splitVariable;

	/** Split at what variable value? */
	protected int splitValue;

	/** Left child if not a leaf node; non-null implies not a leaf node. */
	protected DecisionTree left;
	protected DecisionTree right;

	public DecisionSplitNode(int splitVariable, int splitValue) {
		this.splitVariable = splitVariable;
		this.splitValue = splitValue;
	}

	public int classify(int[] X) {
		if ( X[splitVariable] < splitValue ) {
			return left.classify(X);
		}
		else {
			return right.classify(X);
		}
	}

	@Override
	public JsonObject toJSON(String[] varnames, String[] catnames) {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		if ( varnames!=null ) {
			builder.add("var", varnames[splitVariable]);
		}
		else {
			builder.add("var", "x"+splitVariable);
		}
		builder.add("val", splitValue);
		builder.add("n", numRecords);
		if ( !isClose(entropy,0.0) ) {
			builder.add("E", String.format("%.2f",entropy));
		}
		builder.add("left", left.toJSON(varnames, catnames));
		builder.add("right", right.toJSON(varnames, catnames));
		return builder.build();
	}

	@Override
	protected void getDOTNodeNames(List<String> nodes, String[] varnames, String[] catnames) {
		DecisionSplitNode t = this;
		int id = System.identityHashCode(t);
		if ( varnames!=null ) {
			nodes.add(String.format("n%d [label=\"%s\\nn=%d\\nE=%.2f\"];",
			                        id, varnames[splitVariable], numRecords, entropy));
		}
		else {
			nodes.add(String.format("n%d [label=\"x%d\\nn=%d\\nE=%.2f\"];",
			                        id, splitVariable, numRecords, entropy));
		}
		left.getDOTNodeNames(nodes, varnames, catnames);
		right.getDOTNodeNames(nodes, varnames, catnames);
	}

	@Override
	protected void getDOTEdges(List<String> edges) {
		int id = System.identityHashCode(this);
		edges.add(String.format("n%s -> n%s [label=\"<%d\"];", id, System.identityHashCode(left), splitValue));
		edges.add(String.format("n%s -> n%s [label=\">=%d\"];", id, System.identityHashCode(right), splitValue));
		left.getDOTEdges(edges);
		right.getDOTEdges(edges);
	}
}
