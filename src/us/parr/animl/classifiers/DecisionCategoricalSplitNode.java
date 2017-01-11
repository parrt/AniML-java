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

public class DecisionCategoricalSplitNode extends DecisionSplitNode {
	/** Split according to what variable category? An unknown matches category yes or no */
	protected int splitCategory;

	public DecisionCategoricalSplitNode(int splitVariable, DataTable.VariableType colType, int splitCategory) {
		super(splitVariable, colType);
		this.splitCategory = splitCategory;
	}

	public int classify(int[] X) {
		if ( X[splitVariable]==splitCategory ) { // if equal, choose left child
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
		Object p = DataTable.getValue(data, splitCategory, splitVariable);
		builder.add("cat", p.toString()); // has to be categorical
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
		DecisionCategoricalSplitNode t = this;
		int id = System.identityHashCode(t);
		nodes.add(String.format("n%d [label=\"%s\\nn=%d\\nE=%.2f\"];",
		                        id, data.getColNames()[splitVariable], numRecords, entropy));
		left.getDOTNodeNames(nodes);
		right.getDOTNodeNames(nodes);
	}

	@Override
	protected void getDOTEdges(List<String> edges) {
		int id = System.identityHashCode(this);
		Object p = DataTable.getValue(data, splitCategory, splitVariable);
		edges.add(String.format("n%s -> n%s [label=\"%s\"];", id, System.identityHashCode(left), p.toString()));
		edges.add(String.format("n%s -> n%s [label=\"!%s\"];", id, System.identityHashCode(right), p.toString()));
		left.getDOTEdges(edges);
		right.getDOTEdges(edges);
	}
}
