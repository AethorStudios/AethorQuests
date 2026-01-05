package com.aethor.aethorquests.command;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.*;
import com.aethor.aethorquests.tracker.CollectObjectiveTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles /quest command for players
 */
public class QuestCommand implements CommandExecutor, TabCompleter {
    private final AethorQuestsPlugin plugin;
    private final CollectObjectiveTracker collectTracker;
    
    public QuestCommand(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
        this.collectTracker = new CollectObjectiveTracker(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }
        
        if (!player.hasPermission("aethorquests.use")) {
            player.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            // Default to journal
            plugin.getQuestUI().showQuestJournal(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "journal", "j", "list" -> {
                plugin.getQuestUI().showQuestJournal(player);
            }
            
            case "view", "info" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /quest view <questId>", NamedTextColor.RED));
                    return true;
                }
                
                String questId = args[1];
                QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
                
                if (quest == null) {
                    player.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
                    return true;
                }
                
                plugin.getQuestUI().showQuestDetails(player, quest);
            }
            
            case "accept", "start" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /quest accept <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleAcceptQuest(player, args[1]);
            }
            
            case "turnin", "complete", "finish" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /quest turnin <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleTurnInQuest(player, args[1]);
            }
            
            case "abandon", "cancel", "drop" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /quest abandon <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleAbandonQuest(player, args[1]);
            }
            
            case "track" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /quest track <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleTrackQuest(player, args[1]);
            }
            
            case "dialogue" -> {
                if (args.length < 3 || !args[1].equalsIgnoreCase("select")) {
                    player.sendMessage(Component.text("Usage: /quest dialogue select <option>", NamedTextColor.RED));
                    return true;
                }
                
                handleDialogueSelect(player, args[2]);
            }
            
            case "reload" -> {
                if (!player.hasPermission("aethorquests.admin")) {
                    player.sendMessage(Component.text("You don't have permission to reload quests.", NamedTextColor.RED));
                    return true;
                }
                
                plugin.getQuestManager().reload();
                player.sendMessage(Component.text("Quests reloaded!", NamedTextColor.GREEN));
            }
            
            default -> {
                sendHelp(player);
            }
        }
        
        return true;
    }
    
    private void handleAcceptQuest(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        
        if (quest == null) {
            player.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
            return;
        }
        
        PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, questId);
        
        if (state != null && state.getStatus() != QuestStatus.NOT_STARTED) {
            player.sendMessage(Component.text("You have already accepted this quest.", NamedTextColor.RED));
            return;
        }
        
        // Check requirements
        QuestRequirements req = quest.getRequirements();
        
        if (player.getLevel() < req.getMinLevel()) {
            player.sendMessage(Component.text("You need to be level " + req.getMinLevel() + " to accept this quest.", NamedTextColor.RED));
            return;
        }
        
        for (String requiredQuestId : req.getRequiredQuestsCompleted()) {
            if (!plugin.getPlayerDataStore().hasCompletedQuest(playerId, requiredQuestId)) {
                player.sendMessage(Component.text("You must complete other quests first.", NamedTextColor.RED));
                return;
            }
        }
        
        // Accept quest
        state = plugin.getPlayerDataStore().getOrCreateQuestState(playerId, questId);
        state.setStatus(QuestStatus.ACTIVE);
        plugin.getPlayerDataStore().setQuestState(playerId, state);
        
        // Notify player
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Quest Accepted!", NamedTextColor.GREEN));
        player.sendMessage(Component.text(quest.getTitle(), NamedTextColor.YELLOW));
        
        Objective firstObjective = quest.getCurrentObjective(0);
        if (firstObjective != null) {
            player.sendMessage(Component.text("➤ " + firstObjective.getDescription(), NamedTextColor.WHITE));
        }
        
        player.sendMessage(Component.empty());
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }
    
    private void handleTurnInQuest(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        
        if (quest == null) {
            player.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
            return;
        }
        
        PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, questId);
        
        if (state == null || state.getStatus() != QuestStatus.COMPLETED) {
            player.sendMessage(Component.text("This quest is not ready to turn in.", NamedTextColor.RED));
            return;
        }
        
        // Process any remaining collect objectives
        collectTracker.processCollectObjectives(player, quest, state);
        
        // Give rewards
        Reward rewards = quest.getRewards();
        
        // XP
        if (rewards.getXp() > 0) {
            player.giveExp(rewards.getXp());
        }
        
        // Commands (placeholder replacements)
        for (String cmd : rewards.getCommands()) {
            String processedCmd = cmd.replace("{player}", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCmd);
        }
        
        // Items
        for (org.bukkit.inventory.ItemStack item : rewards.getItems()) {
            player.getInventory().addItem(item);
        }
        
        // Mark as turned in
        state.setStatus(QuestStatus.TURNED_IN);
        plugin.getPlayerDataStore().setQuestState(playerId, state);
        
        // Notify player
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("QUEST COMPLETED!", NamedTextColor.GOLD));
        player.sendMessage(Component.text(quest.getTitle(), NamedTextColor.YELLOW));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Rewards:", NamedTextColor.WHITE));
        
        if (rewards.getXp() > 0) {
            player.sendMessage(Component.text("  + " + rewards.getXp() + " XP", NamedTextColor.GREEN));
        }
        if (rewards.getMoney() > 0) {
            player.sendMessage(Component.text("  + $" + rewards.getMoney(), NamedTextColor.GOLD));
        }
        
        player.sendMessage(Component.text("═══════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }
    
    private void handleAbandonQuest(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, questId);
        
        if (state == null || state.getStatus() == QuestStatus.NOT_STARTED || state.getStatus() == QuestStatus.TURNED_IN) {
            player.sendMessage(Component.text("You don't have this quest active.", NamedTextColor.RED));
            return;
        }
        
        // Reset state
        state.setStatus(QuestStatus.NOT_STARTED);
        state.setObjectiveIndex(0);
        state.setObjectiveProgress(0);
        plugin.getPlayerDataStore().setQuestState(playerId, state);
        
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        String questName = quest != null ? quest.getTitle() : questId;
        
        player.sendMessage(Component.text("Quest abandoned: ", NamedTextColor.YELLOW)
                .append(Component.text(questName, NamedTextColor.RED)));
    }
    
    private void handleTrackQuest(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, questId);
        
        if (state == null || state.getStatus() != QuestStatus.ACTIVE) {
            player.sendMessage(Component.text("You don't have this quest active.", NamedTextColor.RED));
            return;
        }
        
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            player.sendMessage(Component.text("Quest not found.", NamedTextColor.RED));
            return;
        }
        
        Objective currentObj = quest.getCurrentObjective(state.getObjectiveIndex());
        if (currentObj != null) {
            player.sendMessage(Component.text("Tracking: ", NamedTextColor.YELLOW)
                    .append(Component.text(quest.getTitle(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text("➤ " + currentObj.getDescription(), NamedTextColor.AQUA));
        }
    }
    
    private void handleDialogueSelect(Player player, String optionIdStr) {
        UUID playerId = player.getUniqueId();
        
        // Check if player has active dialogue
        if (!plugin.getDialogueManager().hasActiveSession(playerId)) {
            player.sendMessage(Component.text("You don't have an active dialogue.", NamedTextColor.RED));
            return;
        }
        
        // Parse option ID
        int optionId;
        try {
            optionId = Integer.parseInt(optionIdStr);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid option number.", NamedTextColor.RED));
            return;
        }
        
        // Get the session
        var session = plugin.getDialogueManager().getSession(playerId);
        plugin.getLogger().info("Checking session: " + (session != null ? "exists, waitingForChoice=" + session.isWaitingForChoice() : "null"));
        if (session == null || !session.isWaitingForChoice()) {
            player.sendMessage(Component.text("No dialogue choice is currently available.", NamedTextColor.RED));
            return;
        }
        
        // Get the option
        var option = session.getCurrentNode().getOptionById(optionId);
        if (option == null) {
            player.sendMessage(Component.text("Invalid option: " + optionId, NamedTextColor.RED));
            return;
        }
        
        // Handle the selection via the input handler logic
        var renderer = new com.aethor.aethorquests.ui.DialogueRenderer(plugin.getConfig());
        
        // Execute any command associated with the option
        if (option.hasCommand()) {
            executeDialogueCommand(player, session, option.getCommand());
        }
        
        // Select the option
        boolean selected = session.selectOption(optionId);
        
        if (selected) {
            if (!session.isComplete()) {
                renderer.renderCurrentLine(player, session);
            } else {
                renderer.renderDialogueEnd(player);
                plugin.getDialogueManager().endSession(playerId);
            }
        }
    }
    
    private void executeDialogueCommand(Player player, com.aethor.aethorquests.model.dialogue.DialogueSession session, String command) {
        switch (command.toLowerCase()) {
            case "accept_quest" -> {
                if (session.getQuestId() != null) {
                    handleAcceptQuest(player, session.getQuestId());
                }
            }
            case "decline_quest" -> {
                player.sendMessage(Component.text("Perhaps another time...", NamedTextColor.GRAY));
            }
            case "turn_in_quest" -> {
                if (session.getQuestId() != null) {
                    handleTurnInQuest(player, session.getQuestId());
                }
            }
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━ ", NamedTextColor.GRAY)
                .append(Component.text("QUEST COMMANDS", NamedTextColor.GOLD))
                .append(Component.text(" ━━━━━", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/quest journal", NamedTextColor.YELLOW)
                .append(Component.text(" - View active quests", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/quest view <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - View quest details", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/quest accept <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Accept a quest", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/quest turnin <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Turn in completed quest", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/quest abandon <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Abandon a quest", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/quest track <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Track a quest", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("journal");
            completions.add("view");
            completions.add("accept");
            completions.add("turnin");
            completions.add("abandon");
            completions.add("track");
            if (sender.hasPermission("aethorquests.admin")) {
                completions.add("reload");
            }
        } else if (args.length == 2 && sender instanceof Player player) {
            String subCmd = args[0].toLowerCase();
            UUID playerId = player.getUniqueId();
            
            if (subCmd.equals("view") || subCmd.equals("info")) {
                for (QuestDefinition quest : plugin.getQuestManager().getAllQuests()) {
                    completions.add(quest.getId());
                }
            } else if (subCmd.equals("accept") || subCmd.equals("start")) {
                for (QuestDefinition quest : plugin.getQuestManager().getAllQuests()) {
                    PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, quest.getId());
                    if (state == null || state.getStatus() == QuestStatus.NOT_STARTED) {
                        completions.add(quest.getId());
                    }
                }
            } else if (subCmd.equals("turnin") || subCmd.equals("abandon") || subCmd.equals("track")) {
                for (PlayerQuestState state : plugin.getPlayerDataStore().getActiveQuests(playerId)) {
                    completions.add(state.getQuestId());
                }
            }
        }
        
        return completions;
    }
}
