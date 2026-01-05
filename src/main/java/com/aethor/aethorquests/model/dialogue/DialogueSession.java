package com.aethor.aethorquests.model.dialogue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks an active dialogue session for a player.
 * Manages progression through dialogue nodes, line advancement, and choice selection.
 */
public class DialogueSession {
    private final UUID playerId;
    private final String npcId;
    private final String npcName;
    private final Map<String, DialogueNode> nodes;
    
    private DialogueNode currentNode;
    private int currentLineIndex;
    private boolean waitingForChoice;
    private long lastAdvanceTime;
    
    // Metadata for quest context
    private String questId;
    private DialogueContext context;
    
    /**
     * Creates a new dialogue session.
     *
     * @param playerId UUID of the player in this dialogue
     * @param npcId ID of the NPC being conversed with
     * @param npcName Display name of the NPC
     * @param nodes Map of all available dialogue nodes (nodeId -> DialogueNode)
     * @param startNodeId ID of the starting dialogue node
     */
    public DialogueSession(UUID playerId, String npcId, String npcName, 
                          Map<String, DialogueNode> nodes, String startNodeId) {
        this.playerId = playerId;
        this.npcId = npcId;
        this.npcName = npcName;
        this.nodes = new HashMap<>(nodes);
        this.currentNode = nodes.get(startNodeId);
        this.currentLineIndex = 0;
        this.waitingForChoice = false;
        this.lastAdvanceTime = System.currentTimeMillis();
        this.context = DialogueContext.QUEST_INTRO;
        
        if (currentNode == null) {
            throw new IllegalArgumentException("Start node '" + startNodeId + "' not found in dialogue tree");
        }
    }
    
    /**
     * Gets the current line to display.
     *
     * @return The current dialogue line, or null if all lines have been shown
     */
    public String getCurrentLine() {
        if (currentNode == null || currentLineIndex >= currentNode.getLines().size()) {
            return null;
        }
        return currentNode.getLines().get(currentLineIndex);
    }
    
    /**
     * Advances to the next line in the current node.
     *
     * @return true if advanced successfully, false if no more lines in this node
     */
    public boolean advanceLine() {
        if (currentNode == null) {
            return false;
        }
        
        // Debounce rapid advancement (minimum 200ms between advances)
        long now = System.currentTimeMillis();
        if (now - lastAdvanceTime < 200) {
            return false;
        }
        lastAdvanceTime = now;
        
        currentLineIndex++;
        
        // Check if we've reached the end of this node's lines
        if (currentLineIndex >= currentNode.getLines().size()) {
            // If there are options, wait for player choice
            if (currentNode.hasOptions()) {
                waitingForChoice = true;
                return false;
            }
            
            // If there's auto-advance, transition to next node
            if (currentNode.hasAutoAdvance()) {
                return transitionToNode(currentNode.getNextNodeId());
            }
            
            // Otherwise, dialogue ends
            return false;
        }
        
        return true;
    }
    
    /**
     * Transitions to a different dialogue node.
     *
     * @param nodeId ID of the node to transition to
     * @return true if transition was successful, false if node not found
     */
    public boolean transitionToNode(String nodeId) {
        if (nodeId == null) {
            currentNode = null;
            return false;
        }
        
        DialogueNode nextNode = nodes.get(nodeId);
        if (nextNode == null) {
            return false;
        }
        
        currentNode = nextNode;
        currentLineIndex = 0;
        waitingForChoice = false;
        return true;
    }
    
    /**
     * Selects a dialogue option and transitions to the next node.
     *
     * @param optionId The ID of the selected option (1-9)
     * @return true if selection was valid and transition succeeded
     */
    public boolean selectOption(int optionId) {
        if (!waitingForChoice || currentNode == null) {
            return false;
        }
        
        DialogueOption option = currentNode.getOptionById(optionId);
        if (option == null) {
            return false;
        }
        
        waitingForChoice = false;
        
        // If option has no next node, dialogue ends
        if (option.getNextNodeId() == null) {
            currentNode = null;
            return true;
        }
        
        return transitionToNode(option.getNextNodeId());
    }
    
    /**
     * Checks if the dialogue has ended.
     *
     * @return true if dialogue is complete
     */
    public boolean isComplete() {
        return currentNode == null || 
               (currentLineIndex >= currentNode.getLines().size() && 
                !currentNode.hasOptions() && 
                !currentNode.hasAutoAdvance());
    }
    
    /**
     * Checks if all lines have been shown and choices are available.
     *
     * @return true if waiting for player to select an option
     */
    public boolean isWaitingForChoice() {
        return waitingForChoice;
    }
    
    // Getters and setters
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getNpcId() {
        return npcId;
    }
    
    public String getNpcName() {
        return npcName;
    }
    
    public DialogueNode getCurrentNode() {
        return currentNode;
    }
    
    public int getCurrentLineIndex() {
        return currentLineIndex;
    }
    
    public String getQuestId() {
        return questId;
    }
    
    public void setQuestId(String questId) {
        this.questId = questId;
    }
    
    public DialogueContext getContext() {
        return context;
    }
    
    public void setContext(DialogueContext context) {
        this.context = context;
    }
    
    /**
     * Context enum for different dialogue scenarios.
     */
    public enum DialogueContext {
        QUEST_INTRO,      // Initial quest offer
        QUEST_PROGRESS,   // Quest in progress
        QUEST_COMPLETE,   // Quest ready to turn in
        GENERIC           // Non-quest dialogue
    }
}
