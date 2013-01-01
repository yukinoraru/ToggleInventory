package com.github.yukinoraru.ToggleInventory;

import java.io.File;
import java.io.PrintWriter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ConfigManager {

	private ToggleInventory plugin;

	public ConfigManager(ToggleInventory plugin){
		this.plugin = plugin;
	}

    private File getInventoryFile(String playerName){
        String parentPath = plugin.getDataFolder() + File.separator + "players";
        String childPath  = playerName + ".yml";
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

	public void saveInventory(String playerName, PlayerInventory inventory) throws Exception{

        File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

		beforeSave(file);

		//
        Inventory inventoryArmor = ItemSerialization.getArmorInventory(inventory);

        String serializedInventoryNormal = ItemSerialization.toBase64(inventory);
        String serializedInventoryArmor  = ItemSerialization.toBase64(inventoryArmor);

        fileConfiguration.set("inventory_normal", serializedInventoryNormal);
        fileConfiguration.set("inventory_armor", serializedInventoryArmor);

        fileConfiguration.save(file);
        return ;
	}

	public void toggleInventory(){

	}

	private void loadInventory(String playerName, PlayerInventory inventory){

		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

        String serializedInventoryNormal = fileConfiguration.getString("inventory_normal");
        String serializedInventoryArmor  = fileConfiguration.getString("inventory_armor");

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

/*
    private static final String CONFIG_INVENTORY_CURRENT_INDEX          = "inv_current_index";
    private static final String CONFIG_INVENTORY_SPCIAL_INV_INDEX       = "special_inv_index";
    private static final int    CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT  = 1;
    private static final int    CONFIG_INVENTORY_MAX_INDEX_DEFAULT      = 4;
    private static final int    CONFIG_INVENTORY_MAXIMUM                = 30;
    private static final String CONFIG_FILENAME_SPECIAL_INV             = "special_inventories.yml";


    // maximum one precedes
    private int getMaxInventoryIndex(CommandSender sender){
        int max = -1;
        for(int i=2; i <= CONFIG_INVENTORY_MAXIMUM; i++){
            String permissionPath = "toggle_inventory." + Integer.toString(i);
            if(sender.hasPermission(permissionPath)){
                max = i;
            }
        }
        return (max <= 1) ? CONFIG_INVENTORY_MAX_INDEX_DEFAULT : max;
    }

    // create File instance for saving inventory
    // path = plugins/ToggleInventory/players/[player-name]/inv[n].yml
    private File getInventoryFile(String playerName, int num){
        String parentPath = getDataFolder() + File.separator + "players" + File.separator + playerName;
        String childPath  = "inv" + num + ".yml";
        return new File(parentPath, childPath);
    }

    // create File instance for player's config
    // path = plugins/ToggleInventory/players/[player-name]/config.yml
    private File getPlayerConfigFile(String playerName){
        String parentPath = getDataFolder() + File.separator + "players" + File.separator + playerName;
        String childPath  = "config.yml";
        return new File(parentPath, childPath);
    }

    //
    private int getCurrentInventoryIndex(String playerName){
        File configFile         = getPlayerConfigFile(playerName);
        FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);
        int invCurrentIndex     = pConf.getInt(CONFIG_INVENTORY_CURRENT_INDEX, CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT);
        return invCurrentIndex;
    }

    // calculate and set next inventory index
    // inventory index must be loop like a ring: 1 -> 2 -> 3 -> 1 -> .....
    private int setNextInventoryIndex(CommandSender sender, int nextInvIndex) throws IndexOutOfBoundsException{
        String playerName = sender.getName();

        int maxIndex = getMaxInventoryIndex(sender);
        int invCurrentIndex = getCurrentInventoryIndex(playerName);

        if(nextInvIndex < 0){
            nextInvIndex = (invCurrentIndex+1 > maxIndex) ? 1 : invCurrentIndex+1;
        }
        else if(nextInvIndex > maxIndex){
            throw new IndexOutOfBoundsException("[ERROR] Max inventory index is " + ChatColor.RED+Integer.toString(maxIndex));
        }

        // save next index
        File configFile         = getPlayerConfigFile(playerName);
        FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);

        try {
            pConf.set(CONFIG_INVENTORY_CURRENT_INDEX, nextInvIndex);
            pConf.save(configFile);
        } catch (IOException e) {
            outputError("Something went wrong when setting next inventory index.", sender);
            e.printStackTrace();
        }

        return nextInvIndex;
    }
*/


}
