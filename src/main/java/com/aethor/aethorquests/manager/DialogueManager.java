package com.aethor.aethorquests.manager;

import com.aethor.aethorquests.model.dialogue.DialogueNode;
import com.aethor.aethorquests.model.dialogue.DialogueOption;
import com.aethor.aethorquests.model.dialogue.DialogueSession;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages active dialogue sessions for players.
 * Ensures only one dialogue is active per player at a time.
 */
public class DialogueManager {
    
    private final Map<UUID, DialogueSession> activeSessions;
    
    public DialogueManager() {
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Starts a new dialogue session for a player.
     * If the player already has an active session, it will be replaced.
     *
     * @param playerId UUID of the player
     * @param npcId ID of the NPC
     * @param npcName Display name of the NPC
     * @param nodes Map of all dialogue nodes
     * @param startNodeId ID of the starting node
     * @return The created DialogueSession
     */
    public DialogueSession startDialogue(UUID playerId, String npcId, String npcName,
                                        Map<String, DialogueNode> nodes, String startNodeId) {
        DialogueSession session = new DialogueSession(playerId, npcId, npcName, nodes, startNodeId);
        activeSessions.put(playerId, session);
        return session;
    }
    
    /**
     * Creates a simple linear dialogue session from a list of lines.
     * Useful for converting old dialogue format to new system.
     *
     * @param playerId UUID of the player
     * @param npcId ID of the NPC
     * @param npcName Display name of the NPC
     * @param lines List of dialogue lines
     * @return The created DialogueSession
     */
    public DialogueSession startSimpleDialogue(UUID playerId, String npcId, String npcName, List<String> lines) {
        Map<String, DialogueNode> nodes = new HashMap<>();
        DialogueNode node = new DialogueNode("main", lines);
        nodes.put("main", node);
        
        return startDialogue(playerId, npcId, npcName, nodes, "main");
    }
    
    /**
     * Creates a dialogue session with a choice at the end.
     * Useful for quest accept/decline scenarios.
     *
     * @param playerId UUID of the player
     * @param npcId ID of the NPC
     * @param npcName Display name of the NPC
     * @param lines List of dialogue lines
     * @param options List of dialogue options to present after the lines
     * @return The created DialogueSession
     */
    public DialogueSession startDialogueWithChoice(UUID playerId, String npcId, String npcName,
                                                   List<String> lines, List<DialogueOption> options) {
        Map<String, DialogueNode> nodes = new HashMap<>();
        DialogueNode node = new DialogueNode("main", lines, options);
        nodes.put("main", node);
        
        return startDialogue(playerId, npcId, npcName, nodes, "main");
    }
    
    /**
     * Gets the active dialogue session for a player.
     *
     * @param playerId UUID of the player
     * @return The active DialogueSession, or null if none exists
     */
    public DialogueSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }
    
    /**
     * Gets the active dialogue session for a player.
     *
     * @param player The player
     * @return The active DialogueSession, or null if none exists
     */
    public DialogueSession getSession(Player player) {
        return getSession(player.getUniqueId());
    }
    
    /**
     * Checks if a player has an active dialogue session.
     *
     * @param playerId UUID of the player
     * @return true if the player has an active dialogue
     */
    public boolean hasActiveSession(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }
    
    /**
     * Checks if a player has an active dialogue session.
     *
     * @param player The player
     * @return true if the player has an active dialogue
     */
    public boolean hasActiveSession(Player player) {
        return hasActiveSession(player.getUniqueId());
    }
    
    /**
     * Ends a player's dialogue session.
     *
     * @param playerId UUID of the player
     */
    public void endSession(UUID playerId) {
        activeSessions.remove(playerId);
    }
    
    /**
     * Ends a player's dialogue session.
     *
     * @param player The player
     */
    public void endSession(Player player) {
        endSession(player.getUniqueId());
    }
    
    /**
     * Advances the dialogue for a player by one line.
     *
     * @param playerId UUID of the player
     * @return true if advanced successfully, false if dialogue ended or player has no active session
     */
    public boolean advanceDialogue(UUID playerId) {
        DialogueSession session = activeSessions.get(playerId);
        if (session == null) {
            return false;
        }
        
        boolean advanced = session.advanceLine();
        
        // If dialogue is complete, end the session
        if (session.isComplete()) {
            endSession(playerId);
            return false;
        }
        
        return advanced;
    }
    
    /**
     * Selects a dialogue option for a player.
     *
     * @param playerId UUID of the player
     * @param optionId The ID of the selected option (1-9)
     * @return true if selection was valid
     */
    public boolean selectOption(UUID playerId, int optionId) {
        DialogueSession session = activeSessions.get(playerId);
        if (session == null) {
            return false;
        }
        
        boolean selected = session.selectOption(optionId);
        
        // If dialogue is complete after selection, end the session
        if (session.isComplete()) {
            endSession(playerId);
        }
        
        return selected;
    }
    
    /**
     * Cleans up all active sessions.
     * Should be called on plugin disable.
     */
    public void cleanup() {
        activeSessions.clear();
    }
    
    /**
     * Gets the number of active dialogue sessions.
     *
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
