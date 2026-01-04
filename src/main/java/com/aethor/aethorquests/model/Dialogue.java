package com.aethor.aethorquests.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents dialogue that can be shown during quest interactions
 */
public class Dialogue {
    private List<String> acceptDialogue;
    private List<String> progressDialogue;
    private List<String> completionDialogue;
    
    public Dialogue() {
        this.acceptDialogue = new ArrayList<>();
        this.progressDialogue = new ArrayList<>();
        this.completionDialogue = new ArrayList<>();
    }
    
    public List<String> getAcceptDialogue() {
        return acceptDialogue;
    }
    
    public void setAcceptDialogue(List<String> acceptDialogue) {
        this.acceptDialogue = acceptDialogue;
    }
    
    public List<String> getProgressDialogue() {
        return progressDialogue;
    }
    
    public void setProgressDialogue(List<String> progressDialogue) {
        this.progressDialogue = progressDialogue;
    }
    
    public List<String> getCompletionDialogue() {
        return completionDialogue;
    }
    
    public void setCompletionDialogue(List<String> completionDialogue) {
        this.completionDialogue = completionDialogue;
    }
}
