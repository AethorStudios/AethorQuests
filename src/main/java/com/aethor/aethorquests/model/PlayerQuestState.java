package com.aethor.aethorquests.model;

/**
 * Represents a player's state and progress for a specific quest
 */
public class PlayerQuestState {
    private final String questId;
    private QuestStatus status;
    private int objectiveIndex;
    private int objectiveProgress;
    private long acceptedTimestamp;
    private long completedTimestamp;
    private long turnedInTimestamp;
    
    public PlayerQuestState(String questId) {
        this.questId = questId;
        this.status = QuestStatus.NOT_STARTED;
        this.objectiveIndex = 0;
        this.objectiveProgress = 0;
        this.acceptedTimestamp = 0;
        this.completedTimestamp = 0;
        this.turnedInTimestamp = 0;
    }
    
    public String getQuestId() {
        return questId;
    }
    
    public QuestStatus getStatus() {
        return status;
    }
    
    public void setStatus(QuestStatus status) {
        this.status = status;
        
        // Update timestamps
        long now = System.currentTimeMillis();
        if (status == QuestStatus.ACTIVE && acceptedTimestamp == 0) {
            acceptedTimestamp = now;
        } else if (status == QuestStatus.COMPLETED && completedTimestamp == 0) {
            completedTimestamp = now;
        } else if (status == QuestStatus.TURNED_IN && turnedInTimestamp == 0) {
            turnedInTimestamp = now;
        }
    }
    
    public int getObjectiveIndex() {
        return objectiveIndex;
    }
    
    public void setObjectiveIndex(int objectiveIndex) {
        this.objectiveIndex = objectiveIndex;
    }
    
    public int getObjectiveProgress() {
        return objectiveProgress;
    }
    
    public void setObjectiveProgress(int objectiveProgress) {
        this.objectiveProgress = objectiveProgress;
    }
    
    public void incrementProgress(int amount) {
        this.objectiveProgress += amount;
    }
    
    public void resetProgress() {
        this.objectiveProgress = 0;
    }
    
    public void advanceToNextObjective() {
        this.objectiveIndex++;
        this.objectiveProgress = 0;
    }
    
    public long getAcceptedTimestamp() {
        return acceptedTimestamp;
    }
    
    public void setAcceptedTimestamp(long acceptedTimestamp) {
        this.acceptedTimestamp = acceptedTimestamp;
    }
    
    public long getCompletedTimestamp() {
        return completedTimestamp;
    }
    
    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }
    
    public long getTurnedInTimestamp() {
        return turnedInTimestamp;
    }
    
    public void setTurnedInTimestamp(long turnedInTimestamp) {
        this.turnedInTimestamp = turnedInTimestamp;
    }
    
    public boolean isActive() {
        return status == QuestStatus.ACTIVE;
    }
    
    public boolean isCompleted() {
        return status == QuestStatus.COMPLETED;
    }
    
    public boolean isTurnedIn() {
        return status == QuestStatus.TURNED_IN;
    }
}
