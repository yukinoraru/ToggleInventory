package com.github.yukinoraru.ToggleInventory;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.enchantments.Enchantment;

public class EnchantedBook {

    public static boolean isApplicable(ItemStack item) {
        switch (item.getType()) {
            case ENCHANTED_BOOK:
                return true;
            default:
                return false;
        }
    }

	public static Map<Enchantment, Integer> getEnchants(ItemStack item) {
    	EnchantmentStorageMeta esm = (EnchantmentStorageMeta)item.getItemMeta();
    	return esm.getStoredEnchants();
	}

    public static ItemStack addBookEnchantment(ItemStack item, Enchantment enchantment, int level){
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
        meta.addStoredEnchant(enchantment, level, true);
        item.setItemMeta(meta);
        return item;
    }
}
