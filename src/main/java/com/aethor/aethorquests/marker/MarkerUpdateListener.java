package com.aethor.aethorquests.marker;

import com.aethor.aethorquests.AethorQuestsPlugin;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for events that require quest marker updates
 */
public class MarkerUpdateListener implements Listener {
    
    private final AethorQuestsPlugin plugin;
    private final MarkerHologramController controller;
    
    // Track which chunk each player is in to detect chunk changes
    private final Map<UUID, String> playerChunks = new ConcurrentHashMap<>();
    
    public MarkerUpdateListener(AethorQuestsPlugin plugin, MarkerHologramController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }
    
    /**
     * Refresh markers when player joins
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Delay initial refresh to ensure player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            controller.refreshMarkersForPlayer(player);
            updatePlayerChunk(player);
        }, 20L); // 1 second delay
    }
    
    /**
     * Clean up markers when player quits
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        controller.cleanupPlayerHolograms(playerId);
        playerChunks.remove(playerId);
    }
    
    /**
     * Refresh markers when player changes world
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        controller.refreshMarkersForPlayer(player);
        updatePlayerChunk(player);
    }
    
    /**
     * Refresh markers when player teleports (might be to different chunk/world)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Delay refresh to ensure teleport is complete
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                controller.refreshMarkersForPlayer(player);
                updatePlayerChunk(player);
            }
        }, 2L);
    }
    
    /**
     * Refresh markers when player moves to a new chunk
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player moved to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        String oldChunk = playerChunks.get(player.getUniqueId());
        String newChunk = getChunkKey(player.getLocation().getChunk());
        
        if (!newChunk.equals(oldChunk)) {
            playerChunks.put(player.getUniqueId(), newChunk);
            controller.refreshMarkersForPlayer(player);
        }
    }
    
    /**
     * Update stored chunk location for a player
     */
    private void updatePlayerChunk(Player player) {
        String chunkKey = getChunkKey(player.getLocation().getChunk());
        playerChunks.put(player.getUniqueId(), chunkKey);
    }
    
    /**
     * Gets a unique key for a chunk
     */
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}
