package us.parr.rf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static us.parr.rf.RandomForest.INVALID_CATEGORY;

public class TestBasics {
	@Test
	public void testEmpty() throws Exception {
		RandomForest rf = new RandomForest(10);
		List<int[]> X = new ArrayList<>();
		List<Integer> Y = new ArrayList<>();
		int[] variables = new int[5];
		rf.train(X,variables,Y, 2);

		int[] test = new int[5];
		int result = rf.classify(test);
		int expecting = INVALID_CATEGORY;
		assertEquals(expecting, result);
	}
}
