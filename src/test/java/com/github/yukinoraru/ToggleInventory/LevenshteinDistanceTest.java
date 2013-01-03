package com.github.yukinoraru.ToggleInventory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LevenshteinDistanceTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testComputeLevenshteinDistance() {
		int d = LevenshteinDistance
				.computeLevenshteinDistance("minecraft", "Minecraft");
		assertEquals("Distance", 1, d);
	}

	@Test
	public void testFind() {
		final String[] list = {"AAA", "BBB", "CCC", "DDD", "EEE", "FFF"};
		final String[] words = {"A", "B", "C", "D", "E", "F"};

		for(int i=0; i < list.length; i++){
			int result = LevenshteinDistance.find(list, words[i]);
			assertEquals("Find", i, result);
		}

	}

}
