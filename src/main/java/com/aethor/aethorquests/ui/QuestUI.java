package com.aethor.aethorquests.ui;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.*;
import com.aethor.aethorquests.model.dialogue.DialogueOption;
import com.aethor.aethorquests.model.dialogue.DialogueSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides chat-based UI using Adventure text components
 */
public class QuestUI {
    private final AethorQuestsPlugin plugin;
    
    public QuestUI(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Show quest menu for an NPC
     */
    public void showNpcQuestMenu(Player player, String npcId, List<QuestDefinition> quests) {
        UUID playerId = player.getUniqueId();
        
        // Get NPC name for dialogue
        String npcName = plugin.getNpcHook().getNpcName(npcId);
        if (npcName == null) {
            npcName = "NPC";
        }
        
        // Show dialogue for the first quest using progressive dialogue system
        if (!quests.isEmpty()) {
            QuestDefinition firstQuest = quests.get(0);
            PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, firstQuest.getId());
            QuestStatus status = state != null ? state.getStatus() : QuestStatus.NOT_STARTED;
            
            // Start progressive dialogue if quest has dialogue configured
            if (firstQuest.getDialogue() != null) {
                startQuestDialogue(player, npcId, npcName, firstQuest, status);
                return; // Dialogue system will handle the rest
            }
        }
        
        // Fallback to old menu system if no dialogue configured
        showQuestMenuLegacy(player, npcId, quests);
    }
    
    /**
     * Starts a progressive dialogue session for a quest.
     *
     * @param player The player
     * @param npcId ID of the NPC
     * @param npcName Display name of the NPC
     * @param quest The quest
     * @param status The current quest status for this player
     */
    private void startQuestDialogue(Player player, String npcId, String npcName, 
                                   QuestDefinition quest, QuestStatus status) {
        List<String> dialogueLines;
        List<DialogueOption> options = new ArrayList<>();
        
        // Determine which dialogue to show based on quest status
        if (status == QuestStatus.NOT_STARTED || status == null) {
            dialogueLines = quest.getDialogue().getAcceptDialogue();
            
            // Add accept/decline options
            options.add(new DialogueOption(1, "&aAccept the quest", null, "accept_quest"));
            options.add(new DialogueOption(2, "&cDecline", null, "decline_quest"));
            
        } else if (status == QuestStatus.ACTIVE) {
            dialogueLines = quest.getDialogue().getProgressDialogue();
            // No options for in-progress dialogue, it just closes
            
        } else if (status == QuestStatus.COMPLETED) {
            dialogueLines = quest.getDialogue().getCompletionDialogue();
            
            // Add turn-in option
            options.add(new DialogueOption(1, "&aTurn in quest", null, "turn_in_quest"));
            
        } else {
            // Quest already completed
            return;
        }
        
        // If no dialogue lines configured, skip
        if (dialogueLines == null || dialogueLines.isEmpty()) {
            return;
        }
        
        // Start the dialogue session
        DialogueSession session = plugin.getDialogueManager()
                .startDialogueWithChoice(player.getUniqueId(), npcId, npcName, dialogueLines, options);
        
        session.setQuestId(quest.getId());
        
        // Set context based on status
        if (status == QuestStatus.NOT_STARTED || status == null) {
            session.setContext(DialogueSession.DialogueContext.QUEST_INTRO);
        } else if (status == QuestStatus.ACTIVE) {
            session.setContext(DialogueSession.DialogueContext.QUEST_PROGRESS);
        } else if (status == QuestStatus.COMPLETED) {
            session.setContext(DialogueSession.DialogueContext.QUEST_COMPLETE);
        } else {
            session.setContext(DialogueSession.DialogueContext.GENERIC);
        }
        
        // Render the dialogue start and first line
        DialogueRenderer renderer = new DialogueRenderer(plugin.getConfig());
        renderer.renderDialogueStart(player, npcName);
        renderer.renderCurrentLine(player, session);
    }
    
    /**
     * Shows the legacy quest menu (non-progressive dialogue).
     */
    private void showQuestMenuLegacy(Player player, String npcId, List<QuestDefinition> quests) {
        UUID playerId = player.getUniqueId();
        
        // Header
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━ ", NamedTextColor.GRAY)
                .append(Component.text("QUESTS", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ━━━━━━━━", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
        
        if (quests.isEmpty()) {
            player.sendMessage(Component.text("This NPC has no quests available.", NamedTextColor.YELLOW));
            return;
        }
        
        for (QuestDefinition quest : quests) {
            PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, quest.getId());
            QuestStatus status = state != null ? state.getStatus() : QuestStatus.NOT_STARTED;
            
            // Quest title with status indicator
            Component statusIcon = getStatusIcon(status);
            Component titleLine = statusIcon
                    .append(Component.text(" " + quest.getTitle(), getStatusColor(status)));
            
            // Check requirements
            boolean meetsRequirements = checkRequirements(player, quest);
            
            if (status == QuestStatus.NOT_STARTED && meetsRequirements) {
                // Show accept button
                Component acceptButton = Component.text("[Accept]", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Click to accept this quest")))
                        .clickEvent(ClickEvent.runCommand("/quest accept " + quest.getId()));
                
                Component viewButton = Component.text(" [View]", NamedTextColor.AQUA)
                        .hoverEvent(HoverEvent.showText(Component.text("Click to view quest details")))
                        .clickEvent(ClickEvent.runCommand("/quest view " + quest.getId()));
                
                player.sendMessage(titleLine.append(Component.text(" - ")).append(acceptButton).append(viewButton));
                
            } else if (status == QuestStatus.NOT_STARTED && !meetsRequirements) {
                // Locked
                player.sendMessage(titleLine.append(Component.text(" [LOCKED]", NamedTextColor.RED))
                        .hoverEvent(HoverEvent.showText(Component.text("Requirements not met"))));
                
            } else if (status == QuestStatus.ACTIVE) {
                // In progress
                Objective currentObj = quest.getCurrentObjective(state.getObjectiveIndex());
                String progressText = currentObj != null ? 
                        " - " + currentObj.getDescription() + " (" + state.getObjectiveProgress() + "/" + getObjectiveRequirement(currentObj) + ")" 
                        : " - In Progress";
                
                player.sendMessage(titleLine.append(Component.text(progressText, NamedTextColor.YELLOW)));
                
            } else if (status == QuestStatus.COMPLETED) {
                // Ready to turn in
                Component turnInButton = Component.text(" [Turn In]", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Click to complete this quest")))
                        .clickEvent(ClickEvent.runCommand("/quest turnin " + quest.getId()));
                
                player.sendMessage(titleLine.append(turnInButton));
                
            } else if (status == QuestStatus.TURNED_IN) {
                // Already completed
                player.sendMessage(titleLine.append(Component.text(" [Completed]", NamedTextColor.DARK_GRAY)));
            }
        }
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
    }
    
    /**
     * Show quest details
     */
    public void showQuestDetails(Player player, QuestDefinition quest) {
        UUID playerId = player.getUniqueId();
        PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, quest.getId());
        QuestStatus status = state != null ? state.getStatus() : QuestStatus.NOT_STARTED;
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━ ", NamedTextColor.GRAY)
                .append(Component.text(quest.getTitle(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ━━━━━━━━", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
        
        // Description
        for (String line : quest.getDescription()) {
            player.sendMessage(Component.text(line.replace("&", "§")));
        }
        player.sendMessage(Component.empty());
        
        // Objectives
        player.sendMessage(Component.text("Objectives:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        for (int i = 0; i < quest.getObjectives().size(); i++) {
            Objective obj = quest.getObjectives().get(i);
            
            Component bullet;
            if (state != null && i < state.getObjectiveIndex()) {
                bullet = Component.text(" ✓ ", NamedTextColor.GREEN);
            } else if (state != null && i == state.getObjectiveIndex()) {
                bullet = Component.text(" ➤ ", NamedTextColor.YELLOW);
            } else {
                bullet = Component.text(" ○ ", NamedTextColor.GRAY);
            }
            
            player.sendMessage(bullet.append(Component.text(obj.getDescription(), NamedTextColor.WHITE)));
        }
        player.sendMessage(Component.empty());
        
        // Rewards
        Reward rewards = quest.getRewards();
        player.sendMessage(Component.text("Rewards:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        if (rewards.getXp() > 0) {
            player.sendMessage(Component.text(" • ", NamedTextColor.GRAY)
                    .append(Component.text(rewards.getXp() + " XP", NamedTextColor.GREEN)));
        }
        if (rewards.getMoney() > 0) {
            player.sendMessage(Component.text(" • ", NamedTextColor.GRAY)
                    .append(Component.text("$" + rewards.getMoney(), NamedTextColor.GOLD)));
        }
        player.sendMessage(Component.empty());
        
        // Action buttons
        if (status == QuestStatus.NOT_STARTED && checkRequirements(player, quest)) {
            Component acceptButton = Component.text("[Accept Quest]", NamedTextColor.GREEN, TextDecoration.BOLD)
                    .hoverEvent(HoverEvent.showText(Component.text("Click to accept")))
                    .clickEvent(ClickEvent.runCommand("/quest accept " + quest.getId()));
            player.sendMessage(acceptButton);
        }
        
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
    }
    
    /**
     * Show quest turn-in confirmation
     */
    public void showQuestTurnIn(Player player, QuestDefinition quest) {
        // Show completion dialogue if available
        if (quest.getDialogue() != null) {
            List<String> dialogue = quest.getDialogue().getCompletionDialogue();
            if (dialogue != null && !dialogue.isEmpty()) {
                player.sendMessage(Component.empty());
                for (String line : dialogue) {
                    // Replace color codes (& to §)
                    player.sendMessage(Component.text(line.replace("&", "§")));
                }
                player.sendMessage(Component.empty());
            }
        }
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Quest Complete!", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text(quest.getTitle(), NamedTextColor.YELLOW));
        player.sendMessage(Component.empty());
        
        Component turnInButton = Component.text("[Turn In Quest]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Click to claim rewards")))
                .clickEvent(ClickEvent.runCommand("/quest turnin " + quest.getId()));
        
        player.sendMessage(turnInButton);
        player.sendMessage(Component.empty());
    }
    
    /**
     * Show player's active quests (journal)
     */
    public void showQuestJournal(Player player) {
        UUID playerId = player.getUniqueId();
        List<PlayerQuestState> activeQuests = plugin.getPlayerDataStore().getActiveQuests(playerId);
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━ ", NamedTextColor.GRAY)
                .append(Component.text("QUEST JOURNAL", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ━━━━━━━━", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
        
        if (activeQuests.isEmpty()) {
            player.sendMessage(Component.text("You have no active quests.", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Find NPCs with quests to get started!", NamedTextColor.GRAY));
        } else {
            for (PlayerQuestState state : activeQuests) {
                QuestDefinition quest = plugin.getQuestManager().getQuest(state.getQuestId());
                if (quest == null) continue;
                
                Objective currentObj = quest.getCurrentObjective(state.getObjectiveIndex());
                
                Component questLine = Component.text("• ", NamedTextColor.YELLOW)
                        .append(Component.text(quest.getTitle(), NamedTextColor.WHITE));
                
                if (currentObj != null) {
                    int required = getObjectiveRequirement(currentObj);
                    questLine = questLine.append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(state.getObjectiveProgress() + "/" + required, NamedTextColor.AQUA));
                }
                
                Component viewButton = Component.text(" [View]", NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Click to view details")))
                        .clickEvent(ClickEvent.runCommand("/quest view " + quest.getId()));
                
                player.sendMessage(questLine.append(viewButton));
                
                if (currentObj != null) {
                    player.sendMessage(Component.text("  ➤ " + currentObj.getDescription(), NamedTextColor.GRAY));
                }
            }
        }
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
    }
    
    /**
     * Notify player of objective progress
     */
    public void notifyProgress(Player player, QuestDefinition quest, Objective objective, int current, int required) {
        Component message = Component.text("[", NamedTextColor.GRAY)
                .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW))
                .append(Component.text("] ", NamedTextColor.GRAY))
                .append(Component.text(objective.getDescription() + ": ", NamedTextColor.WHITE))
                .append(Component.text(current + "/" + required, NamedTextColor.AQUA));
        
        player.sendMessage(message);
        
        // Action bar update
        if (plugin.getConfig().getBoolean("tracking.showActionBar", true)) {
            player.sendActionBar(Component.text(quest.getTitle() + ": " + current + "/" + required, NamedTextColor.YELLOW));
        }
    }
    
    /**
     * Notify player that objective is complete and next one started
     */
    public void notifyNextObjective(Player player, QuestDefinition quest, Objective nextObjective) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("✓ ", NamedTextColor.GREEN)
                .append(Component.text("Objective Complete!", NamedTextColor.YELLOW)));
        player.sendMessage(Component.text("➤ ", NamedTextColor.YELLOW)
                .append(Component.text(nextObjective.getDescription(), NamedTextColor.WHITE)));
        player.sendMessage(Component.empty());
    }
    
    /**
     * Notify player that quest is fully complete
     */
    public void notifyQuestComplete(Player player, QuestDefinition quest) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("★ ", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text("QUEST COMPLETE", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ★", NamedTextColor.GOLD, TextDecoration.BOLD)));
        player.sendMessage(Component.text(quest.getTitle(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Return to the quest giver to claim your rewards!", NamedTextColor.GREEN));
        player.sendMessage(Component.empty());
        
        // Play sound
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }
    
    // Helper methods
    
    private Component getStatusIcon(QuestStatus status) {
        return switch (status) {
            case NOT_STARTED -> Component.text("○", NamedTextColor.GRAY);
            case ACTIVE -> Component.text("◉", NamedTextColor.YELLOW);
            case COMPLETED -> Component.text("✓", NamedTextColor.GREEN);
            case TURNED_IN -> Component.text("✓", NamedTextColor.DARK_GRAY);
        };
    }
    
    private NamedTextColor getStatusColor(QuestStatus status) {
        return switch (status) {
            case NOT_STARTED -> NamedTextColor.WHITE;
            case ACTIVE -> NamedTextColor.YELLOW;
            case COMPLETED -> NamedTextColor.GREEN;
            case TURNED_IN -> NamedTextColor.DARK_GRAY;
        };
    }
    
    private boolean checkRequirements(Player player, QuestDefinition quest) {
        QuestRequirements req = quest.getRequirements();
        UUID playerId = player.getUniqueId();
        
        // Check level
        if (player.getLevel() < req.getMinLevel()) {
            return false;
        }
        
        // Check required quests
        for (String requiredQuestId : req.getRequiredQuestsCompleted()) {
            if (!plugin.getPlayerDataStore().hasCompletedQuest(playerId, requiredQuestId)) {
                return false;
            }
        }
        
        return true;
    }
    
    private int getObjectiveRequirement(Objective objective) {
        return switch (objective.getType()) {
            case KILL -> objective.getKillAmount();
            case COLLECT -> objective.getCollectAmount();
            case TALK, VISIT -> 1;
        };
    }
}
