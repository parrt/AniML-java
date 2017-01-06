package us.parr.rf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static us.parr.rf.misc.RFUtils.entropy;
import static us.parr.rf.misc.RFUtils.gini;
import static us.parr.rf.misc.RFUtils.isClose;

public class TestImpurity {
	public static final int HEADS = 0;
	public static final int TAILS = 1;

	@Test public void testEntropyFairCoinOneEach() {
		List<Integer> valueCounts = new ArrayList<>();
		valueCounts.add(1); // 1 head
		valueCounts.add(1); // 1 tail
		assertEquals(1.0, entropy(valueCounts));
	}

	@Test public void testEntropyFairCoinOneEach2() {
		List<Integer> valueCounts = new ArrayList<>();
		valueCounts.add(50); // 1 head
		valueCounts.add(50); // 1 tail
		assertEquals(1.0, entropy(valueCounts));
	}

	@Test public void testEntropyUnFairCoinOneEach() {
		List<Integer> valueCounts = new ArrayList<>();
		valueCounts.add(99); // 99 heads
		valueCounts.add(1);  // 1 tail
		assertTrue(isClose(0.08, entropy(valueCounts)));
	}

	@Test public void testEntropyZeroProbability() {
		List<Integer> valueCounts = new ArrayList<>();
		valueCounts.add(100);
		valueCounts.add(0);
		assertTrue(isClose(0.0, entropy(valueCounts)));
	}

	@Test public void testPatronEntropyFromRestaurant() {
		List<Integer> valueCounts = new ArrayList<>();
		valueCounts.add(0);
		valueCounts.add(1);
		assertEquals(1.0, entropy(valueCounts));
	}



	@Test public void testGiniFairCoinOneEach() {
		List<Integer> valueCounts = new ArrayList<>();
		valueCounts.add(1); // 1 head
		valueCounts.add(1); // 1 tail
		assertEquals(0.5, gini(valueCounts));
	}

	@Test public void testGiniUnFairCoinOneEach() {
		List<Integer> valueCounts = new ArrayList<>();
		valueCounts.add(99); // 99 heads
		valueCounts.add(1);  // 1 tail
		double expected = .99*(1-.99)+.01*(1-.01);
		assertTrue(isClose(expected, gini(valueCounts)));
	}
}
