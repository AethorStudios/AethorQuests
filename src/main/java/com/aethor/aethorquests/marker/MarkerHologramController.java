package com.aethor.aethorquests.marker;

import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethorquests.AethorQuestsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player quest marker holograms using DecentHolograms.
 * Creates individual holograms above NPCs that only specific players can see.
 */
public class MarkerHologramController {
    
    private final AethorQuestsPlugin plugin;
    private final QuestMarkerService markerService;
    
    // Track last known state to avoid flicker
    private final Map<UUID, Map<String, MarkerState>> playerMarkerStates = new ConcurrentHashMap<>();
    
    // Per-player hologram tracking: player UUID -> npc ID -> hologram name
    private final Map<UUID, Map<String, String>> playerHolograms = new ConcurrentHashMap<>();
    
    // Refresh task
    private BukkitTask refreshTask;
    
    // Configuration
    private static final double MARKER_Y_OFFSET = 2.5; // Above NPC nameplate
    private static final double VISIBILITY_RANGE = 48.0;
    private static final long REFRESH_INTERVAL_TICKS = 40L; // 2 seconds
    
    public MarkerHologramController(AethorQuestsPlugin plugin, QuestMarkerService markerService) {
        this.plugin = plugin;
        this.markerService = markerService;
    }
    
    /**
     * Starts the marker system and periodic refresh task
     */
    public void start() {
        // Start periodic refresh task
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refreshMarkersForPlayer(player);
            }
        }, REFRESH_INTERVAL_TICKS, REFRESH_INTERVAL_TICKS);
        
        plugin.getLogger().info("Quest marker system started");
    }
    
    /**
     * Stops the marker system and cleans up all holograms
     */
    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        
        // Clean up all player holograms
        for (UUID playerId : playerHolograms.keySet()) {
            cleanupPlayerHolograms(playerId);
        }
        
        playerHolograms.clear();
        playerMarkerStates.clear();
        
        plugin.getLogger().info("Quest marker system stopped");
    }
    
    /**
     * Refreshes markers for a specific player
     */
    public void refreshMarkersForPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }
        
        World playerWorld = player.getWorld();
        Location playerLoc = player.getLocation();
        
        // Get or create state map for this player
        Map<String, MarkerState> stateMap = playerMarkerStates.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>());
        
        // Get or create hologram map for this player
        Map<String, String> hologramMap = playerHolograms.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>());
        
        // Get all NPCs with quests
        Set<String> npcIds = plugin.getQuestManager().getAllNpcIds();
        
        for (String npcId : npcIds) {
            // Get NPC location
            Optional<Location> npcLocationOpt = getNpcLocation(npcId);
            if (!npcLocationOpt.isPresent()) {
                continue;
            }
            
            Location npcLoc = npcLocationOpt.get();
            
            // Check if NPC is in same world and within range
            if (!npcLoc.getWorld().equals(playerWorld) || 
                npcLoc.distance(playerLoc) > VISIBILITY_RANGE) {
                // Remove marker if it exists
                removeMarkerForPlayer(player, npcId, hologramMap);
                stateMap.remove(npcId);
                continue;
            }
            
            // Compute marker state
            MarkerState newState = markerService.computeMarkerState(player, npcId);
            MarkerState oldState = stateMap.get(npcId);
            
            // Only update if state changed
            if (newState != oldState) {
                stateMap.put(npcId, newState);
                updateMarkerForPlayer(player, npcId, npcLoc, newState, hologramMap);
            }
        }
    }
    
    /**
     * Refreshes markers for a specific player and NPC (used after quest accept/complete)
     */
    public void refreshMarkerForPlayerAndNpc(Player player, String npcId) {
        if (!player.isOnline()) {
            return;
        }
        
        Map<String, MarkerState> stateMap = playerMarkerStates.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>());
        
        Map<String, String> hologramMap = playerHolograms.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>());
        
        Optional<Location> npcLocationOpt = getNpcLocation(npcId);
        if (!npcLocationOpt.isPresent()) {
            return;
        }
        
        Location npcLoc = npcLocationOpt.get();
        MarkerState newState = markerService.computeMarkerState(player, npcId);
        
        stateMap.put(npcId, newState);
        updateMarkerForPlayer(player, npcId, npcLoc, newState, hologramMap);
    }
    
    /**
     * Updates or creates a marker hologram for a player at an NPC
     */
    private void updateMarkerForPlayer(Player player, String npcId, Location npcLoc, 
                                       MarkerState state, Map<String, String> hologramMap) {
        if (state == MarkerState.NONE) {
            removeMarkerForPlayer(player, npcId, hologramMap);
            return;
        }
        
        String markerText = getMarkerText(state);
        Location markerLoc = npcLoc.clone().add(0, MARKER_Y_OFFSET, 0);
        
        String hologramName = hologramMap.get(npcId);
        
        if (hologramName == null) {
            // Create new hologram
            hologramName = "quest_marker_" + player.getUniqueId() + "_" + npcId;
            createHologram(hologramName, markerLoc, markerText, player);
            hologramMap.put(npcId, hologramName);
        } else {
            // Update existing hologram
            updateHologram(hologramName, markerLoc, markerText);
        }
    }
    
    /**
     * Removes a marker hologram for a player
     */
    private void removeMarkerForPlayer(Player player, String npcId, Map<String, String> hologramMap) {
        String hologramName = hologramMap.remove(npcId);
        if (hologramName != null) {
            deleteHologram(hologramName);
        }
    }
    
    /**
     * Cleans up all holograms for a player (on quit)
     */
    public void cleanupPlayerHolograms(UUID playerId) {
        Map<String, String> hologramMap = playerHolograms.remove(playerId);
        if (hologramMap != null) {
            for (String hologramName : hologramMap.values()) {
                deleteHologram(hologramName);
            }
        }
        playerMarkerStates.remove(playerId);
    }
    
    /**
     * Gets the marker text based on state
     */
    private String getMarkerText(MarkerState state) {
        switch (state) {
            case AVAILABLE:
                return "§a§l!";
            case TURNIN:
                return "§e§l?";
            default:
                return "";
        }
    }
    
    /**
     * Gets the location of an NPC by ID
     */
    private Optional<Location> getNpcLocation(String npcId) {
        if (!plugin.getNpcHook().isEnabled()) {
            return Optional.empty();
        }
        
        try {
            Optional<Npc> npcOpt = plugin.getNpcHook().getApi().getNpc(npcId);
            if (!npcOpt.isPresent()) {
                return Optional.empty();
            }
            
            Npc npc = npcOpt.get();
            UUID entityUuid = npc.getEntityUuid();
            
            // Find the entity in all worlds
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(entityUuid)) {
                        return Optional.of(entity.getLocation());
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting NPC location for " + npcId + ": " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Creates a new hologram using DecentHolograms API (placeholder - implement with actual API)
     */
    private void createHologram(String name, Location location, String text, Player viewer) {
        // TODO: Implement DecentHolograms integration
        // For now, this is a placeholder that shows the marker approach
        // You'll need to use DecentHolograms' per-player hologram API:
        // 
        // DHAPI.createHologram(name, location);
        // DHAPI.setHologramLine(name, 0, text);
        // DHAPI.addHologramViewer(name, viewer);
        //
        // Note: Make sure the hologram is only visible to the specified viewer
        
        if (plugin.isDebug()) {
            plugin.getLogger().info("Creating marker hologram '" + name + "' at " + 
                    location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + 
                    " for player " + viewer.getName());
        }
    }
    
    /**
     * Updates an existing hologram
     */
    private void updateHologram(String name, Location location, String text) {
        // TODO: Implement DecentHolograms integration
        // DHAPI.moveHologram(name, location);
        // DHAPI.setHologramLine(name, 0, text);
        
        if (plugin.isDebug()) {
            plugin.getLogger().info("Updating marker hologram '" + name + "'");
        }
    }
    
    /**
     * Deletes a hologram
     */
    private void deleteHologram(String name) {
        // TODO: Implement DecentHolograms integration
        // DHAPI.removeHologram(name);
        
        if (plugin.isDebug()) {
            plugin.getLogger().info("Deleting marker hologram '" + name + "'");
        }
    }
}
