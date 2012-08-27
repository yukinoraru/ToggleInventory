package com.github.yukinoraru.ToggleInventory;

//import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class ToggleInventory extends JavaPlugin implements Listener {

    private static final String CONFIG_INVENTORY_CURRENT_INDEX          = "inv_current_index";
    private static final int    CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT  = 1;
    private static final int    CONFIG_INVENTORY_MAX_INDEX_DEFAULT      = 4;
    private static final int    CONFIG_INVENTORY_MAXIMUM                = 30;

    public void onEnable(){

        //
        getServer().getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        saveResource("special_inventories.yml", false);

        //
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
      if(event.isCancelled()){
        return;
      }
      // onCommand
    }

    public void onDisable(){
    }

    // maximum one precedes
    private int getMaxInventoryIndex(CommandSender sender){
        int max = -1;
        for(int i=2; i <= CONFIG_INVENTORY_MAXIMUM; i++){
            String permissionPath = "toggle_inventory." + Integer.toString(i);
            if(sender.hasPermission(permissionPath)){
                max = i;
            }
        }
        //getLogger().info("sender has toggle_inventory." + Integer.toString(max) + " permission.");
        return (max <= 1) ? CONFIG_INVENTORY_MAX_INDEX_DEFAULT : max;
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

    private int setNextInventoryIndex(CommandSender sender, int nextInvIndex) throws IndexOutOfBoundsException{
        String playerName = sender.getName();
        File configFile         = getPlayerConfigFile(playerName);
        FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);

        int invCurrentIndex = pConf.getInt(CONFIG_INVENTORY_CURRENT_INDEX, CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT);
        int maxIndex = getMaxInventoryIndex(sender);

        if(nextInvIndex < 0){
            nextInvIndex = (invCurrentIndex+1 > maxIndex) ? 1 : invCurrentIndex+1;
        }
        else if(nextInvIndex > maxIndex){
            //throw new IndexOutOfBoundsException("nextInvIndex must be less than maxIndex.");
            throw new IndexOutOfBoundsException("[ERROR] Max inventory index is " + ChatColor.RED+Integer.toString(maxIndex));
        }
        pConf.set(CONFIG_INVENTORY_CURRENT_INDEX, nextInvIndex);

        try {
            pConf.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nextInvIndex;
    }

    private String []getEnchantmentsString(ItemStack item){

        Map<Enchantment,Integer> enchantments = item.getEnchantments();
        Iterator<Entry<Enchantment,Integer>> iter = enchantments.entrySet().iterator();
        ArrayList<String> listOfEnchantment      = new ArrayList<String>();
        while(iter.hasNext()){
            Entry<Enchantment,Integer> entry = iter.next();
            String enchantmentName = entry.getKey().getName();
            int    echantmentLevel = entry.getValue();
            listOfEnchantment.add(enchantmentName + "," + echantmentLevel);
        }

        if(listOfEnchantment.size() > 0){
            String [] arrayOfEnchantment = listOfEnchantment.toArray(new String[listOfEnchantment.size()]);
            return arrayOfEnchantment;
        }

        return null;
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

            // get/set basic info for item
            int itemID = item.getTypeId();
            pInv.set(start + ".id", itemID);
            pInv.set(start + ".amount", item.getAmount());
            pInv.set(start + ".durability", item.getDurability());

            // enchantment
            String [] arrayOfEnchantment = getEnchantmentsString(item);
            if(arrayOfEnchantment != null){
                pInv.set(start + ".enchantment", Arrays.asList(arrayOfEnchantment));
            }

            //written book
            if(itemID == 387) {
                //getLogger().info("There is written book.");
                Book book = new Book(item);
                pInv.set(start + ".book" + ".title", book.getTitle());
                pInv.set(start + ".book" + ".author", book.getAuthor());
                pInv.set(start + ".book" + ".pages", Arrays.asList(book.getPages()));
            }
        }


        // save armor
        i = 0;
        for (ItemStack item : inventory.getArmorContents()) {
            i++;
            if(item == null){
                continue;
            }
            String start = "armor" + Integer.toString(i);

            // get/set basic info for item
            int itemID = item.getTypeId();
            pInv.set(start + ".id", itemID);
            pInv.set(start + ".durability", item.getDurability());

            // enchantment
            String [] arrayOfEnchantment = getEnchantmentsString(item);
            if(arrayOfEnchantment != null){
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

    private void deserializeInventoryConfig(PlayerInventory inventory, ConfigurationSection pInv){

        inventory.clear();

        int armorIndex = 0;
        ItemStack[] armorContents = new ItemStack[4];

        Set<String> item_keys = pInv.getKeys(false);
        for (String key: item_keys) {

            int index        = (key.startsWith("item")) ? Integer.parseInt(key.substring("item".length())) - 1 : Integer.parseInt(key.substring("armor".length())) - 1 ;
            int itemID       = pInv.getInt(key + ".id");
            int amount       = pInv.getInt(key + ".amount");
            short durability = (short)pInv.getInt(key + ".durability");

            ItemStack item = new ItemStack(itemID);
            item.setDurability(durability);

            List<String> enchantments = pInv.getStringList(key + ".enchantment");
            for (String e : enchantments){
                String[] tmp = e.split(",");
                if(tmp.length != 2){
                    getLogger().warning("enchantments is something wrong.");
                    continue;
                }
                Enchantment enchantment = Enchantment.getByName(tmp[0]);
                item.addUnsafeEnchantment(enchantment, Integer.parseInt(tmp[1]));
            }

            //written book
            if(itemID == 387) {
                //getLogger().info("Load from written book.");
                String author  = pInv.getString(key + ".book.author");
                String title   = pInv.getString(key + ".book.title");
                List<String> pages     = pInv.getStringList(key + ".book.pages");
                String[] pagesInString = pages.toArray(new String[pages.size()]);
                Book book = new Book(title, author, pagesInString);
                item = book.generateItemStack();
            }

            if(key.startsWith("item")){
                item.setAmount(amount);
                inventory.setItem(index, item);
            }
            else if(key.startsWith("armor")){
                armorContents[armorIndex++] = item;
            }
        }

        inventory.setArmorContents(armorContents);
        return ;
    }

    private void loadInventory(CommandSender sender, int inventoryIndex){
        Player player = (Player)sender;
        PlayerInventory inventory = player.getInventory();

        //
        File inventoryFile = getInventoryFile(player.getName(), inventoryIndex);
        FileConfiguration pInv  = YamlConfiguration.loadConfiguration(inventoryFile);

        deserializeInventoryConfig(inventory, pInv.getRoot());

        return ;
    }

    // [1 2 3 4]
    private String makeInventoryMessage(CommandSender sender){
        String playerName = sender.getName();
        String msg = ChatColor.GRAY+"[";
        File configFile         = getPlayerConfigFile(playerName);
        FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);

        int invCurrentIndex = pConf.getInt(CONFIG_INVENTORY_CURRENT_INDEX, CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT);
        int maxIndex = getMaxInventoryIndex(sender);

        for(int i=1; i <= maxIndex; i++){
            if(i == invCurrentIndex){
                msg += ChatColor.WHITE+Integer.toString(i);
            }
            else{
                msg += ChatColor.GRAY+Integer.toString(i);
            }
            msg += ChatColor.RESET+" ";
        }
        return msg + ChatColor.GRAY+"] ";
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("togglei")){
            if(!sender.hasPermission("toggle_inventory.toggle")){
                sender.sendMessage("You don't have permission to toggle inventory.");
                return true;
            }
            int index = -1;
            // toggle to same inventory is prohibit
            if(args.length >= 1 && args[0].length() > 0){
                try{
                    index = Integer.parseInt(args[0]);
                }
                catch(Exception e){
                    sender.sendMessage("Inventory index is wrong.");
                    return true;
                }
                int invCurrentIndex = getCurrentInventoryIndex(sender.getName());
                if(index == invCurrentIndex){
                    sender.sendMessage(makeInventoryMessage(sender) + "It's your current inventory index.");
                    return true;
                }
            }

            //
            File configFile         = getPlayerConfigFile(((Player)sender).getName());
            FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);

            boolean isSpecialInvEnabled = pConf.getString("special_inv_index", "").length() > 0;
            if(isSpecialInvEnabled){
                pConf.set("special_inv_index", "");
                try{
                    pConf.save(configFile);
                }catch (Exception e){

                }
                int inventoryIndex = getCurrentInventoryIndex(((Player)sender).getName());
                loadInventory(sender, inventoryIndex);
                sender.sendMessage(makeInventoryMessage(sender) + "inventory restored.");
                return true;
            }
            else{
                // save current inventory
                saveInventory(sender);

                int nextInvIndex;
                if(index > 0){
                    // set next inventory
                    try{
                        nextInvIndex = setNextInventoryIndex(sender, index);
                    }
                    catch(Exception e){
                        sender.sendMessage(e.getMessage()); // out of bounds exception
                        return true;
                    }
                }
                else{
                    // toggle inventory like a ring
                    nextInvIndex = setNextInventoryIndex(sender, -1);
                }

                // load from currentIndex
                loadInventory(sender, nextInvIndex);

                // TOOD: message to player
                sender.sendMessage(makeInventoryMessage(sender) + "inventory toggled.");
            }

            return true;
        }

        if(cmd.getName().equalsIgnoreCase("toggleis")){
            if(!sender.hasPermission("toggle_inventory.toggleis")){
                sender.sendMessage("You don't have permission to toggle special inventories.");
                return true;
            }

            this.reloadSpecialInventories();

            File configFile         = getPlayerConfigFile(((Player)sender).getName());
            FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);

            String currentSpecialInv = pConf.getString("special_inv_index", "");
            boolean isSpecialInvEnabled = currentSpecialInv.length() > 0;

            String targetInv = null;
            Set<String> nameList = specialInventoriesConfig.getKeys(false);

            if(args.length > 0){
                getLogger().info("args=" + args[0] + " , namelist=" + nameList.toString() + " , " + specialInventoriesConfig.toString());
            }

            // type /tis [query]
            if(args.length == 1 && args[0].length() > 0){
                int minDist = 0;
                boolean isFirst = true;
                for(String name : nameList){
                    int dist = LevenshteinDistance.computeLevenshteinDistance(name, args[0]);
                    if(name.startsWith(args[0])){
                        dist -= args[0].length() * 2;
                    }
                    if(isFirst){
                        targetInv = name;
                        minDist = dist;
                        isFirst = false;
                    }
                    else if(dist < minDist){
                        targetInv = name;
                        minDist = dist;
                    }
                    getLogger().info("A=" + name + " , B=" + args[0] + " , Distance=" + Integer.toString(dist));
                }
            }
            //*
            // just type /tis
            else{
                String[] nameListString = nameList.toArray(new String[0]);
                if(isSpecialInvEnabled){
                    int targetIndex = -1;
                    for(int i=0; i < nameListString.length; i++){
                        String name = nameListString[i];
                        if(name.equals(currentSpecialInv)){
                            targetIndex = i+1;
                        }
                    }
                    if(targetIndex >= nameListString.length){
                        targetIndex = 0;
                    }
                    targetInv = nameListString[targetIndex];
                }
                else{
                    if(nameListString.length > 0){
                        targetInv = nameListString[0];
                    }
                }
            }
            //*/

            //load inventory
            if(targetInv != null){

                //
                String msg = ChatColor.GRAY+"[";
                for(String name: nameList){
                    if(name.equals(targetInv)){
                        msg += ChatColor.GREEN+name;
                    }
                    else{
                        msg += ChatColor.DARK_GREEN+name;
                    }
                    msg += ChatColor.RESET+" ";
                }
                sender.sendMessage(msg + ChatColor.GRAY + "]  is toggled.");

                //
                try{
                    if(!isSpecialInvEnabled){
                        saveInventory(sender);
                    }
                    pConf.set("special_inv_index", targetInv);
                    pConf.save(configFile);
                }
                catch (Exception e){

                }

                deserializeInventoryConfig(
                        ((Player)sender).getInventory(),
                        specialInventoriesConfig.getConfigurationSection(targetInv)
                        );
                return true;
            }
            else{
                sender.sendMessage("There's no matched inventory.");
                return true;
            }

        }

        return false;
    }

    private FileConfiguration specialInventoriesConfig = null;
    private File specialInventoriesConfigFile = null;

    public void reloadSpecialInventories() {
        if (specialInventoriesConfigFile == null) {
            specialInventoriesConfigFile = new File(getDataFolder(), "special_inventories.yml");
        }
        specialInventoriesConfig = YamlConfiguration.loadConfiguration(specialInventoriesConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource("special_inventories.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            specialInventoriesConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getSpecialInventoriesConfig() {
        if (specialInventoriesConfig == null) {
            this.reloadSpecialInventories();
        }
        return specialInventoriesConfig;
    }

}

class LevenshteinDistance {
    private static int minimum(int a, int b, int c) {
            return Math.min(Math.min(a, b), c);
    }

    public static int computeLevenshteinDistance(CharSequence str1,
                    CharSequence str2) {
            int[][] distance = new int[str1.length() + 1][str2.length() + 1];

            for (int i = 0; i <= str1.length(); i++)
                    distance[i][0] = i;
            for (int j = 1; j <= str2.length(); j++)
                    distance[0][j] = j;

            for (int i = 1; i <= str1.length(); i++)
                    for (int j = 1; j <= str2.length(); j++)
                            distance[i][j] = minimum(
                                            distance[i - 1][j] + 1,
                                            distance[i][j - 1] + 1,
                                            distance[i - 1][j - 1]
                                                            + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                                                            : 1));

            return distance[str1.length()][str2.length()];
    }
}