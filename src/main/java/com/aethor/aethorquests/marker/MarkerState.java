package com.aethor.aethorquests.marker;

/**
 * Represents the state of a quest marker above an NPC.
 * Priority: TURNIN > AVAILABLE > NONE
 */
public enum MarkerState {
    /**
     * No quest marker should be shown
     */
    NONE(0),
    
    /**
     * Quest is available to accept (green !)
     */
    AVAILABLE(1),
    
    /**
     * Quest is ready to turn in (yellow ?)
     */
    TURNIN(2);
    
    private final int priority;
    
    MarkerState(int priority) {
        this.priority = priority;
    }
    
    public int getPriority() {
        return priority;
    }
    
    /**
     * Gets the marker with the highest priority
     */
    public static MarkerState highest(MarkerState a, MarkerState b) {
        return a.priority > b.priority ? a : b;
    }
}
