package us.parr.rf;

import org.junit.Test;
import us.parr.rf.data.DataTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestTable {
	@Test public void testEmpty() {
		DataTable t = DataTable.empty(null, null);
		assertEquals("", t.toTestString());
	}
}
