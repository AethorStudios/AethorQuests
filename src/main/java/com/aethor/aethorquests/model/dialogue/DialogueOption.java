package com.aethor.aethorquests.model.dialogue;

/**
 * Represents a player choice in a dialogue conversation.
 * Each option has an ID, display text, and the ID of the next dialogue node to transition to.
 */
public class DialogueOption {
    private final int id;
    private final String text;
    private final String nextNodeId;
    private final String command; // Optional command to execute (e.g., "accept_quest", "decline_quest")
    
    /**
     * Creates a new dialogue option.
     *
     * @param id Unique identifier for this option (1-9 for keyboard input)
     * @param text The text displayed to the player
     * @param nextNodeId The ID of the dialogue node to transition to (null if ending dialogue)
     */
    public DialogueOption(int id, String text, String nextNodeId) {
        this(id, text, nextNodeId, null);
    }
    
    /**
     * Creates a new dialogue option with an associated command.
     *
     * @param id Unique identifier for this option (1-9 for keyboard input)
     * @param text The text displayed to the player
     * @param nextNodeId The ID of the dialogue node to transition to (null if ending dialogue)
     * @param command Optional command to execute when this option is selected
     */
    public DialogueOption(int id, String text, String nextNodeId, String command) {
        this.id = id;
        this.text = text;
        this.nextNodeId = nextNodeId;
        this.command = command;
    }
    
    public int getId() {
        return id;
    }
    
    public String getText() {
        return text;
    }
    
    public String getNextNodeId() {
        return nextNodeId;
    }
    
    public String getCommand() {
        return command;
    }
    
    public boolean hasCommand() {
        return command != null && !command.isEmpty();
    }
}
