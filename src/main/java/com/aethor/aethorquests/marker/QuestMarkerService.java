package com.aethor.aethorquests.marker;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.PlayerQuestState;
import com.aethor.aethorquests.model.QuestDefinition;
import com.aethor.aethorquests.model.QuestStatus;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Service for computing quest marker states for NPCs based on player quest progress.
 */
public class QuestMarkerService {
    
    private final AethorQuestsPlugin plugin;
    
    public QuestMarkerService(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Computes the marker state for a given player and NPC.
     * Priority: TURNIN > AVAILABLE > NONE
     *
     * @param player The player
     * @param npcId The NPC ID
     * @return The marker state
     */
    public MarkerState computeMarkerState(Player player, String npcId) {
        List<QuestDefinition> quests = plugin.getQuestManager().getQuestsByGiverNpcId(npcId);
        
        if (quests.isEmpty()) {
            return MarkerState.NONE;
        }
        
        MarkerState highestState = MarkerState.NONE;
        
        for (QuestDefinition quest : quests) {
            PlayerQuestState state = plugin.getPlayerDataStore()
                    .getQuestState(player.getUniqueId(), quest.getId());
            
            if (state == null || state.getStatus() == QuestStatus.NOT_STARTED) {
                // Check if player can accept this quest
                if (canPlayerAcceptQuest(player, quest)) {
                    highestState = MarkerState.highest(highestState, MarkerState.AVAILABLE);
                }
            } else if (state.getStatus() == QuestStatus.COMPLETED) {
                // Quest is ready to turn in - highest priority
                return MarkerState.TURNIN;
            }
            // ACTIVE and TURNED_IN states don't show markers
        }
        
        return highestState;
    }
    
    /**
     * Checks if a player can accept a quest based on requirements.
     *
     * @param player The player
     * @param quest The quest
     * @return true if the player meets all requirements
     */
    private boolean canPlayerAcceptQuest(Player player, QuestDefinition quest) {
        if (quest.getRequirements() == null) {
            return true;
        }
        
        // Check level requirement
        int minLevel = quest.getRequirements().getMinLevel();
        if (player.getLevel() < minLevel) {
            return false;
        }
        
        // Check required quests completed
        List<String> requiredQuests = quest.getRequirements().getRequiredQuestsCompleted();
        if (requiredQuests != null && !requiredQuests.isEmpty()) {
            for (String requiredQuestId : requiredQuests) {
                PlayerQuestState reqState = plugin.getPlayerDataStore()
                        .getQuestState(player.getUniqueId(), requiredQuestId);
                
                if (reqState == null || reqState.getStatus() != QuestStatus.TURNED_IN) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
