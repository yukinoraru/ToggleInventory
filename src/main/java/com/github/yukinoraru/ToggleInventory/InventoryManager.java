package com.github.yukinoraru.ToggleInventory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
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

    public File getDefaultSpecialInventoryFile(){
    	return new File(plugin.getDataFolder(), CONFIG_FILENAME_SPECIAL_INV);
    }

	private void prepareFile(File file) throws Exception{
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

	private FileConfiguration getPlayersFileConfiguration(String playerName){
		File file = getInventoryFile(playerName);
		return YamlConfiguration.loadConfiguration(file);
	}

	//
	private void setPlayerConfig(String playerName, String section, Object obj) throws IOException{
		File file = getInventoryFile(playerName);
		FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.set(section, obj);
        fileConfiguration.save(file);
	}

	public boolean getSpecialInventoryUsingStatus(String playerName){
        return getPlayersFileConfiguration(playerName).getBoolean("sp_using");
	}

	private String getCurrentSpecialInventoryIndex(String playerName){
        return getPlayersFileConfiguration(playerName).getString("sp_current", "");
	}

	public int getCurrentInventoryIndex(String playerName){
        return getPlayersFileConfiguration(playerName).getInt("current", 1);
	}

	public boolean isFirstUseForToggleInventorySpecial(String playerName){
        return getPlayersFileConfiguration(playerName).getBoolean("sp_firstuse", true);
	}

	private void setCurrentInventoryIndex(String playerName, int index) throws IOException{
		setPlayerConfig(playerName, "current", index);
	}

	private void setCurrentSpecialInventoryIndex(String playerName, String name) throws IOException{
		setPlayerConfig(playerName, "sp_current", name);
	}

	public void setSpecialInventoryUsingStatusForFirstUse(String playerName, boolean isFirstUse) throws IOException{
        setPlayerConfig(playerName, "sp_firstuse", isFirstUse);
	}

	public void setSpecialInventoryUsingStatus(String playerName, boolean isUsing) throws IOException{
		setPlayerConfig(playerName, "sp_using", isUsing);
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
		String []list = getListSpecialInventory(getInventoryFile(playerName));

		for (int i = 0; i < list.length; i++) {
			msg += ((list[i].equals(invCurrentName)) ? ChatColor.GREEN : ChatColor.DARK_GREEN) + list[i];
			msg += ChatColor.RESET + " ";
		}
		return msg + ChatColor.GRAY + "] ";
	}

	public void toggleInventory(CommandSender player, int index) throws Exception{

		// 1. get info
		String playerName = player.getName();

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
		saveInventory((Player) player, String.valueOf(currentIndex), false);
		loadInventory((Player) player, String.valueOf(nextIndex), false);

		// 4. set next inv
		setCurrentInventoryIndex(playerName, nextIndex);
	}

	public void toggleSpecialInventory(CommandSender player, String inventoryName) throws Exception{
		// 1. get current inv index and save inventory
		String playerName = player.getName();
		if (!getSpecialInventoryUsingStatus(playerName)) {
			int currentIndex = getCurrentInventoryIndex(playerName);
			saveInventory((Player) player, String.valueOf(currentIndex), false);
		}

		// 2. matching inv with inventoryName
		String []list = getListSpecialInventory(getInventoryFile(playerName));
        if(list == null){
			throw new Exception("Your special inventory is empty.\n Try '/tis add' or '/tis reset -f', '/tis reset-default -f'");
		}
		int index = LevenshteinDistance.find(list, inventoryName);
		String specialInvName = list[index];

		// 3. load inventory
		loadSpecialInventory((Player) player, specialInvName);

		// 4. set SpInv index
		setCurrentSpecialInventoryIndex(playerName, specialInvName);

		// 5. update SpInv Using Status
		setSpecialInventoryUsingStatus(playerName, true);
	}

	private String[] getListSpecialInventory(File specialInventoryFile) throws Exception{
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(specialInventoryFile);
        try{
            Set<String> nameList = fileConfiguration.getConfigurationSection("special_inventories").getKeys(false);
    		String []tmp = nameList.toArray(new String[0]);
    		if(tmp.length == 0){
    			throw new Exception();
    		}
    		return tmp;
        }catch(Exception e){
        	return null;
        }
	}

	public String getNextSpecialInventory(String []list, String name, boolean rotateDirection){
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

	private boolean isExistSpecialInv(String playerName, String specialInventoryName) throws Exception{
		String []list = getListSpecialInventory(getInventoryFile(playerName));
		boolean isMatched = false;
		for(String name : list){
			if(name.equals(specialInventoryName)){
				isMatched = true;
				break;
			}
		}
		return isMatched;
	}

	public void copySpInvToNormalInventory(CommandSender player, String specialInventoryName, int destinationIndex) throws Exception{
		String playerName = player.getName();

		// validation
		int maxIndex = getMaxInventoryIndex(player);
		if(destinationIndex <= 0 || destinationIndex > maxIndex){
			throw new Exception("Wrong destination index.");
		}

		// isExit?
		if(!isExistSpecialInv(playerName, specialInventoryName)){
			throw new Exception(String.format("No such special inventory found: '%s'", specialInventoryName));
		}

        File playerInventoryFile = getInventoryFile(playerName);
        File specialInventoryFile = getInventoryFile(playerName);
        FileConfiguration playerFileConfiguration = YamlConfiguration.loadConfiguration(playerInventoryFile);
        FileConfiguration spinvFileConfiguration = YamlConfiguration.loadConfiguration(specialInventoryFile);

        String playerSectionPathContents = getSectionPathForUserContents(destinationIndex);
        String playerSectionPathArmor = getSectionPathForUserArmor(destinationIndex);

        String spinvSectionPathContents = getSectionPathForSPInvContents(specialInventoryName);
        String spinvSectionPathArmor    = getSectionPathForSPInvArmor(specialInventoryName);

        // copy
        playerFileConfiguration.set(playerSectionPathContents, spinvFileConfiguration.get(spinvSectionPathContents));
        playerFileConfiguration.set(playerSectionPathArmor, spinvFileConfiguration.get(spinvSectionPathArmor));

        playerFileConfiguration.save(playerInventoryFile);

        return;
	}

	public void toggleSpecialInventory(CommandSender player, boolean rotateDirection) throws Exception{
		String playerName = player.getName();
		String currentSpIndex = getCurrentSpecialInventoryIndex(playerName);
		String []list = getListSpecialInventory(getInventoryFile(playerName));
		try{
			String nextSpIndex = (getSpecialInventoryUsingStatus(playerName)) ? getNextSpecialInventory(
				list, currentSpIndex, rotateDirection) : currentSpIndex;
				toggleSpecialInventory(player, nextSpIndex);
		}catch(NullPointerException e){
			// delegate exception when NullPointerException occurred
			toggleSpecialInventory(player, null);
		}
	}

	public void toggleInventory(CommandSender player, boolean rotateDirection) throws Exception{
		String playerName = player.getName();

		int maxIndex = getMaxInventoryIndex(player);
		int currentIndex = getCurrentInventoryIndex(playerName);
		int nextIndex = calcNextInventoryIndex(maxIndex, currentIndex, rotateDirection);

		toggleInventory(player, nextIndex);
	}

	// index equals inventory name
	public void saveInventory(Player player, String index, boolean isSpecialInventory) throws Exception{

		// preparing
		PlayerInventory inventory = player.getInventory();
        Inventory inventoryArmor = InventoryUtils.getArmorInventory(inventory);

        // serialize
        String serializedInventoryContents = InventoryUtils.inventoryToString(inventory);
        String serializedInventoryArmor = InventoryUtils.inventoryToString(inventoryArmor);
        String serializedPotion = PotionUtils.serializePotion(player.getActivePotionEffects());

        // create section name
        String sectionPathContents;
        String sectionPathArmor;
        String sectionPathGameMode;
        String sectionPathPotion;

        // if special inv
        if(isSpecialInventory){
        	sectionPathContents = getSectionPathForSPInvContents(index);
        	sectionPathArmor = getSectionPathForSPInvArmor(index);
        	sectionPathGameMode = getSectionPathForSPInvGameMode(index);
        	sectionPathPotion = getSectionPathForSPInvPotion(index);
        }
        else{
        	int tmp = Integer.parseInt(index);
        	sectionPathContents = getSectionPathForUserContents(tmp);
        	sectionPathArmor = getSectionPathForUserArmor(tmp);
        	sectionPathGameMode = getSectionPathForUserInvGameMode(tmp);
        	sectionPathPotion = getSectionPathForUserInvPotion(tmp);
        }

        // save to config file
		File inventoryFile = getInventoryFile(player.getName());
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(inventoryFile);

        fileConfiguration.set(sectionPathContents, serializedInventoryContents);
        fileConfiguration.set(sectionPathArmor, serializedInventoryArmor);
        fileConfiguration.set(sectionPathGameMode, player.getGameMode().name());
        fileConfiguration.set(sectionPathPotion, serializedPotion);

		prepareFile(inventoryFile);

        fileConfiguration.save(inventoryFile);

        return ;
	}

	private void loadInventory(Player player, String index, boolean isSpecialInventory){

		File inventoryFile = getInventoryFile(player.getName());
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(inventoryFile);

        String sectionPathContents;
        String sectionPathArmor;
        String sectionPathGameMode;
        String sectionPathPotion;

        // set special inv
        if(isSpecialInventory){
        	sectionPathContents = getSectionPathForSPInvContents(index);
        	sectionPathArmor = getSectionPathForSPInvArmor(index);
        	sectionPathGameMode = getSectionPathForSPInvGameMode(index);
        	sectionPathPotion = getSectionPathForSPInvPotion(index);
        }
        else{
        	int tmp = Integer.parseInt(index);
        	sectionPathContents = getSectionPathForUserContents(tmp);
        	sectionPathArmor = getSectionPathForUserArmor(tmp);
        	sectionPathGameMode = getSectionPathForUserInvGameMode(tmp);
        	sectionPathPotion = getSectionPathForUserInvPotion(tmp);
        }

        // deserialize
        String serializedInventoryContents = fileConfiguration.getString(sectionPathContents);
        String serializedInventoryArmor  = fileConfiguration.getString(sectionPathArmor);

		PlayerInventory inventory = player.getInventory();

		inventory.clear();
		inventory.setArmorContents(null);

        if(serializedInventoryContents != null){
        	Inventory deserializedInventoryNormal = InventoryUtils.stringToInventory(serializedInventoryContents);

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
            Inventory deserialized_inv_armor = InventoryUtils.stringToInventory(serializedInventoryArmor);
            ItemStack []tmp = deserialized_inv_armor.getContents();
        	if(tmp != null){
        		inventory.setArmorContents(tmp);
        	}
        }

        // restore Game Mode
        String gamemode = fileConfiguration.getString(sectionPathGameMode);
        if(gamemode != null && gamemode.length() > 0){
        	player.setGameMode(GameMode.valueOf(gamemode));
        }

        // restore PotionEffect
        for (PotionEffect effect : player.getActivePotionEffects()){
            player.removePotionEffect(effect.getType());
        }
        // restore
        String effectsInString = fileConfiguration.getString(sectionPathPotion);
        try {
			player.addPotionEffects(PotionUtils.deserializePotion(effectsInString));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}

	private void deleteAllSPInventoryFromUser(String playerName) throws Exception{
		File playerFile = getInventoryFile(playerName);
        String [] playerSPInvList = getListSpecialInventory(playerFile);
		if (playerSPInvList != null) {
			for (String name : playerSPInvList) {
				deleteSpecialInventory(playerFile, name);
			}
		}
	}

	private String getSectionPathForSPInvPotion(String name) {
		return String.format("%s.potion", getSectionPathForSPInvRoot(name));
	}

	private String getSectionPathForUserInvPotion(int index) {
		return String.format("inv%d.potion", index);
	}


	private String getSectionPathForSPInvGameMode(String name) {
		return String.format("%s.gamemode", getSectionPathForSPInvRoot(name));
	}

	private String getSectionPathForUserInvGameMode(int index) {
		return String.format("inv%d.gamemode", index);
	}

	private String getSectionPathForUserContents(int index) {
		return String.format("inv%d.contents", index);
	}

	private String getSectionPathForUserArmor(int index) {
		return String.format("inv%d.armor", index);
	}

	private String getSectionPathForSPInvRoot(String name) {
		return String.format("special_inventories.%s", name);
	}

	private String getSectionPathForSPInvContents(String name) {
		return String.format("%s.contents", getSectionPathForSPInvRoot(name));
	}

	private String getSectionPathForSPInvArmor(String name) {
		return String.format("%s.armor", getSectionPathForSPInvRoot(name));
	}

	public void initializeSPInvFromDefault(String playerName) throws Exception{
        // delete
        deleteAllSPInventoryFromUser(playerName);

        // copy
		File defaultFile = getDefaultSpecialInventoryFile();
		File playerFile = getInventoryFile(playerName);
        FileConfiguration defaultFileConfiguration = YamlConfiguration.loadConfiguration(defaultFile);
        FileConfiguration playerFileConfiguration = YamlConfiguration.loadConfiguration(playerFile);

        String [] defaultSPInvList = getListSpecialInventory(defaultFile);

        if(defaultSPInvList == null){
			throw new Exception("There are no default special inventories in "
					+ CONFIG_FILENAME_SPECIAL_INV
					+ ". Please check it.");
		}

        for(String name : defaultSPInvList){
        	playerFileConfiguration.set(getSectionPathForSPInvContents(name), defaultFileConfiguration.get(getSectionPathForSPInvContents(name)));
        	playerFileConfiguration.set(getSectionPathForSPInvArmor(name), defaultFileConfiguration.get(getSectionPathForSPInvArmor(name)));
        }
        playerFileConfiguration.save(playerFile);
	}

	public void restoreInventory(CommandSender player) {
		int index = getCurrentInventoryIndex(((Player)player).getName());
		loadInventory((Player)player, index);
	}

	public void deleteSpecialInventory(File file, String name) throws IOException{
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        plugin.getLogger().warning(getSectionPathForSPInvRoot(name));
		fileConfiguration.set(getSectionPathForSPInvRoot(name), null);
		fileConfiguration.save(file);
	}

	public void saveSpecialInventory(Player player, String name) throws Exception{
        saveInventory(player, name, true);
	}

	private void loadInventory(Player player, int index){
        loadInventory(player, String.valueOf(index), false);
	}

	private void loadSpecialInventory(Player player, String name){
        loadInventory(player, name, true);
	}
}
