package com.github.yukinoraru.ToggleInventory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InventoryManagerTest {

	ToggleInventory plugin;
	InventoryManager im;
	final String[] list = {"AAA", "BBB", "CCC", "DDD", "EEE", "FFF"};

	@Before
	public void setUp() throws Exception {
		plugin = new ToggleInventory();
		im = new InventoryManager(plugin);
	}

	@Test
	public void testCalcNextInv_normal_and_reverse() {
		for(int max = 2; max < 30; max++){
			int current = 1;
			for(int i = 0; i <= max*2; i++){
				current = im.calcNextInventoryIndex(max, current, true);
			}
			for(int i = 0; i <= max*2; i++){
				current = im.calcNextInventoryIndex(max, current, false);
			}
			assertEquals("current inv index must be 1", 1, current);
		}
	}

	@Test
	public void testFindSPInv_normal() throws Exception {
		String result = im.getNextSpecialInventory(list, "BBB", true);
		assertEquals("Get CCC", "CCC", result);
	}
	@Test
	public void testFindSPInv_reverse() throws Exception {
		String result = im.getNextSpecialInventory(list, "BBB", false);
		assertEquals("Get AAA", "AAA", result);
	}
	@Test
	public void testFindSPInv_boundaryCheck() throws Exception {
		String result1 = im.getNextSpecialInventory(list, "AAA", false);
		String result2 = im.getNextSpecialInventory(list, "FFF", true);
		assertEquals("Get FFF", "FFF", result1);
		assertEquals("Get AAA", "AAA", result2);
	}
	@Test(expected= ArrayIndexOutOfBoundsException.class)
	public void testFindSPInv_emptyList() throws Exception {
		String []emptyList = {};
		im.getNextSpecialInventory(emptyList, "", true);
		im.getNextSpecialInventory(emptyList, "", false);
	}
	@Test
	public void testFindSPInv_firstUse() throws Exception {
		String result = im.getNextSpecialInventory(list, "", true);
		assertEquals("Get AAA", "AAA", result);
	}

}
