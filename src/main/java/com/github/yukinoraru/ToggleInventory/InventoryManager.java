package com.github.yukinoraru.ToggleInventory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

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
	public static final String CONFIG_FILENAME_SPECIAL_INV = "special_inventories.yml";

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

    public File getSpecialInventoryFile(){
    	return new File(plugin.getDataFolder(), CONFIG_FILENAME_SPECIAL_INV);
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
	public int calcNextInventoryIndex(int maxIndex, int currentIndex, boolean rotateDirection){
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

	private void setCurrentSpecialInventoryIndex(String playerName, String name) throws IOException{
		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.set("sp_current", name);
        fileConfiguration.save(file);
	}

	public void setSpecialInventoryUsingStatus(String playerName, boolean isUsing) throws IOException{
		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.set("sp_using", isUsing);
        fileConfiguration.save(file);
	}

	public boolean getSpecialInventoryUsingStatus(String playerName){
		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        return fileConfiguration.getBoolean("sp_using");
	}


	private String getCurrentSpecialInventoryIndex(String playerName){
		File file = getInventoryFile(playerName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        return (String) fileConfiguration.get("sp_current", "");
	}

	// this method generates string like '[1 2 3 4 ... n]'
	public String makeInventoryMessage(CommandSender player) {
		String msg = ChatColor.GRAY + "[";
		String playerName = player.getName();
		int invCurrentIndex = getCurrentInventoryIndex(playerName);
		int maxIndex = getMaxInventoryIndex(player);

		for (int i = 1; i <= maxIndex; i++) {
			msg += ( (i == invCurrentIndex) ? ChatColor.WHITE : ChatColor.GRAY ) + Integer.toString(i);
			msg += ChatColor.RESET + " ";
		}
		return msg + ChatColor.GRAY + "] ";
	}

	// this method generates string like '[1 2 3 4 ... n]'
	public String makeSpecialInventoryMessage(CommandSender player) throws Exception {
		String msg = ChatColor.GRAY + "[";

		String playerName = player.getName();
		String invCurrentName = getCurrentSpecialInventoryIndex(playerName);
		String []list = getListSpecialInventory(getSpecialInventoryFile());

		for (int i = 0; i < list.length; i++) {
			msg += ((list[i].equals(invCurrentName)) ? ChatColor.GREEN : ChatColor.DARK_GREEN) + list[i];
			msg += ChatColor.RESET + " ";
		}
		return msg + ChatColor.GRAY + "] ";
	}

	public void toggleInventory(CommandSender player, int index) throws Exception{

		// 1. get info
		String playerName = player.getName();
		PlayerInventory inventory = ((Player) player).getInventory();
		int maxIndex = getMaxInventoryIndex(player);
		int currentIndex = getCurrentInventoryIndex(playerName);
		int nextIndex = index;

		// 2. index validating
		if (nextIndex > maxIndex) {
			throw new IndexOutOfBoundsException(String.format(
					"Max inventory index is %d", maxIndex));
		}else if(nextIndex <= 0){
			throw new Exception("Inventory index is wrong.");
		}else if(currentIndex == nextIndex){
			throw new Exception("It's current inventory.");
		}

		// 3. save and load inv
		saveInventory(playerName, inventory, currentIndex);
		loadInventory(playerName, inventory, nextIndex);

		// 4. set next inv
		setCurrentInventoryIndex(playerName, nextIndex);
	}

	public void toggleSpecialInventory(CommandSender player, String inventoryName) throws Exception{
		// 1. get current inv index and save inventory
		String playerName = player.getName();
		PlayerInventory inventory = ((Player) player).getInventory();
		if (!getSpecialInventoryUsingStatus(playerName)) {
			int currentIndex = getCurrentInventoryIndex(playerName);
			saveInventory(playerName, inventory, currentIndex);
		}

		// 2. matching inv with inventoryName
		String []list = getListSpecialInventory(getSpecialInventoryFile());
		int index = LevenshteinDistance.find(list, inventoryName);
		String specialInvName = list[index];

		// 3. load inventory
		loadSpecialInventory(playerName, inventory, specialInvName);

		// 4. set SpInv index
		setCurrentSpecialInventoryIndex(playerName, specialInvName);

		// 5. update SpInv Using Status
		setSpecialInventoryUsingStatus(playerName, true);
	}

	private String[] getListSpecialInventory(File specialInventoryFile) throws Exception{
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(specialInventoryFile);
		Set<String> nameList = fileConfiguration.getKeys(false);
		String []tmp = nameList.toArray(new String[0]);
		if(tmp.length == 0){
			throw new Exception("There are no special inventories in "
					+ CONFIG_FILENAME_SPECIAL_INV
					+ ". Please check it.");
		}
		return tmp;
	}

	public String getNextSpecialInventory(String []list, String name, boolean rotateDirection) throws Exception{
		String nextInvName = null;
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(name)) {
				if (rotateDirection) {
					if (i + 1 < list.length) {
						nextInvName = list[i + 1];
					} else {
						nextInvName = list[0];
					}
				} else {
					if (i - 1 >= 0) {
						nextInvName = list[i - 1];
					} else {
						nextInvName = list[list.length-1];
					}
				}
				break;
			}
		}
		return (nextInvName == null) ? list[0] : nextInvName;
	}

	public void toggleSpecialInventory(CommandSender player, boolean rotateDirection) throws Exception{
		String playerName = player.getName();
		String []list = getListSpecialInventory(getSpecialInventoryFile());
		String currentSpIndex = getCurrentSpecialInventoryIndex(playerName);
		String nextSpIndex = (getSpecialInventoryUsingStatus(playerName)) ? getNextSpecialInventory(
				list, currentSpIndex, rotateDirection) : currentSpIndex;
		toggleSpecialInventory(player, nextSpIndex);
	}

	public void toggleInventory(CommandSender player, boolean rotateDirection) throws Exception{
		String playerName = player.getName();

		int maxIndex = getMaxInventoryIndex(player);
		int currentIndex = getCurrentInventoryIndex(playerName);
		int nextIndex = calcNextInventoryIndex(maxIndex, currentIndex, rotateDirection);

		toggleInventory(player, nextIndex);
	}

	public void saveInventory(PlayerInventory inventory, File inventoryFile, String sectionPathContents, String sectionPathArmor) throws Exception{

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(inventoryFile);

        Inventory inventoryArmor = ItemSerialization.getArmorInventory(inventory);

        String serializedInventoryContents = ItemSerialization.toBase64(inventory);
        String serializedInventoryArmor  = ItemSerialization.toBase64(inventoryArmor);

        fileConfiguration.set(sectionPathContents, serializedInventoryContents);
        fileConfiguration.set(sectionPathArmor, serializedInventoryArmor);

		beforeSave(inventoryFile);
        fileConfiguration.save(inventoryFile);
        return ;
	}

	public void saveInventory(String playerName, PlayerInventory inventory, int index) throws Exception{
        File inventoryFile = getInventoryFile(playerName);
        String sectionPathContents = String.format("inv%d.contents", index);
        String sectionPathArmor = String.format("inv%d.armor", index);
        saveInventory(inventory, inventoryFile, sectionPathContents, sectionPathArmor);
        return ;
	}

	private void loadInventory(String playerName, PlayerInventory inventory, File inventoryFile, String sectionPathContents, String sectionPathArmor){
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(inventoryFile);

        String serializedInventoryContents = fileConfiguration.getString(sectionPathContents);
        String serializedInventoryArmor  = fileConfiguration.getString(sectionPathArmor);

		inventory.clear();
		inventory.setArmorContents(null);

        if(serializedInventoryContents != null){
        	Inventory deserializedInventoryNormal = ItemSerialization.fromBase64(serializedInventoryContents);

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

	public void restoreInventory(CommandSender player) {
		String playerName = player.getName();
		PlayerInventory inventory = ((Player) player).getInventory();
		int index = getCurrentInventoryIndex(playerName);
		String sectionPathContents = String.format("inv%d.contents", index);
		String sectionPathArmor = String.format("inv%d.armor", index);
		loadInventory(playerName, inventory, getInventoryFile(playerName), sectionPathContents, sectionPathArmor);
	}

	public void deleteSpecialInventory(String name) throws IOException{
		File file = getSpecialInventoryFile();
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
		fileConfiguration.set(name, null);
		fileConfiguration.save(file);
	}

	public void saveSpecialInventory(PlayerInventory inventory, String name) throws Exception{
        String sectionPathContents = String.format("%s.contents", name);
        String sectionPathArmor    = String.format("%s.armor", name);
        saveInventory(inventory, getSpecialInventoryFile(), sectionPathContents, sectionPathArmor);
	}

	private void loadInventory(String playerName, PlayerInventory inventory, int index){
        String sectionPathContents = String.format("inv%d.contents", index);
        String sectionPathArmor    = String.format("inv%d.armor", index);
        loadInventory(playerName, inventory, getInventoryFile(playerName), sectionPathContents, sectionPathArmor);
	}

	private void loadSpecialInventory(String playerName, PlayerInventory inventory, String name){
        String sectionPathContents = String.format("%s.contents", name);
        String sectionPathArmor    = String.format("%s.armor", name);
        loadInventory(playerName, inventory, getSpecialInventoryFile(), sectionPathContents, sectionPathArmor);
	}

}
