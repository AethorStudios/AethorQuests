package com.aethor.aethorquests.tracker;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 * Tracks VISIT objectives using a periodic task
 */
public class VisitObjectiveTracker extends BukkitRunnable {
    private final AethorQuestsPlugin plugin;
    
    public VisitObjectiveTracker(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        // Check all online players with active quests
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            checkPlayerVisitObjectives(player);
        }
    }
    
    /**
     * Check if a player has reached any visit objectives
     */
    private void checkPlayerVisitObjectives(Player player) {
        UUID playerId = player.getUniqueId();
        List<PlayerQuestState> activeQuests = plugin.getPlayerDataStore().getActiveQuests(playerId);
        
        for (PlayerQuestState state : activeQuests) {
            QuestDefinition quest = plugin.getQuestManager().getQuest(state.getQuestId());
            if (quest == null) continue;
            
            Objective currentObjective = quest.getCurrentObjective(state.getObjectiveIndex());
            if (currentObjective == null || currentObjective.getType() != ObjectiveType.VISIT) {
                continue;
            }
            
            // Check if player is at the location
            if (isAtLocation(player, currentObjective)) {
                // Complete this objective
                completeObjective(player, quest, state);
                
                // Save progress
                plugin.getPlayerDataStore().setQuestState(playerId, state);
            }
        }
    }
    
    /**
     * Check if player is within range of the objective location
     */
    private boolean isAtLocation(Player player, Objective objective) {
        Location targetLocation = objective.getVisitLocation(plugin.getServer());
        if (targetLocation == null) return false;
        
        Location playerLocation = player.getLocation();
        
        // Check world
        if (!playerLocation.getWorld().getName().equals(targetLocation.getWorld().getName())) {
            return false;
        }
        
        // Check distance
        double distance = playerLocation.distance(targetLocation);
        return distance <= objective.getVisitRadius();
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
    
    /**
     * Start the periodic checking task
     */
    public void start() {
        long periodTicks = plugin.getConfig().getLong("visitCheck.periodTicks", 10);
        this.runTaskTimer(plugin, periodTicks, periodTicks);
    }
}
