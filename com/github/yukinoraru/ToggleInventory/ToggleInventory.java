package com.github.yukinoraru.ToggleInventory;

//import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
 
public class ToggleInventory extends JavaPlugin {
 
	private static final String CONFIG_INVENTORY_CURRENT_INDEX          = "inv_current_index";
	private static final int    CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT  = 1;
	private static final String CONFIG_INVENTORY_MAX_INDEX              = "max_index";
	private static final int    CONFIG_INVENTORY_MAX_INDEX_DEFAULT      = 4;	
	
	public void onEnable(){
		getLogger().info("Your plugin has been enabled!");
	}
 
	public void onDisable(){
		getLogger().info("Your plugin has been disabled.");
	}
	
	// load from yml
	private File getInventoryFile(String playerName, int num){
		String parentPath = getDataFolder() + File.separator + "players" + File.separator + playerName;
		String childPath  = "inv" + num + ".yml";		
    	return new File(parentPath, childPath);
	}
	
	private File getPlayerConfigFile(String playerName){
		String parentPath = getDataFolder() + File.separator + "players" + File.separator + playerName;
		String childPath  = "config.yml";		
    	return new File(parentPath, childPath);
	}

	private int getCurrentInventoryIndex(String playerName){
        File configFile         = getPlayerConfigFile(playerName);
        FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);
        int invCurrentIndex     = pConf.getInt(CONFIG_INVENTORY_CURRENT_INDEX, CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT);		
        return invCurrentIndex;
	}
	
	private int setNextInventoryIndex(String playerName, int nextInvIndex){
        File configFile         = getPlayerConfigFile(playerName);
        FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);
        
        if(nextInvIndex < 0){
            int invCurrentIndex = pConf.getInt(CONFIG_INVENTORY_CURRENT_INDEX, CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT);		
            int maxIndex = pConf.getInt(CONFIG_INVENTORY_MAX_INDEX, CONFIG_INVENTORY_MAX_INDEX_DEFAULT);
            nextInvIndex = (invCurrentIndex+1 > maxIndex) ? 1 : invCurrentIndex+1;
        }
        pConf.set(CONFIG_INVENTORY_CURRENT_INDEX, nextInvIndex);
       
        try {
			pConf.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return nextInvIndex;		
	}
	
	private void saveInventory(CommandSender sender){

    	Player player = (Player)sender;
        PlayerInventory inventory = player.getInventory();

        // load current inventory index
        int invCurrentIndex     = getCurrentInventoryIndex(player.getName());

        // load saved currentInventory 
        File inventoryFile      = getInventoryFile(player.getName(), invCurrentIndex);
        inventoryFile.delete(); // before save, file must be filled with empty
                                // TODO: deleting file is not suitable solution
        FileConfiguration pInv  = YamlConfiguration.loadConfiguration(inventoryFile);
                
        int i = 0;
        for (ItemStack item : inventory) {
        	i++;
        	if(item == null){
        		continue;
        	}

        	String start = "item" + Integer.toString(i);

        	getLogger().info("start =" + start);

            // get/set basic info for item
            pInv.set(start + ".id", item.getTypeId());
            pInv.set(start + ".amount", item.getAmount());
            pInv.set(start + ".durability", item.getDurability());
            	            
            // enchantment
            Map<Enchantment,Integer> enchantments = item.getEnchantments();
            Iterator<Entry<Enchantment,Integer>> iter = enchantments.entrySet().iterator();
            ArrayList<String> listOfEnchantment      = new ArrayList<String>();
            while(iter.hasNext()){
                Entry<Enchantment,Integer> entry = iter.next();
                String enchantmentName = entry.getKey().getName();
                int    echantmentLevel = entry.getValue();
                listOfEnchantment.add(enchantmentName + "," + echantmentLevel);
                //listOfEnchantment.add("raw = " + entry.toString() + " , getID = " + entry.getKey().getId() + " , getName = " + entry.getKey().getName());
            }
            if(listOfEnchantment.size() > 0){
            	String [] arrayOfEnchantment = listOfEnchantment.toArray(new String[listOfEnchantment.size()]);
            	pInv.set(start + ".enchantment", Arrays.asList(arrayOfEnchantment));
            }
        }
        
        try{
        	pInv.save(inventoryFile);
        }
        catch(Exception e){
        	getLogger().warning("couldn't save items!");	        	
        }		
		return;
	}
	
	private void loadInventory(CommandSender sender, int inventoryIndex){
    	Player player = (Player)sender;
        PlayerInventory inventory = player.getInventory();
        
        inventory.clear();
        
        // 
        File inventoryFile = getInventoryFile(player.getName(), inventoryIndex);
        FileConfiguration pInv  = YamlConfiguration.loadConfiguration(inventoryFile);
        
    	getLogger().info("load from:" + inventoryFile.getName() + " , keys =");
        Set<String> item_keys = pInv.getKeys(false);
        for (String key: item_keys) {
        	getLogger().info(key);
        	
        	int index        = Integer.parseInt(key.substring(4)) - 1;
        	int id           = pInv.getInt(key + ".id");
        	int amount       = pInv.getInt(key + ".amount");
        	short durability = (short)pInv.getInt(key + ".durability");

        	ItemStack item = new ItemStack(id);
        	item.setAmount(amount);
        	item.setDurability(durability);
        	
            List<String> enchantments = pInv.getStringList(key + ".enchantment");
            for (String e : enchantments){
            	String[] tmp = e.split(",");
            	if(tmp.length != 2){
            		getLogger().warning("enchantments is something wrong.");
            		continue;
            	}
            	Enchantment enchantment = Enchantment.getByName(tmp[0]);
            	item.addEnchantment(enchantment, Integer.parseInt(tmp[1]));            	
            }        	
        	inventory.setItem(index, item);
        }
        
		return ;
	}
		
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	    if(cmd.getName().equalsIgnoreCase("togglei")){	    	
	    	int index = -1;	    	
	    	// toggle to same inventory is prohibit
	    	if(args.length >= 1 && args[0].length() > 0){
	    		index = Integer.parseInt(args[0]);
	            int invCurrentIndex = getCurrentInventoryIndex(sender.getName());
	    		if(index == invCurrentIndex){
		    		// TODO: add index number to show
	            	sender.sendMessage("It's your current inventory index.");
	    			return true;
	    		}
	    	}
	    	
	    	// save current inventory
	    	saveInventory(sender);
	    	
    		int nextInvIndex;
	    	if(index > 0){
		    	// set next inventory
	    		// TODO: check index range
	    		nextInvIndex = setNextInventoryIndex(sender.getName(), index);	    		
	    	}
	    	else{
		    	// toggle inventory circularity
	    		nextInvIndex = setNextInventoryIndex(sender.getName(), -1);	    		
	    	}
	    	
	    	// load from currentIndex
	    	loadInventory(sender, nextInvIndex);

	    	// TOOD: message to player
	    	/*
        	player.sendMessage("current inventory = " + invNextIndex);
	        //*/

	        return true;
	    }
	    return false;
	}	
}