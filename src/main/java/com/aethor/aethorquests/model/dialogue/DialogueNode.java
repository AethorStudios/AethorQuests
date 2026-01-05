package com.aethor.aethorquests.model.dialogue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a node in a quest dialogue tree.
 * Each node contains one or more lines of NPC dialogue and optional player response choices.
 */
public class DialogueNode {
    private final String id;
    private final List<String> lines;
    private final List<DialogueOption> options;
    private final String nextNodeId; // Auto-advance to this node if no options are present
    
    /**
     * Creates a new dialogue node with no options (linear dialogue).
     *
     * @param id Unique identifier for this node
     * @param lines List of dialogue lines to display progressively
     */
    public DialogueNode(String id, List<String> lines) {
        this(id, lines, Collections.emptyList(), null);
    }
    
    /**
     * Creates a new dialogue node with auto-advance.
     *
     * @param id Unique identifier for this node
     * @param lines List of dialogue lines to display progressively
     * @param nextNodeId ID of the next node to automatically transition to
     */
    public DialogueNode(String id, List<String> lines, String nextNodeId) {
        this(id, lines, Collections.emptyList(), nextNodeId);
    }
    
    /**
     * Creates a new dialogue node with player choices.
     *
     * @param id Unique identifier for this node
     * @param lines List of dialogue lines to display progressively
     * @param options List of player response options
     */
    public DialogueNode(String id, List<String> lines, List<DialogueOption> options) {
        this(id, lines, options, null);
    }
    
    /**
     * Creates a new dialogue node with full configuration.
     *
     * @param id Unique identifier for this node
     * @param lines List of dialogue lines to display progressively
     * @param options List of player response options (empty for linear dialogue)
     * @param nextNodeId ID of the next node to transition to when no options are present
     */
    public DialogueNode(String id, List<String> lines, List<DialogueOption> options, String nextNodeId) {
        this.id = id;
        this.lines = new ArrayList<>(lines);
        this.options = new ArrayList<>(options);
        this.nextNodeId = nextNodeId;
    }
    
    public String getId() {
        return id;
    }
    
    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }
    
    public List<DialogueOption> getOptions() {
        return Collections.unmodifiableList(options);
    }
    
    public boolean hasOptions() {
        return !options.isEmpty();
    }
    
    public String getNextNodeId() {
        return nextNodeId;
    }
    
    public boolean hasAutoAdvance() {
        return nextNodeId != null && !nextNodeId.isEmpty();
    }
    
    public DialogueOption getOptionById(int id) {
        return options.stream()
                .filter(opt -> opt.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
