/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.AniStats;
import us.parr.animl.data.CountingSet;
import us.parr.animl.data.DataTable;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.Map;

import static us.parr.animl.AniMath.isClose;
import static us.parr.animl.classifiers.trees.DecisionTree.INVALID_CATEGORY;

public class DecisionLeafNode extends DecisionTreeNode {
	/** The predicted category */
	protected int prediction = INVALID_CATEGORY;

	/** The predicted variable index */
	protected int predictionVariable;

	/** Track how many of each category we have in this leaf */
	protected CountingSet<Integer> categoryCounts;

	protected Map<Integer, Double> categoryProbabilities;

	public DecisionLeafNode(CountingSet<Integer> categoryCounts, int predictionVariable) {
		this.prediction = categoryCounts.argmax();
		this.predictionVariable = predictionVariable;
		this.entropy = AniStats.entropy(categoryCounts.counts());
		this.categoryCounts = categoryCounts;
		this.numRecords = categoryCounts.total();
		categoryProbabilities = new HashMap<>();
		for (Integer I : categoryCounts.keySet()) {
			categoryProbabilities.put(I, categoryCounts.get(I).v / (double)numRecords);
		}
	}

	public int classify(int[] X) {
		return prediction;
	}

	@Override
	public Map<Integer, Double> classProbabilities(int[] X) {
		return categoryProbabilities;
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
