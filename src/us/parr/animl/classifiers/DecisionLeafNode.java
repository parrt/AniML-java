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

public class DecisionLeafNode extends DecisionTree {
	/** The predicted category if this is a leaf node; non-leaf by default */
	protected int category = RandomForest.INVALID_CATEGORY;

	public DecisionLeafNode(int predictedCategory) {
		this.category = predictedCategory;
	}

	public int classify(int[] X) {
		return category;
	}

	@Override
	public JsonObject toJSON(String[] varnames, String[] catnames) {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		if ( catnames!=null ) {
			builder.add("predict", catnames[category]);
		}
		else {
			builder.add("predict", category);
		}
		builder.add("n", numRecords);
		if ( !isClose(entropy,0.0) ) {
			builder.add("E", String.format("%.2f",entropy));
		}
		return builder.build();
	}

	@Override
	protected void getDOTNodeNames(List<String> nodes, String[] varnames, String[] catnames) {
		int id = System.identityHashCode(this);
		if ( catnames!=null ) {
			nodes.add(String.format("n%d [shape=box, label=\"%s\\nn=%d\\nE=%.2f\"];",
			                        id, catnames[category], numRecords, entropy));
		}
		else {
			nodes.add(String.format("n%d [shape=box, label=\"y%d\\nn=%d\\nE=%.2f\"];",
			                        id, category, numRecords, entropy));
		}
	}

	@Override
	protected void getDOTEdges(List<String> edges) { }
}
