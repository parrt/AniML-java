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

public class DecisionLeafNode extends DecisionTree {
	/** The predicted category if this is a leaf node; non-leaf by default */
	protected int prediction = RandomForest.INVALID_CATEGORY;
	protected int predictionVariable;

	public DecisionLeafNode(int prediction, int predictionVariable) {
		this.prediction = prediction;
		this.predictionVariable = predictionVariable;
	}

	public int classify(int[] X) {
		return prediction;
	}

	@Override
	public JsonObject toJSON() {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		String p = DataTable.getValue(data, prediction, predictionVariable).toString();
		builder.add("predict", p);
		builder.add("n", numRecords);
		if ( !isClose(entropy,0.0) ) {
			builder.add("E", String.format("%.2f",entropy));
		}
		return builder.build();
	}

	@Override
	protected void getDOTNodeNames(List<String> nodes) {
		int id = System.identityHashCode(this);
		String p = DataTable.getValue(data, prediction, predictionVariable).toString();
		nodes.add(String.format("n%d [shape=box, label=\"%s\\nn=%d\\nE=%.2f\"];",
		                        id, p, numRecords, entropy));
	}

	@Override
	protected void getDOTEdges(List<String> edges) { }
}
