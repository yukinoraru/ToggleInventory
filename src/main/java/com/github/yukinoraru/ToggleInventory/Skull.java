package com.github.yukinoraru.ToggleInventory;

import net.minecraft.server.NBTTagCompound;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Class, that allows setting and getting skin fo skull.
 * Based on PrettyScaryLib.Skull
 */
public class Skull {

    /**
     * Sets skin.
     *
     * @param item item
     * @param nick nick
     * @return item stack
     */
    public static ItemStack setSkin(ItemStack item, String nick) {
        if (!isApplicable(item))
            return null;
        CraftItemStack craftStack = null;
        net.minecraft.server.ItemStack itemStack = null;
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            itemStack = craftStack.getHandle();
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            itemStack = craftStack.getHandle();
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setString("SkullOwner", nick);
        itemStack.tag = tag;
        return craftStack;
    }

    /**
     * Gets skin.
     *
     * @param item item
     * @return owner name
     */
    public static String getSkin(ItemStack item) {
        if (!isApplicable(item))
            return null;
        CraftItemStack craftStack = null;
        net.minecraft.server.ItemStack itemStack = null;
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            itemStack = craftStack.getHandle();
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            itemStack = craftStack.getHandle();
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
            return null;
        }
        return tag.getString("SkullOwner");
    }

    /**
     * Checks if is applicable.
     *
     * @param item item
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
