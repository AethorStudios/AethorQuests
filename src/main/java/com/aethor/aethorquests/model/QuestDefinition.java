package com.aethor.aethorquests.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete quest definition loaded from quests.yml
 */
public class QuestDefinition {
    private final String id;
    private String title;
    private List<String> description;
    private String giverNpcId;
    private QuestRequirements requirements;
    private List<Objective> objectives;
    private Reward rewards;
    private Dialogue dialogue;
    
    public QuestDefinition(String id) {
        this.id = id;
        this.description = new ArrayList<>();
        this.requirements = new QuestRequirements();
        this.objectives = new ArrayList<>();
        this.rewards = new Reward();
        this.dialogue = new Dialogue();
    }
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<String> getDescription() {
        return description;
    }
    
    public void setDescription(List<String> description) {
        this.description = description;
    }
    
    public String getGiverNpcId() {
        return giverNpcId;
    }
    
    public void setGiverNpcId(String giverNpcId) {
        this.giverNpcId = giverNpcId;
    }
    
    public QuestRequirements getRequirements() {
        return requirements;
    }
    
    public void setRequirements(QuestRequirements requirements) {
        this.requirements = requirements;
    }
    
    public List<Objective> getObjectives() {
        return objectives;
    }
    
    public void setObjectives(List<Objective> objectives) {
        this.objectives = objectives;
    }
    
    public void addObjective(Objective objective) {
        this.objectives.add(objective);
    }
    
    public Reward getRewards() {
        return rewards;
    }
    
    public void setRewards(Reward rewards) {
        this.rewards = rewards;
    }
    
    public Dialogue getDialogue() {
        return dialogue;
    }
    
    public void setDialogue(Dialogue dialogue) {
        this.dialogue = dialogue;
    }
    
    /**
     * Get the current objective for a player based on their objective index
     */
    public Objective getCurrentObjective(int objectiveIndex) {
        if (objectiveIndex < 0 || objectiveIndex >= objectives.size()) {
            return null;
        }
        return objectives.get(objectiveIndex);
    }
    
    /**
     * Check if all objectives are completed
     */
    public boolean isLastObjective(int objectiveIndex) {
        return objectiveIndex >= objectives.size() - 1;
    }
}
