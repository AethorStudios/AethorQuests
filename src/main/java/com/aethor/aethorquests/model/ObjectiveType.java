package com.aethor.aethorquests.model;

/**
 * Enum representing the different types of quest objectives
 */
public enum ObjectiveType {
    /**
     * Kill a specific number of entities (vanilla or MythicMobs)
     */
    KILL,
    
    /**
     * Talk to a specific NPC (AethorNPCS)
     */
    TALK,
    
    /**
     * Collect a specific number of items
     */
    COLLECT,
    
    /**
     * Visit a specific location
     */
    VISIT
}
