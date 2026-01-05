package com.aethor.aethorquests.tracker;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.Objective;
import com.aethor.aethorquests.model.ObjectiveType;
import com.aethor.aethorquests.model.PlayerQuestState;
import com.aethor.aethorquests.model.QuestDefinition;
import com.aethor.aethorquests.model.QuestStatus;

/**
 * Tracks TALK objectives for active quests
 */
public class TalkObjectiveTracker {
    private final AethorQuestsPlugin plugin;
    
    public TalkObjectiveTracker(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if NPC interaction completes a TALK objective
     */
    public void checkTalkObjective(Player player, String npcId) {
        UUID playerId = player.getUniqueId();
        
        // Check all active quests for this player
        List<PlayerQuestState> activeQuests = plugin.getPlayerDataStore().getActiveQuests(playerId);
        
        for (PlayerQuestState state : activeQuests) {
            QuestDefinition quest = plugin.getQuestManager().getQuest(state.getQuestId());
            if (quest == null) continue;
            
            Objective currentObjective = quest.getCurrentObjective(state.getObjectiveIndex());
            if (currentObjective == null || currentObjective.getType() != ObjectiveType.TALK) {
                continue;
            }
            
            // Check if the NPC matches the objective
            if (npcId.equals(currentObjective.getTalkNpcId())) {
                // Complete this objective immediately
                completeObjective(player, quest, state);
                
                // Save progress
                plugin.getPlayerDataStore().setQuestState(playerId, state);
            }
        }
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
