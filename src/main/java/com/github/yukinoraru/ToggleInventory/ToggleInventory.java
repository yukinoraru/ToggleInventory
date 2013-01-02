package com.github.yukinoraru.ToggleInventory;

import java.util.logging.Logger;
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

    	if(this.updateChecker.updateNeeded()){
    		this.log.info("A new version is available: v." + this.updateChecker.getVersion());
    		this.log.info("Get it from: " + this.updateChecker.getLink());
    	}

    	this.inventoryManager = new InventoryManager(this);
    }

    //
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        // make sure the sender is a Player before casting
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
         } else {
            sender.sendMessage("You must be a player!");
            return false;
         }

        // implement /togglei command.
        boolean isToggleInvCommand = cmd.getName().equalsIgnoreCase("togglei");
        boolean isReverse = cmd.getName().equalsIgnoreCase("toggleir");

        if(isToggleInvCommand || isReverse){
            if(!player.hasPermission("toggle_inventory.toggle")){
                player.sendMessage("You don't have permission to toggle inventory.");
                return true;
            }

            // toggle inventory
            try {
				inventoryManager.toggleInventory(player, !isReverse);
				player.sendMessage(inventoryManager.makeInventoryMessage(player));
			} catch (Exception e) {
				player.sendMessage(e.toString());
				e.printStackTrace();
			}

            return true;
        }

        return false;
    }

    public void onDisable(){
    }

    private void outputError(String msg){
        getLogger().warning(msg);
    }

    private void outputError(String msg, CommandSender sender){
        sender.sendMessage(msg);
        outputError(msg);
    }
}
