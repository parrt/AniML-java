/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.data.DataTable;
import us.parr.lib.collections.CountingSet;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.Map;

import static us.parr.animl.classifiers.trees.DecisionTree.INVALID_CATEGORY;
import static us.parr.lib.ParrtMath.isClose;

public class DecisionLeafNode extends DecisionTreeNode {
	/** The predicted category */
	protected int targetCategory = INVALID_CATEGORY;

	/** The predicted variable index */
	protected int targetVariable;

	/** What kind of variable is the target variable? */
	protected Object targetCategoryDisplayValue;

	/** Track how many of each category we have in this leaf */
	protected CountingSet<Integer> categoryCounts;

	public DecisionLeafNode(DataTable data, CountingSet<Integer> categoryCounts, int targetVariable) {
		this.targetCategory = categoryCounts.argmax();
		this.targetVariable = targetVariable;
		this.entropy = (float)categoryCounts.entropy();
		this.categoryCounts = categoryCounts;
		this.numRecords = categoryCounts.total();
		targetCategoryDisplayValue = DataTable.getValue(data, targetCategory, targetVariable);
	}

	public int classify(int[] X) {
		return targetCategory;
	}

	@Override
	public Map<Integer, Double> classProbabilities(int[] X) {
		Map<Integer, Double> categoryProbabilities = new HashMap<>();
		for (Integer I : categoryCounts.keySet()) {
			categoryProbabilities.put(I, categoryCounts.count(I) / (double)numRecords);
		}
		return categoryProbabilities;
	}

	@Override
	public JsonObjectBuilder getJSONData() {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		if ( targetCategoryDisplayValue instanceof Integer ) {
			builder.add("predict", ((Integer)targetCategoryDisplayValue));
		}
		else if ( targetCategoryDisplayValue instanceof Float ) {
			builder.add("predict", ((Float)targetCategoryDisplayValue));
		}
		else {
			builder.add("predict", targetCategoryDisplayValue.toString());
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
		return String.format("n%d [shape=box, label=\"%s\\nn=%d\\nE=%.2f\"];",
		                     id, targetCategoryDisplayValue, numRecords, entropy);
	}
}
