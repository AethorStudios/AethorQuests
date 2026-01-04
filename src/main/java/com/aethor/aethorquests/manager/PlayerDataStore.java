package com.aethor.aethorquests.manager;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.PlayerQuestState;
import com.aethor.aethorquests.model.QuestStatus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player quest progress data and persistence
 */
public class PlayerDataStore {
    private final AethorQuestsPlugin plugin;
    private final File playerDataFolder;
    private final Map<UUID, Map<String, PlayerQuestState>> playerData;
    
    public PlayerDataStore(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.playerData = new ConcurrentHashMap<>();
        
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }
    
    /**
     * Load player data from disk
     */
    public void loadPlayerData(UUID playerId) {
        File playerFile = getPlayerFile(playerId);
        
        if (!playerFile.exists()) {
            playerData.put(playerId, new ConcurrentHashMap<>());
            return;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            Map<String, PlayerQuestState> questStates = new ConcurrentHashMap<>();
            
            ConfigurationSection questsSection = config.getConfigurationSection("quests");
            if (questsSection != null) {
                for (String questId : questsSection.getKeys(false)) {
                    ConfigurationSection questSection = questsSection.getConfigurationSection(questId);
                    if (questSection == null) continue;
                    
                    PlayerQuestState state = new PlayerQuestState(questId);
                    state.setStatus(QuestStatus.valueOf(questSection.getString("status", "NOT_STARTED")));
                    state.setObjectiveIndex(questSection.getInt("objectiveIndex", 0));
                    state.setObjectiveProgress(questSection.getInt("objectiveProgress", 0));
                    state.setAcceptedTimestamp(questSection.getLong("acceptedTimestamp", 0));
                    state.setCompletedTimestamp(questSection.getLong("completedTimestamp", 0));
                    state.setTurnedInTimestamp(questSection.getLong("turnedInTimestamp", 0));
                    
                    questStates.put(questId, state);
                }
            }
            
            playerData.put(playerId, questStates);
            
            if (plugin.isDebug()) {
                plugin.getLogger().info("Loaded data for player " + playerId + " (" + questStates.size() + " quests)");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + playerId, e);
            playerData.put(playerId, new ConcurrentHashMap<>());
        }
    }
    
    /**
     * Save player data to disk
     */
    public void savePlayerData(UUID playerId) {
        Map<String, PlayerQuestState> questStates = playerData.get(playerId);
        if (questStates == null || questStates.isEmpty()) {
            return;
        }
        
        File playerFile = getPlayerFile(playerId);
        FileConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, PlayerQuestState> entry : questStates.entrySet()) {
            String questId = entry.getKey();
            PlayerQuestState state = entry.getValue();
            
            String path = "quests." + questId;
            config.set(path + ".status", state.getStatus().name());
            config.set(path + ".objectiveIndex", state.getObjectiveIndex());
            config.set(path + ".objectiveProgress", state.getObjectiveProgress());
            config.set(path + ".acceptedTimestamp", state.getAcceptedTimestamp());
            config.set(path + ".completedTimestamp", state.getCompletedTimestamp());
            config.set(path + ".turnedInTimestamp", state.getTurnedInTimestamp());
        }
        
        try {
            config.save(playerFile);
            
            if (plugin.isDebug()) {
                plugin.getLogger().info("Saved data for player " + playerId);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + playerId, e);
        }
    }
    
    /**
     * Save all player data
     */
    public void saveAll() {
        for (UUID playerId : playerData.keySet()) {
            savePlayerData(playerId);
        }
    }
    
    /**
     * Unload player data from memory (call on quit)
     */
    public void unloadPlayerData(UUID playerId) {
        savePlayerData(playerId);
        playerData.remove(playerId);
    }
    
    /**
     * Get quest state for a player
     */
    public PlayerQuestState getQuestState(UUID playerId, String questId) {
        Map<String, PlayerQuestState> states = playerData.get(playerId);
        if (states == null) {
            return null;
        }
        return states.get(questId);
    }
    
    /**
     * Get or create quest state for a player
     */
    public PlayerQuestState getOrCreateQuestState(UUID playerId, String questId) {
        Map<String, PlayerQuestState> states = playerData.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        return states.computeIfAbsent(questId, k -> new PlayerQuestState(questId));
    }
    
    /**
     * Set quest state for a player
     */
    public void setQuestState(UUID playerId, PlayerQuestState state) {
        Map<String, PlayerQuestState> states = playerData.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        states.put(state.getQuestId(), state);
    }
    
    /**
     * Get all quest states for a player
     */
    public Collection<PlayerQuestState> getPlayerQuestStates(UUID playerId) {
        Map<String, PlayerQuestState> states = playerData.get(playerId);
        if (states == null) {
            return Collections.emptyList();
        }
        return states.values();
    }
    
    /**
     * Get active quests for a player
     */
    public List<PlayerQuestState> getActiveQuests(UUID playerId) {
        List<PlayerQuestState> active = new ArrayList<>();
        for (PlayerQuestState state : getPlayerQuestStates(playerId)) {
            if (state.getStatus() == QuestStatus.ACTIVE) {
                active.add(state);
            }
        }
        return active;
    }
    
    /**
     * Get completed quests for a player (turned in)
     */
    public List<PlayerQuestState> getCompletedQuests(UUID playerId) {
        List<PlayerQuestState> completed = new ArrayList<>();
        for (PlayerQuestState state : getPlayerQuestStates(playerId)) {
            if (state.getStatus() == QuestStatus.TURNED_IN) {
                completed.add(state);
            }
        }
        return completed;
    }
    
    /**
     * Check if player has completed a quest
     */
    public boolean hasCompletedQuest(UUID playerId, String questId) {
        PlayerQuestState state = getQuestState(playerId, questId);
        return state != null && state.getStatus() == QuestStatus.TURNED_IN;
    }
    
    private File getPlayerFile(UUID playerId) {
        return new File(playerDataFolder, playerId.toString() + ".yml");
    }
}
