/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.data.DataTable;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import static us.parr.animl.AniMath.isClose;
import static us.parr.animl.classifiers.trees.DecisionTree.INVALID_CATEGORY;

public class DecisionLeafNode extends DecisionTreeNode {
	/** The predicted category if this is a leaf node; non-leaf by default */
	protected int prediction = INVALID_CATEGORY;
	protected int predictionVariable;

	public DecisionLeafNode(int prediction, int predictionVariable) {
		this.prediction = prediction;
		this.predictionVariable = predictionVariable;
	}

	public int classify(int[] X) {
		return prediction;
	}

	@Override
	public JsonObjectBuilder getJSONData() {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		Object p = DataTable.getValue(data, prediction, predictionVariable);
		if ( p instanceof Integer ) {
			builder.add("predict", ((Integer)p));
		}
		else if ( p instanceof Float ) {
			builder.add("predict", ((Float)p));
		}
		else {
			builder.add("predict", p.toString());
		}
		builder.add("n", numRecords);
		if ( !isClose(entropy,0.0) ) {
			builder.add("E", String.format("%.2f",entropy));
		}
		return builder;
	}

	@Override
	public String getDOTNodeDef() {
		int id = System.identityHashCode(this);
		Object p = DataTable.getValue(data, prediction, predictionVariable);
		return String.format("n%d [shape=box, label=\"%s\\nn=%d\\nE=%.2f\"];",
		                     id, p.toString(), numRecords, entropy);
	}
}
