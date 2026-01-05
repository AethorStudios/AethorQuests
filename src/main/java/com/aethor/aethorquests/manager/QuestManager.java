package com.aethor.aethorquests.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.Dialogue;
import com.aethor.aethorquests.model.Objective;
import com.aethor.aethorquests.model.ObjectiveType;
import com.aethor.aethorquests.model.QuestDefinition;
import com.aethor.aethorquests.model.QuestRequirements;
import com.aethor.aethorquests.model.Reward;

/**
 * Manages quest definitions loaded from quests.yml
 */
public class QuestManager {
    private final AethorQuestsPlugin plugin;
    private final Map<String, QuestDefinition> quests;
    private File questsFile;
    
    public QuestManager(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
        this.quests = new HashMap<>();
    }
    
    /**
     * Load all quests from quests.yml
     */
    public void loadQuests() {
        quests.clear();
        
        // Ensure quests.yml exists
        questsFile = new File(plugin.getDataFolder(), "quests.yml");
        if (!questsFile.exists()) {
            plugin.saveResource("quests.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(questsFile);
        ConfigurationSection questsSection = config.getConfigurationSection("quests");
        
        if (questsSection == null) {
            plugin.getLogger().warning("No quests found in quests.yml");
            return;
        }
        
        int loaded = 0;
        int failed = 0;
        
        for (String questKey : questsSection.getKeys(false)) {
            try {
                ConfigurationSection questSection = questsSection.getConfigurationSection(questKey);
                if (questSection == null) continue;
                
                QuestDefinition quest = loadQuest(questSection);
                if (quest != null) {
                    quests.put(quest.getId(), quest);
                    loaded++;
                    
                    if (plugin.isDebug()) {
                        plugin.getLogger().info("Loaded quest: " + quest.getId() + " - " + quest.getTitle());
                    }
                }
            } catch (Exception e) {
                failed++;
                plugin.getLogger().log(Level.SEVERE, "Failed to load quest: " + questKey, e);
            }
        }
        
        plugin.getLogger().info("Loaded " + loaded + " quest(s)" + (failed > 0 ? " (" + failed + " failed)" : ""));
    }
    
    /**
     * Load a single quest from configuration
     */
    private QuestDefinition loadQuest(ConfigurationSection section) {
        String id = section.getString("id");
        if (id == null || id.isEmpty()) {
            plugin.getLogger().warning("Quest missing ID, skipping");
            return null;
        }
        
        QuestDefinition quest = new QuestDefinition(id);
        
        // Basic info
        quest.setTitle(section.getString("title", id));
        quest.setDescription(section.getStringList("description"));
        quest.setGiverNpcId(section.getString("giverNpcId", ""));
        
        // Requirements
        ConfigurationSection reqSection = section.getConfigurationSection("requirements");
        if (reqSection != null) {
            QuestRequirements requirements = new QuestRequirements();
            requirements.setMinLevel(reqSection.getInt("minLevel", 1));
            requirements.setRequiredQuestsCompleted(reqSection.getStringList("requiredQuestsCompleted"));
            quest.setRequirements(requirements);
        }
        
        // Objectives
        List<?> objectivesList = section.getList("objectives");
        if (objectivesList != null) {
            for (Object obj : objectivesList) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> objMap = (Map<String, Object>) obj;
                    Objective objective = loadObjective(objMap);
                    if (objective != null) {
                        quest.addObjective(objective);
                    }
                }
            }
        }
        
        // Rewards
        ConfigurationSection rewardSection = section.getConfigurationSection("rewards");
        if (rewardSection != null) {
            Reward reward = new Reward();
            reward.setXp(rewardSection.getInt("xp", 0));
            reward.setMoney(rewardSection.getDouble("money", 0.0));
            reward.setCommands(rewardSection.getStringList("commands"));
            quest.setRewards(reward);
        }
        
        // Dialogue
        ConfigurationSection dialogueSection = section.getConfigurationSection("dialogue");
        if (dialogueSection != null) {
            Dialogue dialogue = new Dialogue();
            dialogue.setAcceptDialogue(dialogueSection.getStringList("acceptDialogue"));
            dialogue.setProgressDialogue(dialogueSection.getStringList("progressDialogue"));
            dialogue.setCompletionDialogue(dialogueSection.getStringList("completionDialogue"));
            quest.setDialogue(dialogue);
        }
        
        return quest;
    }
    
    /**
     * Load an objective from a map
     */
    private Objective loadObjective(Map<String, Object> map) {
        String typeStr = (String) map.get("type");
        if (typeStr == null) return null;
        
        ObjectiveType type;
        try {
            type = ObjectiveType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid objective type: " + typeStr);
            return null;
        }
        
        String description = (String) map.getOrDefault("description", "Complete objective");
        Objective objective = new Objective(type, description);
        
        switch (type) {
            case KILL:
                String target = (String) map.get("target");
                int amount = ((Number) map.getOrDefault("amount", 1)).intValue();
                
                // Try to parse as EntityType first (vanilla)
                try {
                    EntityType entityType = EntityType.valueOf(target.toUpperCase());
                    objective.setEntityType(entityType);
                } catch (IllegalArgumentException e) {
                    // Not a vanilla entity, treat as MythicMob
                    objective.setMythicMobName(target);
                }
                
                objective.setKillAmount(amount);
                break;
                
            case TALK:
                objective.setTalkNpcId((String) map.get("target"));
                break;
                
            case COLLECT:
                String materialStr = (String) map.get("target");
                int collectAmount = ((Number) map.getOrDefault("amount", 1)).intValue();
                
                try {
                    Material material = Material.valueOf(materialStr.toUpperCase());
                    objective.setCollectMaterial(material);
                    objective.setCollectAmount(collectAmount);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material: " + materialStr);
                    return null;
                }
                break;
                
            case VISIT:
                objective.setVisitWorld((String) map.get("world"));
                objective.setVisitX(((Number) map.getOrDefault("x", 0)).doubleValue());
                objective.setVisitY(((Number) map.getOrDefault("y", 64)).doubleValue());
                objective.setVisitZ(((Number) map.getOrDefault("z", 0)).doubleValue());
                objective.setVisitRadius(((Number) map.getOrDefault("radius", 
                    plugin.getConfig().getDouble("visitCheck.radiusDefault", 5.0))).doubleValue());
                break;
        }
        
        return objective;
    }
    
    /**
     * Get a quest by ID
     */
    public QuestDefinition getQuest(String questId) {
        return quests.get(questId);
    }
    
    /**
     * Get all quests
     */
    public Collection<QuestDefinition> getAllQuests() {
        return quests.values();
    }
    
    /**
     * Get quests offered by a specific NPC
     */
    public List<QuestDefinition> getQuestsByGiverNpcId(String npcId) {
        return quests.values().stream()
                .filter(q -> q.getGiverNpcId().equals(npcId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all unique NPC IDs that have quests assigned
     */
    public Set<String> getAllNpcIds() {
        return quests.values().stream()
                .map(QuestDefinition::getGiverNpcId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Reload all quests
     */
    public void reload() {
        loadQuests();
    }
}
