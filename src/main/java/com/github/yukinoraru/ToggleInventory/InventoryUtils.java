package com.github.yukinoraru.ToggleInventory;

// thanks NathanWolf

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.minecraft.server.v1_7_R3.NBTBase;
import net.minecraft.server.v1_7_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.minecraft.server.v1_7_R3.NBTTagList;

@SuppressWarnings("unused")
public class InventoryUtils
{
	private static String versionPrefix = "";

	private static Class<?> class_ItemStack;
	private static Class<?> class_NBTBase;
	private static Class<?> class_NBTTagCompound;
	private static Class<?> class_NBTTagList;
	private static Class<?> class_CraftInventoryCustom;
	private static Class<?> class_CraftItemStack;

	static
	{
		// Find classes Bukkit hides from us. :-D
		// Much thanks to @DPOHVAR for sharing the PowerNBT code that powers the reflection approach.
		try {
			String className = Bukkit.getServer().getClass().getName();
			String[] packages = className.split("\\.");
			if (packages.length == 5) {
				versionPrefix = packages[3] + ".";
			}

			class_ItemStack = fixBukkitClass("net.minecraft.server.ItemStack");
			class_NBTBase = fixBukkitClass("net.minecraft.server.NBTBase");
			fixBukkitClass("net.minecraft.server.NBTCompressedStreamTools");
			class_NBTTagCompound = fixBukkitClass("net.minecraft.server.NBTTagCompound");
			class_NBTTagList = fixBukkitClass("net.minecraft.server.NBTTagList");
			class_CraftInventoryCustom = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftInventoryCustom");
			class_CraftItemStack = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftItemStack");
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private static Class<?> fixBukkitClass(String className) {
		className = className.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + versionPrefix);
		className = className.replace("net.minecraft.server.", "net.minecraft.server." + versionPrefix);
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static Object getNMSCopy(ItemStack stack) {
    	Object nms = null;
    	try {
			Method copyMethod = class_CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
			nms = copyMethod.invoke(null, stack);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return nms;
    }

	protected static Object getHandle(ItemStack stack) {
		Object handle = null;
		try {
			Field handleField = stack.getClass().getDeclaredField("handle");
			handleField.setAccessible(true);
			handle = handleField.get(stack);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return handle;
	}

	protected static Object getTag(Object mcItemStack) {
		Object tag = null;
		try {
			Field tagField = class_ItemStack.getField("tag");
			tag = tagField.get(mcItemStack);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return tag;
	}

	public static ItemStack getCopy(ItemStack stack) {
		if (stack == null) return null;

        try {
                Object craft = getNMSCopy(stack);
                Method mirrorMethod = class_CraftItemStack.getMethod("asCraftMirror", craft.getClass());
                stack = (ItemStack)mirrorMethod.invoke(null, craft);
        } catch (Throwable ex) {
                ex.printStackTrace();
        }

        return stack;
	}

	public static String getMeta(ItemStack stack, String tag, String defaultValue) {
		String result = getMeta(stack, tag);
		return result == null ? defaultValue : result;
	}

	public static String getMeta(ItemStack stack, String tag) {
		if (stack == null) return null;
		String meta = null;
		try {
			Object craft = getHandle(stack);
			if (craft == null) return null;
			Object tagObject = getTag(craft);
			if (tagObject == null) return null;
			Method getStringMethod = class_NBTTagCompound.getMethod("getString", String.class);
			meta = (String)getStringMethod.invoke(tagObject, tag);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return meta;
	}

	public static void setMeta(ItemStack stack, String tag, String value) {
		if (stack == null) return;
		try {
			Object craft = getHandle(stack);
			Object tagObject = getTag(craft);
			Method setStringMethod = class_NBTTagCompound.getMethod("setString", String.class, String.class);
			setStringMethod.invoke(tagObject, tag, value);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static void addGlow(ItemStack stack) {
		if (stack == null) return;

		try {
			Object craft = getHandle(stack);
			Object tagObject = getTag(craft);
			final Object enchList = class_NBTTagList.newInstance();
			Method setMethod = class_NBTTagCompound.getMethod("set", String.class, class_NBTBase);
			setMethod.invoke(tagObject, "ench", enchList);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
    }

	public static String inventoryToString(final Inventory inventory) {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final OutputStream dataOutput = new DataOutputStream(outputStream);
		try {
			final NBTTagList itemList = new NBTTagList();
			for (int i = 0; i < inventory.getSize(); i++) {
				final NBTTagCompound outputObject = new NBTTagCompound();
				Object craft = null;
				final CraftItemStack is = (CraftItemStack) inventory.getItem(i);
				if (is != null) {
					craft = getNMSCopy(is);
				} else {
					craft = null;
				}
				if (craft != null && class_ItemStack.isInstance(craft)) {
					outputObject.setByte("Slot", (byte) i);
					CraftItemStack.asNMSCopy(is).save(outputObject);
					itemList.add(outputObject);
				}
			}

			// This bit is kind of ugly and prone to break between versions
			// Well, moreso than the rest of this, even.
			NBTTagCompound tag = new NBTTagCompound();
			tag.set("Items", itemList);
			NBTCompressedStreamTools.a(tag, dataOutput);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}

		return new BigInteger(1, outputStream.toByteArray()).toString(32);
	}

	public static Inventory stringToInventory(final String data)  throws Exception {
		Inventory inventory = null;

		try {
			final ByteArrayInputStream dataInput = new ByteArrayInputStream(
					new BigInteger(data, 32).toByteArray());
			final InputStream inputStream = new DataInputStream(dataInput);

			// More MC internals :(
			final NBTTagCompound tagCompound = NBTCompressedStreamTools
					.a(inputStream);

			NBTTagList itemList = tagCompound.getList("Items", 10);

			//-----------------------------------------------------------------
			// create inventory
			//-----------------------------------------------------------------
			// TODO: fix this monkey patch!
			// get max slot number = max of inventory
			int maxSlot = 0;
			for (int i = 0; i < itemList.size(); i++) {
				int tmp = itemList.get(i).getByte("Slot") & 0xFF;
				if(maxSlot < tmp){
					maxSlot = tmp;
				}
			}
			inventory = createInventory(null, maxSlot+1);

			for (int i = 0; i < itemList.size(); i++) {
				NBTTagCompound tmpTagCompound = itemList.get(i);
				int slot = tmpTagCompound.getByte("Slot") & 0xFF;
				//System.out.println("slot="+slot+" ,inventory.getsize="+inventory.getSize());

				if(slot >= 0 && slot < inventory.getSize()){
					net.minecraft.server.v1_7_R3.ItemStack itemStack = net.minecraft.server.v1_7_R3.ItemStack
							.createStack(tmpTagCompound);

					CraftItemStack craftItemStack = CraftItemStack.asCraftMirror(itemStack);
					inventory.setItem(slot, craftItemStack);
				}
			}
			//-----------------------------------------------------------------

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(
					"Sorry, your inventory isn't compatible with this version. Clear and create new one.");
		}

		return inventory;
	}

	public static Inventory createInventory(InventoryHolder holder, final int size) {
		Inventory inventory = null;
		try {
			Constructor<?> inventoryConstructor = class_CraftInventoryCustom.getConstructor(InventoryHolder.class, Integer.TYPE);
			inventory = (Inventory)inventoryConstructor.newInstance(holder, size);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return inventory;
	}

	public static boolean inventorySetItem(Inventory inventory, int index, ItemStack item) {
		try {
			Method setItemMethod = class_CraftInventoryCustom.getMethod("setItem", Integer.TYPE, ItemStack.class);
			setItemMethod.invoke(inventory, index, item);
			return true;
		} catch(Throwable ex) {
			ex.printStackTrace();
		}
		return false;
	}

	// ---------------------------------

	//
    public static Inventory getArmorInventory(PlayerInventory playerInventory) {
        ItemStack[] armor = playerInventory.getArmorContents();
        Inventory inventory = createInventory(null, armor.length);
        for (int i = 0; i < armor.length; i++){
        	inventory.setItem(i, armor[i]);
        }
        return inventory;
    }

}