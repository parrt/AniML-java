/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.classifiers.Classifier;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public abstract class DecisionTreeNode implements Classifier {
	// for debugging, fields below
	protected int numRecords;
	protected double entropy;

	public JsonObject toJSON() { return toJSON(this); }

	public abstract JsonObjectBuilder getJSONData();

	public static JsonObject toJSON(DecisionTreeNode t) {
		JsonObjectBuilder builder = t.getJSONData();
		if ( t instanceof DecisionSplitNode ) {
			DecisionSplitNode s = (DecisionSplitNode)t;
			builder.add("left", s.left.toJSON());
			builder.add("right", s.right.toJSON());
		}
		return builder.build();
	}

	public abstract String getDOTNodeDef();
}
