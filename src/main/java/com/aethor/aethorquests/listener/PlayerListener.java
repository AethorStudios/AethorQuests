package com.aethor.aethorquests.listener;

import com.aethor.aethorquests.AethorQuestsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events for data loading/saving
 */
public class PlayerListener implements Listener {
    private final AethorQuestsPlugin plugin;
    
    public PlayerListener(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player data asynchronously to avoid blocking login
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getPlayerDataStore().loadPlayerData(event.getPlayer().getUniqueId());
        });
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getConfig().getBoolean("storage.saveOnQuit", true)) {
            // Save and unload player data
            plugin.getPlayerDataStore().unloadPlayerData(event.getPlayer().getUniqueId());
        }
    }
}
