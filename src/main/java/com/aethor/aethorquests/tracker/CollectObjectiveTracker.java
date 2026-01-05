package com.aethor.aethorquests.tracker;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Validates COLLECT objectives (checked at turn-in time)
 */
public class CollectObjectiveTracker {
    private final AethorQuestsPlugin plugin;
    
    public CollectObjectiveTracker(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if player has the required items for a collect objective
     */
    public boolean hasRequiredItems(Player player, Objective objective) {
        if (objective.getType() != ObjectiveType.COLLECT) {
            return false;
        }
        
        int required = objective.getCollectAmount();
        int found = countItems(player, objective.getCollectMaterial());
        
        return found >= required;
    }
    
    /**
     * Count items of a specific material in player's inventory
     */
    private int countItems(Player player, org.bukkit.Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    /**
     * Remove required items from player's inventory
     */
    public void removeItems(Player player, Objective objective) {
        if (objective.getType() != ObjectiveType.COLLECT) {
            return;
        }
        
        int toRemove = objective.getCollectAmount();
        org.bukkit.Material material = objective.getCollectMaterial();
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int amount = item.getAmount();
                if (amount <= toRemove) {
                    toRemove -= amount;
                    item.setAmount(0);
                } else {
                    item.setAmount(amount - toRemove);
                    toRemove = 0;
                }
                
                if (toRemove <= 0) break;
            }
        }
    }
    
    /**
     * Validate all COLLECT objectives for a quest
     */
    public boolean validateCollectObjectives(Player player, QuestDefinition quest, PlayerQuestState state) {
        // Check from current objective onwards
        for (int i = state.getObjectiveIndex(); i < quest.getObjectives().size(); i++) {
            Objective obj = quest.getObjectives().get(i);
            if (obj.getType() == ObjectiveType.COLLECT) {
                if (!hasRequiredItems(player, obj)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Process collect objectives and advance through them
     */
    public void processCollectObjectives(Player player, QuestDefinition quest, PlayerQuestState state) {
        UUID playerId = player.getUniqueId();
        
        while (state.getObjectiveIndex() < quest.getObjectives().size()) {
            Objective currentObj = quest.getCurrentObjective(state.getObjectiveIndex());
            
            if (currentObj.getType() == ObjectiveType.COLLECT) {
                if (hasRequiredItems(player, currentObj)) {
                    // Remove items
                    removeItems(player, currentObj);
                    
                    // Advance objective
                    if (quest.isLastObjective(state.getObjectiveIndex())) {
                        state.setStatus(QuestStatus.COMPLETED);
                        
                        // Refresh quest marker for turn-in NPC
                        if (plugin.getMarkerController() != null && quest.getGiverNpcId() != null) {
                            plugin.getMarkerController().refreshMarkerForPlayerAndNpc(player, quest.getGiverNpcId());
                        }
                        
                        break;
                    } else {
                        state.advanceToNextObjective();
                    }
                } else {
                    // Missing items, can't proceed
                    break;
                }
            } else {
                // Not a collect objective, stop processing
                break;
            }
        }
        
        plugin.getPlayerDataStore().setQuestState(playerId, state);
    }
}
