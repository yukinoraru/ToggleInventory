package com.github.yukinoraru.ToggleInventory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Class, that allows setting and getting name and lore of item. Based on
 * PrettyScaryLib.Namer
 */
public class Namer {

	/**
	 * Sets name.
	 * 
	 * @param item
	 *            item
	 * @param name
	 *            name
	 * @return item stack
	 */
	public static ItemStack setName(ItemStack item, String name) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		item.setItemMeta(im);
		return item;
	}

	public static ItemStack setNameInURLEncoded(ItemStack item,
			String encoded_name) throws UnsupportedEncodingException {
		String name = URLDecoder.decode(encoded_name, "UTF-8");
		return setName(item, name);
	}

	public static ItemStack setLoreInURLEncoded(ItemStack item,
			String... encoded_lores) throws UnsupportedEncodingException {
		String[] lores = new String[encoded_lores.length];
		for (int m = 0; m < lores.length; m++) {
			lores[m] = URLDecoder.decode(encoded_lores[m], "UTF-8");
		}
		return setLore(item, lores);
	}

	public static String getNameInURLEncoded(ItemStack item)
			throws UnsupportedEncodingException {
		String name = getName(item);
		return (name != null) ? URLEncoder.encode(name, "UTF-8") : "";
	}

	public static String[] getLoreInURLEncoded(ItemStack item)
			throws UnsupportedEncodingException {
		String[] lores = getLore(item);
		if (lores != null) {
			String[] encoded_lores = new String[lores.length];
			for (int m = 0; m < lores.length; m++) {
				encoded_lores[m] = URLEncoder.encode(lores[m], "UTF-8");
			}
			return encoded_lores;
		} else {
			return null;
		}
	}

	/**
	 * Gets name.
	 * 
	 * @param item
	 *            item
	 * @return name
	 */
	public static String getName(ItemStack item) {
		return item.getItemMeta().getDisplayName();
	}

	/**
	 * Sets lore.
	 * 
	 * @param item
	 *            item
	 * @param lore
	 *            lore
	 * @return item stack
	 */
	public static ItemStack setLore(ItemStack item, String... lore) {
		ItemMeta im = item.getItemMeta();
		List<String> newLore = new ArrayList<String>();
		for (String l : lore) {
			newLore.add(l);
		}
		im.setLore(newLore);
		item.setItemMeta(im);
		return item;
	}

	/**
	 * Adds lore.
	 * 
	 * @param item
	 *            item
	 * @param lore
	 *            lore
	 * @return item stack
	 */
	public static ItemStack addLore(ItemStack item, String lore) {
		ItemMeta im = item.getItemMeta();
		List<String> newLore = im.getLore();
		newLore.add(lore);
		im.setLore(newLore);
		item.setItemMeta(im);
		return item;
	}

	/**
	 * Gets lore.
	 * 
	 * @param item
	 *            item
	 * @return lore
	 */
	public static String[] getLore(ItemStack item) {
		ItemMeta im = item.getItemMeta();
		if (im.getLore() != null) {

			String[] lores = new String[im.getLore().size()];
			for (int i = 0; i < im.getLore().size(); i++) {
				lores[i] = im.getLore().get(i).toString();
			}
			return lores;
		} else {
			return null;
		}
	}
}
