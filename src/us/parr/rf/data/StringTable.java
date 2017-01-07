package us.parr.rf.data;

import java.util.LinkedHashMap;

/** A unique set of strings mapped to a monotonically increasing index.
 *  These indexes often useful to bytecode interpreters that have instructions
 *  referring to strings by unique integer. Indexing is from 0.
 *
 *  We can also get them back out in original order.
 *
 *  Yes, I know that this is similar to {@link String#intern()} but in this
 *  case, I need the index out not just to make these strings unique.
 *
 *  Copied from https://github.com/antlr/symtab
 */
public class StringTable {
	protected LinkedHashMap<String, Integer> table = new LinkedHashMap<String, Integer>();
	protected int index = -1; // index we have just written

	public int add(String s) {
		Integer I = table.get(s);
		if (I != null) return I;
		index++;
		table.put(s, index);
		return index;
	}
}