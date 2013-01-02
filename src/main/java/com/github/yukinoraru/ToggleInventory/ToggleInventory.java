package com.github.yukinoraru.ToggleInventory;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ToggleInventory extends JavaPlugin implements Listener {

    protected Logger log;
    protected UpdateChecker updateChecker;
    protected InventoryManager inventoryManager;

    public void onEnable(){
    	this.log = this.getLogger();
    	this.updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/server-mods/toggleinventory/files.rss");
    	this.inventoryManager = new InventoryManager(this);

    	if(this.updateChecker.updateNeeded()){
    		this.log.info("A new version is available: v." + this.updateChecker.getVersion());
    		this.log.info("Get it from: " + this.updateChecker.getLink());
    	}
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
    }

    //
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        // make sure the sender is a Player before casting
        Player player = null;
        String playerName = null;
        if (sender instanceof Player) {
            player = (Player)sender;
            playerName = player.getName();
         } else {
            sender.sendMessage("You must be a player!");
            return false;
         }

        // implement /togglei command.
        boolean isToggleInvCommand = cmd.getName().equalsIgnoreCase("togglei");
        boolean isReverse = cmd.getName().equalsIgnoreCase("toggleir");

        if(isToggleInvCommand || isReverse){
            if(!player.hasPermission("toggle_inventory.toggle")){
            	outputError("You don't have permission to toggle inventory.", player);
                return true;
            }

            // toggle inventory (/ti or /it, and /ti [n])
            try {
            	if(inventoryManager.getSpecialInventoryUsingStatus(playerName)){
            		inventoryManager.restoreInventory(player);
            		inventoryManager.setSpecialInventoryUsingStatus(playerName, false);
            	}else if (args.length >= 1 && args[0].length() > 0) {
    				int index = Integer.parseInt(args[0]);
    				inventoryManager.toggleInventory(player, index);
    			}
    			else{
    				inventoryManager.toggleInventory(player, !isReverse);
    			}
				player.sendMessage(inventoryManager.makeInventoryMessage(player) + " inventory toggled.");
			} catch(NumberFormatException e){
				outputError("Index must be a number.", player);
			} catch (Exception e) {
				outputError(e.getMessage(), player);
			}
            return true;
        }

        // toggle special inventory (/tis or /tis [name])
        boolean isTogglelInvSpecialCommand = cmd.getName().equalsIgnoreCase("toggleis");
        boolean isTISReverse = cmd.getName().equalsIgnoreCase("toggleisr");
        if(isTogglelInvSpecialCommand || isTISReverse){
			if (!player.hasPermission("toggle_inventory.toggle_special")) {
				outputError("You don't have permission to toggle special inventories.", player);
				return true;
			}
			try {
				if (args.length == 1 && args[0].length() > 0) {
					inventoryManager.toggleSpecialInventory(player, args[0]);
				} else {
					inventoryManager.toggleSpecialInventory(player, !isTISReverse);
				}
				player.sendMessage(inventoryManager.makeSpecialInventoryMessage(player) + " inventory toggled.");
			} catch (Exception e) {
				outputError(e.getMessage(), player);
				e.printStackTrace();
			}
        }
        return true;
    }

    public void onDisable(){
    }

    private void outputError(String msg){
        getLogger().warning(msg);
    }

    private void outputError(String msg, CommandSender sender){
        sender.sendMessage(ChatColor.RED + "[ERROR] " + msg);
        outputError(msg);
    }
}
