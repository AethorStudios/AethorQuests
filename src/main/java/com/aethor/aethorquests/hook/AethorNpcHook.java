package com.aethor.aethorquests.hook;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicesManager;

import com.aethor.aethornpcs.api.AethorNpcApi;
import com.aethor.aethornpcs.api.payload.InteractPayload;
import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.PlayerQuestState;
import com.aethor.aethorquests.model.QuestDefinition;
import com.aethor.aethorquests.model.QuestStatus;

/**
 * Hooks into AethorNPCS API
 * NOTE: Since the AethorNPCS API uses payloads rather than events,
 * this hook provides the API reference and helper methods.
 * The actual NPC interaction handling may need to be implemented
 * based on how AethorNPCS exposes interaction hooks.
 */
public class AethorNpcHook {
    private final AethorQuestsPlugin plugin;
    private AethorNpcApi npcApi;
    private boolean enabled;
    
    public AethorNpcHook(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }
    
    /**
     * Attempts to load the AethorNPCS API from the ServicesManager
     * @return true if successfully loaded, false otherwise
     */
    public boolean loadApi() {
        ServicesManager servicesManager = plugin.getServer().getServicesManager();
        
        try {
            npcApi = servicesManager.load(AethorNpcApi.class);
            
            if (npcApi == null) {
                plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                plugin.getLogger().severe("  AethorNPCS API not found!");
                plugin.getLogger().severe("  AethorQuests requires AethorNPCS to function.");
                plugin.getLogger().severe("  Please install AethorNPCS and restart the server.");
                plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                return false;
            }
            
            enabled = true;
            plugin.getLogger().info("Successfully hooked into AethorNPCS API");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load AethorNPCS API", e);
            plugin.getLogger().severe("AethorQuests will be disabled.");
            return false;
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public AethorNpcApi getApi() {
        return npcApi;
    }
    
    /**
     * Handle NPC interaction via API callback
     * This method should be registered with the AethorNPCS API
     */
    public void handleNpcInteraction(Player player, String npcId, InteractPayload.ClickType clickType) {
        // Only handle RIGHT_CLICK interactions for quest menus
        if (clickType != InteractPayload.ClickType.RIGHT_CLICK) {
            return;
        }
        
        if (plugin.isDebug()) {
            plugin.getLogger().info("Player " + player.getName() + " interacted with NPC: " + npcId);
        }
        
        // Check if this NPC has any associated quests
        List<QuestDefinition> availableQuests = plugin.getQuestManager()
                .getQuestsByGiverNpcId(npcId);
        
        if (availableQuests.isEmpty()) {
            // No quests associated with this NPC
            return;
        }
        
        // Check if player is turning in a quest
        boolean handledTurnIn = false;
        for (QuestDefinition quest : availableQuests) {
            PlayerQuestState state = plugin.getPlayerDataStore()
                    .getQuestState(player.getUniqueId(), quest.getId());
            
            if (state != null && state.getStatus() == QuestStatus.COMPLETED) {
                // Player can turn in this quest
                handledTurnIn = true;
                plugin.getQuestUI().showQuestTurnIn(player, quest);
                return;
            }
        }
        
        // Show quest menu for this NPC
        plugin.getQuestUI().showNpcQuestMenu(player, npcId, availableQuests);
        
        // Also check TALK objectives
        if (plugin.getTalkTracker() != null) {
            plugin.getTalkTracker().checkTalkObjective(player, npcId);
        }
    }
    
    /**
     * Check if an NPC exists by ID
     */
    public boolean npcExists(String npcId) {
        if (!enabled || npcApi == null) {
            return false;
        }
        
        try {
            return npcApi.getNpc(npcId) != null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking NPC existence: " + npcId, e);
            return false;
        }
    }
    
    /**
     * Get the display name of an NPC.
     */
    public String getNpcName(String npcId) {
        if (!enabled || npcApi == null) {
            return "NPC";
        }
        
        try {
            var npc = npcApi.getNpc(npcId);
            if (npc.isPresent()) {
                return npc.get().getName();
            }
            return "NPC";
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting NPC name: " + npcId, e);
            return "NPC";
        }
    }
}
