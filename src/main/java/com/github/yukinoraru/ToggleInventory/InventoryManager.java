package com.github.yukinoraru.ToggleInventory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.entity.Player;

public class InventoryManager {

	private static final int CONFIG_INVENTORY_MAX_INDEX_DEFAULT = 4;
	private static final int CONFIG_INVENTORY_MAXIMUM = 30;

	private ToggleInventory plugin;

	public InventoryManager(ToggleInventory plugin){
		this.plugin = plugin;
	}

    public File getInventoryFile(String playerName){
        String parentPath = plugin.getDataFolder() + File.separator + "players";
        String childPath  = playerName + ".yml";
        File f = new File(parentPath);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
        return new File(parentPath, childPath);
    }

	private void beforeSave(File file) throws Exception{
        // before save, file must be filled with empty
        PrintWriter writer;
        try {
        	// file doesn't exist, create new file
            if(!file.exists()){
            	file.createNewFile();
            }
            writer = new PrintWriter(file);
            writer.print("");
            writer.close();
        } catch (Exception e) {
        	throw e;
        }
	}

	private int getMaxInventoryIndex(CommandSender player) {
		int max = -1;
		for (int i = 2; i <= CONFIG_INVENTORY_MAXIMUM; i++) {
			String permissionPath = String.format("toggle_inventory.%d", i);
			//plugin.getLogger().warning(permissionPath + ":" + player.hasPermission(permissionPath));
			if (player.hasPermission(permissionPath)) {
				max = i;
			}
		}
		return (max <= 1) ? CONFIG_INVENTORY_MAX_INDEX_DEFAULT : max;
	}

	// calculate and set next inventory index
	// inventory index must be loop like a ring: 1 -> 2 -> 3 -> 1 -> .....
	private int calcNextInventoryIndex(int maxIndex, int currentIndex, boolean rotateDirection){
		int nextIndex;
		if(rotateDirection){
			nextIndex = ((currentIndex + 1) > maxIndex) ? 1 : currentIndex + 1;
		}
		else{
			nextIndex = ((currentIndex - 1) <= 0) ? maxIndex : currentIndex - 1;
		}
		return nextIndex;
	}

	private void setCurrentInventoryIndex(String playerName, int index) throws IOException{
		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.set("current", index);
        fileConfiguration.save(file);
	}

	private int getCurrentInventoryIndex(String playerName){
		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        return fileConfiguration.getInt("current", 1);
	}

	public void saveInventory(String playerName, PlayerInventory inventory, int index) throws Exception{

        File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

		beforeSave(file);

        Inventory inventoryArmor = ItemSerialization.getArmorInventory(inventory);

        String serializedInventoryNormal = ItemSerialization.toBase64(inventory);
        String serializedInventoryArmor  = ItemSerialization.toBase64(inventoryArmor);

        fileConfiguration.set(String.format("inv%d_normal", index), serializedInventoryNormal);
        fileConfiguration.set(String.format("inv%d_armor", index), serializedInventoryArmor);

        fileConfiguration.save(file);
        return ;
	}

	// this method generates string like '[1 2 3 4 ... n]'
	public String makeInventoryMessage(CommandSender player) {
		String msg = ChatColor.GRAY + "[";
		String playerName = player.getName();
		int invCurrentIndex = getCurrentInventoryIndex(playerName);
		int maxIndex = getMaxInventoryIndex(player);

		for (int i = 1; i <= maxIndex; i++) {
			if (i == invCurrentIndex) {
				msg += ChatColor.WHITE + Integer.toString(i);
			} else {
				msg += ChatColor.GRAY + Integer.toString(i);
			}
			msg += ChatColor.RESET + " ";
		}
		return msg + ChatColor.GRAY + "] ";
	}

	public void toggleInventory(CommandSender player, boolean rotateDirection) throws Exception{

		// 1. get info
		String playerName = player.getName();
		PlayerInventory inventory = ((Player) player).getInventory();
		int maxIndex = getMaxInventoryIndex(player);
		int currentIndex = getCurrentInventoryIndex(playerName);
		int nextIndex = calcNextInventoryIndex(maxIndex, currentIndex, rotateDirection);

		// 2. save and load inv
		saveInventory(playerName, inventory, currentIndex);
		loadInventory(playerName, inventory, nextIndex);

		// 3. set next inv
		setCurrentInventoryIndex(playerName, nextIndex);
	}

	private void loadInventory(String playerName, PlayerInventory inventory, int index){

		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

        String serializedInventoryNormal = fileConfiguration.getString(String.format("inv%d_normal", index));
        String serializedInventoryArmor  = fileConfiguration.getString(String.format("inv%d_armor", index));

		inventory.clear();
		inventory.setArmorContents(null);

        if(serializedInventoryNormal != null){
        	Inventory deserializedInventoryNormal = ItemSerialization.fromBase64(serializedInventoryNormal);

        	int i=0;
			for (ItemStack item : deserializedInventoryNormal.getContents()) {
				i++;
				if (item == null) {
					continue;
				}
        		inventory.setItem(i-1, item);
			}
        }

        if(serializedInventoryArmor != null){
            Inventory deserialized_inv_armor = ItemSerialization.fromBase64(serializedInventoryArmor);
            ItemStack []tmp = deserialized_inv_armor.getContents();
        	if(tmp != null){
        		inventory.setArmorContents(tmp);
        	}
        }
		return;
	}
}
