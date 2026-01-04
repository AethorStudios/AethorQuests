package com.aethor.aethorquests.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents quest requirements that must be met before a player can accept a quest
 */
public class QuestRequirements {
    private int minLevel;
    private List<String> requiredQuestsCompleted;
    
    public QuestRequirements() {
        this.minLevel = 1;
        this.requiredQuestsCompleted = new ArrayList<>();
    }
    
    public int getMinLevel() {
        return minLevel;
    }
    
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }
    
    public List<String> getRequiredQuestsCompleted() {
        return requiredQuestsCompleted;
    }
    
    public void setRequiredQuestsCompleted(List<String> requiredQuestsCompleted) {
        this.requiredQuestsCompleted = requiredQuestsCompleted;
    }
}
