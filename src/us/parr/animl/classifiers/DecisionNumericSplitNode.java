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
	protected int splitValue;

	public DecisionNumericSplitNode(int splitVariable, int splitValue) {
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
	public JsonObject toJSON() {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		builder.add("var", data.getColNames()[splitVariable]);
		Object p = DataTable.getValue(data, splitValue, splitVariable);
		if ( p instanceof Integer ) {
			builder.add("val", ((Integer)p));
		}
		else if ( p instanceof Float ) {
			builder.add("val", ((Float)p));
		}
		else {
			builder.add("val", p.toString());
		}
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
		Object p = DataTable.getValue(data, splitValue, splitVariable);
		edges.add(String.format("n%s -> n%s [label=\"<%s\"];", id, System.identityHashCode(left), p.toString()));
		edges.add(String.format("n%s -> n%s [label=\">=%s\"];", id, System.identityHashCode(right), p.toString()));
		left.getDOTEdges(edges);
		right.getDOTEdges(edges);
	}
}
