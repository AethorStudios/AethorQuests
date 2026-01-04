package com.aethor.aethorquests.manager;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages reading and writing quests to quests.yml
 */
public class QuestFileManager {
    private final AethorQuestsPlugin plugin;
    private final File questsFile;
    
    public QuestFileManager(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
        this.questsFile = new File(plugin.getDataFolder(), "quests.yml");
    }
    
    /**
     * Add a new quest to the quests.yml file
     */
    public void addQuest(QuestDefinition quest) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(questsFile);
        
        String path = "quests." + quest.getId();
        
        config.set(path + ".id", quest.getId());
        config.set(path + ".title", quest.getTitle());
        config.set(path + ".description", quest.getDescription());
        config.set(path + ".giverNpcId", quest.getGiverNpcId());
        
        // Requirements
        config.set(path + ".requirements.minLevel", quest.getRequirements().getMinLevel());
        config.set(path + ".requirements.requiredQuestsCompleted", quest.getRequirements().getRequiredQuestsCompleted());
        
        // Objectives
        List<Map<String, Object>> objectivesList = new ArrayList<>();
        for (Objective obj : quest.getObjectives()) {
            Map<String, Object> objMap = new HashMap<>();
            objMap.put("type", obj.getType().name());
            objMap.put("description", obj.getDescription());
            
            switch (obj.getType()) {
                case KILL -> {
                    if (obj.isMythicMob()) {
                        objMap.put("target", obj.getMythicMobName());
                    } else {
                        objMap.put("target", obj.getEntityType().name());
                    }
                    objMap.put("amount", obj.getKillAmount());
                }
                case TALK -> objMap.put("target", obj.getTalkNpcId());
                case COLLECT -> {
                    objMap.put("target", obj.getCollectMaterial().name());
                    objMap.put("amount", obj.getCollectAmount());
                }
                case VISIT -> {
                    objMap.put("world", obj.getVisitWorld());
                    objMap.put("x", obj.getVisitX());
                    objMap.put("y", obj.getVisitY());
                    objMap.put("z", obj.getVisitZ());
                    objMap.put("radius", obj.getVisitRadius());
                }
            }
            
            objectivesList.add(objMap);
        }
        config.set(path + ".objectives", objectivesList);
        
        // Rewards
        Reward rewards = quest.getRewards();
        config.set(path + ".rewards.xp", rewards.getXp());
        config.set(path + ".rewards.money", rewards.getMoney());
        config.set(path + ".rewards.commands", rewards.getCommands());
        config.set(path + ".rewards.items", new ArrayList<>()); // Items handled separately
        
        try {
            config.save(questsFile);
            plugin.getLogger().info("Saved quest: " + quest.getId());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save quest: " + quest.getId(), e);
        }
    }
    
    /**
     * Update an existing quest in the quests.yml file
     */
    public void updateQuest(QuestDefinition quest) {
        // Remove old quest
        removeQuest(quest.getId());
        // Add updated quest
        addQuest(quest);
    }
    
    /**
     * Remove a quest from the quests.yml file
     */
    public void removeQuest(String questId) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(questsFile);
        
        ConfigurationSection questsSection = config.getConfigurationSection("quests");
        if (questsSection != null) {
            questsSection.set(questId, null);
        }
        
        try {
            config.save(questsFile);
            plugin.getLogger().info("Removed quest: " + questId);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to remove quest: " + questId, e);
        }
    }
    
    /**
     * Save all currently loaded quests back to the file
     */
    public void saveAllQuests() {
        FileConfiguration config = new YamlConfiguration();
        
        for (QuestDefinition quest : plugin.getQuestManager().getAllQuests()) {
            String path = "quests." + quest.getId();
            
            config.set(path + ".id", quest.getId());
            config.set(path + ".title", quest.getTitle());
            config.set(path + ".description", quest.getDescription());
            config.set(path + ".giverNpcId", quest.getGiverNpcId());
            
            // Requirements
            config.set(path + ".requirements.minLevel", quest.getRequirements().getMinLevel());
            config.set(path + ".requirements.requiredQuestsCompleted", quest.getRequirements().getRequiredQuestsCompleted());
            
            // Objectives
            List<Map<String, Object>> objectivesList = new ArrayList<>();
            for (Objective obj : quest.getObjectives()) {
                Map<String, Object> objMap = new HashMap<>();
                objMap.put("type", obj.getType().name());
                objMap.put("description", obj.getDescription());
                
                switch (obj.getType()) {
                    case KILL -> {
                        if (obj.isMythicMob()) {
                            objMap.put("target", obj.getMythicMobName());
                        } else {
                            objMap.put("target", obj.getEntityType().name());
                        }
                        objMap.put("amount", obj.getKillAmount());
                    }
                    case TALK -> objMap.put("target", obj.getTalkNpcId());
                    case COLLECT -> {
                        objMap.put("target", obj.getCollectMaterial().name());
                        objMap.put("amount", obj.getCollectAmount());
                    }
                    case VISIT -> {
                        objMap.put("world", obj.getVisitWorld());
                        objMap.put("x", obj.getVisitX());
                        objMap.put("y", obj.getVisitY());
                        objMap.put("z", obj.getVisitZ());
                        objMap.put("radius", obj.getVisitRadius());
                    }
                }
                
                objectivesList.add(objMap);
            }
            config.set(path + ".objectives", objectivesList);
            
            // Rewards
            Reward rewards = quest.getRewards();
            config.set(path + ".rewards.xp", rewards.getXp());
            config.set(path + ".rewards.money", rewards.getMoney());
            config.set(path + ".rewards.commands", rewards.getCommands());
            config.set(path + ".rewards.items", new ArrayList<>());
        }
        
        try {
            config.save(questsFile);
            plugin.getLogger().info("Saved all quests to file");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save quests", e);
        }
    }
}
