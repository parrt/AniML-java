/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers;

import us.parr.animl.data.DataTable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;

import static us.parr.animl.AniMath.isClose;

public class DecisionNumericSplitNode extends DecisionSplitNode {
	/** Split at what variable value? */
	protected double splitValue;

	public DecisionNumericSplitNode(int splitVariable, DataTable.VariableType colType, double splitValue) {
		super(splitVariable, colType);
		this.splitValue = splitValue;
	}

	public int classify(int[] X) {
		double v;
		if ( colType==DataTable.VariableType.NUMERICAL_INT ) {
			v = X[splitVariable];
		}
		else {
			v = Float.intBitsToFloat(X[splitVariable]);
		}
		if ( v < splitValue ) {
			return left.classify(X);
		}
		else {
			return right.classify(X);
		}
	}

	@Override
	public JsonObject toJSON() {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		builder.add("var", data.getColNames()[splitVariable]);
		builder.add("val", splitValue);
		builder.add("n", numRecords);
		if ( !isClose(entropy,0.0) ) {
			builder.add("E", String.format("%.2f",entropy));
		}
		builder.add("left", left.toJSON());
		builder.add("right", right.toJSON());
		return builder.build();
	}

	@Override
	protected void getDOTNodeNames(List<String> nodes) {
		DecisionNumericSplitNode t = this;
		int id = System.identityHashCode(t);
		nodes.add(String.format("n%d [label=\"%s\\nn=%d\\nE=%.2f\"];",
		                        id, data.getColNames()[splitVariable], numRecords, entropy));
		left.getDOTNodeNames(nodes);
		right.getDOTNodeNames(nodes);
	}

	@Override
	protected void getDOTEdges(List<String> edges) {
		int id = System.identityHashCode(this);
		edges.add(String.format("n%s -> n%s [label=\"<%.2f\"];", id, System.identityHashCode(left), splitValue));
		edges.add(String.format("n%s -> n%s [label=\">=%.2f\"];", id, System.identityHashCode(right), splitValue));
		left.getDOTEdges(edges);
		right.getDOTEdges(edges);
	}
}
