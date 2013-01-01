package com.github.yukinoraru.ToggleInventory;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 * Class, that allows setting and getting color of the leather armor.
 * Based on PrettyScaryLib.Armor
 */
public class Armor {

    /**
     * Sets the color.
     *
     * @param item item to color
     * @param color color
     * @return colored item
     * @throws Exception thrown,  when item is not applicable
     */
    public static ItemStack setColor(ItemStack item, int color) {
        if (!isApplicable(item))
            return null;
        LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
        lam.setColor(Color.fromRGB(color));
        item.setItemMeta(lam);
        return item;
    }

    /**
     * Checks if item is applicable.
     *
     * @param item the item to check
     * @return true, if is applicable
     */
    public static boolean isApplicable(ItemStack item) {
        switch (item.getType()) {
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Sets the color.
     *
     * @param item item to color
     * @param color color
     * @return colored item
     * @throws Exception thrown,  when item is not applicable
     */
    public static ItemStack setColor(ItemStack item, Color color) {
        if (!isApplicable(item))
            return null;
        LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
        lam.setColor(color);
        item.setItemMeta(lam);
        return item;
    }

    /**
     * Sets the color.
     *
     * @param item item to color
     * @param color color
     * @return colored item
     */
    public static ItemStack setColor(ItemStack item, ArmorColor color) {
        if (!isApplicable(item))
            return null;
        LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
        lam.setColor(Color.fromRGB(color.getColor()));
        item.setItemMeta(lam);
        return item;
    }

    /**
     * Sets the color.
     *
     * @param item item to color
     * @param colorStr color
     * @return colored item
     */
    public static ItemStack setColor(ItemStack item, String colorStr) {
        if (!isApplicable(item))
            return null;
        int color = Integer.decode(colorStr);
        LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
        lam.setColor(Color.fromRGB(color));
        item.setItemMeta(lam);
        return item;
    }

    /**
     * Sets the color.
     *
     * @param item item to color
     * @param colorR amount of red
     * @param colorG amount of green
     * @param colorB amount of blue
     * @return colored item
     */
    public static ItemStack setColor(ItemStack item, int colorR, int colorG, int colorB) {
        if (!isApplicable(item))
            return null;
        Color c = Color.fromRGB(colorR, colorG, colorB);
        LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
        lam.setColor(c);
        item.setItemMeta(lam);
        return item;
    }

    /**
     * Gets the color.
     *
     * @param item colored item
     * @return color
     */
    public static int getColor(ItemStack item) {
        if (!isApplicable(item))
            return -1;
        LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
        int color = lam.getColor().asRGB();
        return color;
    }
}
