package com.github.yukinoraru.ToggleInventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Class, that allows setting and getting skin fo skull. Based on
 * PrettyScaryLib.Skull
 */
public class Skull {

	/**
	 * Sets skin.
	 * 
	 * @param item
	 *            item
	 * @param nick
	 *            nick
	 * @return item stack
	 */
	public static ItemStack setSkin(ItemStack item, String nick) {
		if (!isApplicable(item))
			return null;
		SkullMeta sm = (SkullMeta) item.getItemMeta();
		sm.setOwner(nick);
		item.setItemMeta(sm);
		return item;
	}

	/**
	 * Gets skin.
	 * 
	 * @param item
	 *            item
	 * @return owner name
	 */
	public static String getSkin(ItemStack item) {
		if (!isApplicable(item))
			return null;
		return ((SkullMeta) item.getItemMeta()).getOwner();
	}

	/**
	 * Checks if is applicable.
	 * 
	 * @param item
	 *            item
	 * @return true, if is applicable
	 */
	public static boolean isApplicable(ItemStack item) {
		switch (item.getType()) {
		case SKULL_ITEM:
			return true;
		default:
			return false;
		}
	}
}
