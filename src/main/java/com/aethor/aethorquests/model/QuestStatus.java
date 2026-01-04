package com.aethor.aethorquests.model;

/**
 * Enum representing the status of a player's quest
 */
public enum QuestStatus {
    /**
     * Quest has not been started by the player
     */
    NOT_STARTED,
    
    /**
     * Quest is currently active and in progress
     */
    ACTIVE,
    
    /**
     * All objectives completed, ready to turn in
     */
    COMPLETED,
    
    /**
     * Quest has been turned in and rewards claimed
     */
    TURNED_IN
}
