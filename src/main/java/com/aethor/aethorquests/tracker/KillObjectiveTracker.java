package com.aethor.aethorquests.tracker;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;
import java.util.UUID;

/**
 * Tracks KILL objectives for active quests
 */
public class KillObjectiveTracker implements Listener {
    private final AethorQuestsPlugin plugin;
    
    public KillObjectiveTracker(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        UUID playerId = killer.getUniqueId();
        
        // Check all active quests for this player
        List<PlayerQuestState> activeQuests = plugin.getPlayerDataStore().getActiveQuests(playerId);
        
        for (PlayerQuestState state : activeQuests) {
            QuestDefinition quest = plugin.getQuestManager().getQuest(state.getQuestId());
            if (quest == null) continue;
            
            Objective currentObjective = quest.getCurrentObjective(state.getObjectiveIndex());
            if (currentObjective == null || currentObjective.getType() != ObjectiveType.KILL) {
                continue;
            }
            
            // Check if the killed entity matches the objective
            boolean matches = false;
            
            if (currentObjective.isMythicMob()) {
                // Check for MythicMobs (via entity metadata/tags)
                // TODO: Integrate with MythicMobs API when available
                String mythicType = getMythicMobType(event.getEntity());
                if (mythicType != null && mythicType.equals(currentObjective.getMythicMobName())) {
                    matches = true;
                }
            } else {
                // Check vanilla entity type
                if (event.getEntityType() == currentObjective.getEntityType()) {
                    matches = true;
                }
            }
            
            if (matches) {
                // Increment progress
                state.incrementProgress(1);
                
                int required = currentObjective.getKillAmount();
                int current = state.getObjectiveProgress();
                
                // Notify player
                plugin.getQuestUI().notifyProgress(killer, quest, currentObjective, current, required);
                
                // Check if objective is complete
                if (current >= required) {
                    completeObjective(killer, quest, state);
                }
                
                // Auto-save progress
                plugin.getPlayerDataStore().setQuestState(playerId, state);
            }
        }
    }
    
    /**
     * Get MythicMob type from entity (if MythicMobs is installed)
     */
    private String getMythicMobType(org.bukkit.entity.Entity entity) {
        // Check for MythicMobs metadata
        // MythicMobs typically stores mob type in persistent data container
        try {
            // Attempt to get MythicMobs type via PDC
            org.bukkit.persistence.PersistentDataContainer pdc = entity.getPersistentDataContainer();
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("mythicmobs", "type");
            
            if (pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                return pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
            }
        } catch (Exception e) {
            // MythicMobs not installed or incompatible version
        }
        
        return null;
    }
    
    /**
     * Complete the current objective and advance to next
     */
    private void completeObjective(Player player, QuestDefinition quest, PlayerQuestState state) {
        if (quest.isLastObjective(state.getObjectiveIndex())) {
            // Quest fully completed
            state.setStatus(QuestStatus.COMPLETED);
            
            // Refresh quest marker for turn-in NPC
            if (plugin.getMarkerController() != null && quest.getGiverNpcId() != null) {
                plugin.getMarkerController().refreshMarkerForPlayerAndNpc(player, quest.getGiverNpcId());
            }
            
            plugin.getQuestUI().notifyQuestComplete(player, quest);
        } else {
            // Advance to next objective
            state.advanceToNextObjective();
            Objective nextObjective = quest.getCurrentObjective(state.getObjectiveIndex());
            plugin.getQuestUI().notifyNextObjective(player, quest, nextObjective);
        }
    }
}
