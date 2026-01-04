package com.aethor.aethorquests.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.PlayerQuestState;
import com.aethor.aethorquests.model.QuestDefinition;
import com.aethor.aethorquests.model.QuestStatus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Handles /questadmin command for administrators
 */
public class QuestAdminCommand implements CommandExecutor, TabCompleter {
    private final AethorQuestsPlugin plugin;
    
    public QuestAdminCommand(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("aethorquests.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "editor", "edit", "gui" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
                    return true;
                }
                
                if (!sender.hasPermission("aethorquests.admin.edit")) {
                    sender.sendMessage(Component.text("You don't have permission to use the quest editor.", NamedTextColor.RED));
                    return true;
                }
                
                new com.aethor.aethorquests.gui.NpcSelectorGUI(plugin, player).open();
                return true;
            }
            
            case "reload" -> {
                plugin.getQuestManager().reload();
                sender.sendMessage(Component.text("Quests reloaded successfully!", NamedTextColor.GREEN));
            }
            
            case "give", "assign" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /questadmin give <player> <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleGiveQuest(sender, args[1], args[2]);
            }
            
            case "complete", "finish" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /questadmin complete <player> <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleCompleteQuest(sender, args[1], args[2]);
            }
            
            case "reset" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /questadmin reset <player> <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleResetQuest(sender, args[1], args[2]);
            }
            
            case "list" -> {
                sender.sendMessage(Component.text("Loaded Quests:", NamedTextColor.GOLD));
                for (QuestDefinition quest : plugin.getQuestManager().getAllQuests()) {
                    sender.sendMessage(Component.text("  • ", NamedTextColor.GRAY)
                            .append(Component.text(quest.getId(), NamedTextColor.YELLOW))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(quest.getTitle(), NamedTextColor.WHITE)));
                }
            }
            
            case "info" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /questadmin info <player> <questId>", NamedTextColor.RED));
                    return true;
                }
                
                handleQuestInfo(sender, args[1], args[2]);
            }
            
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    private void handleGiveQuest(CommandSender sender, String playerName, String questId) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
            return;
        }
        
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            sender.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
            return;
        }
        
        UUID playerId = target.getUniqueId();
        PlayerQuestState state = plugin.getPlayerDataStore().getOrCreateQuestState(playerId, questId);
        state.setStatus(QuestStatus.ACTIVE);
        plugin.getPlayerDataStore().setQuestState(playerId, state);
        
        sender.sendMessage(Component.text("Quest ", NamedTextColor.GREEN)
                .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW))
                .append(Component.text(" given to ", NamedTextColor.GREEN))
                .append(Component.text(target.getName(), NamedTextColor.WHITE)));
        
        target.sendMessage(Component.text("An administrator has given you the quest: ", NamedTextColor.GREEN)
                .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW)));
    }
    
    private void handleCompleteQuest(CommandSender sender, String playerName, String questId) {
        Player target = Bukkit.getPlayer(playerName);
        UUID playerId;
        
        if (target == null) {
            // Try to get offline player
            try {
                playerId = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            } catch (Exception e) {
                sender.sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
                return;
            }
        } else {
            playerId = target.getUniqueId();
        }
        
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            sender.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
            return;
        }
        
        PlayerQuestState state = plugin.getPlayerDataStore().getOrCreateQuestState(playerId, questId);
        state.setStatus(QuestStatus.COMPLETED);
        state.setObjectiveIndex(quest.getObjectives().size() - 1);
        plugin.getPlayerDataStore().setQuestState(playerId, state);
        
        sender.sendMessage(Component.text("Quest ", NamedTextColor.GREEN)
                .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW))
                .append(Component.text(" marked as completed for ", NamedTextColor.GREEN))
                .append(Component.text(playerName, NamedTextColor.WHITE)));
        
        if (target != null) {
            target.sendMessage(Component.text("An administrator has completed your quest: ", NamedTextColor.GREEN)
                    .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW)));
        }
    }
    
    private void handleResetQuest(CommandSender sender, String playerName, String questId) {
        Player target = Bukkit.getPlayer(playerName);
        UUID playerId;
        
        if (target == null) {
            try {
                playerId = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            } catch (Exception e) {
                sender.sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
                return;
            }
        } else {
            playerId = target.getUniqueId();
        }
        
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            sender.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
            return;
        }
        
        PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, questId);
        if (state != null) {
            state.setStatus(QuestStatus.NOT_STARTED);
            state.setObjectiveIndex(0);
            state.setObjectiveProgress(0);
            plugin.getPlayerDataStore().setQuestState(playerId, state);
        }
        
        sender.sendMessage(Component.text("Quest ", NamedTextColor.GREEN)
                .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW))
                .append(Component.text(" reset for ", NamedTextColor.GREEN))
                .append(Component.text(playerName, NamedTextColor.WHITE)));
    }
    
    private void handleQuestInfo(CommandSender sender, String playerName, String questId) {
        Player target = Bukkit.getPlayer(playerName);
        UUID playerId;
        
        if (target == null) {
            try {
                playerId = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            } catch (Exception e) {
                sender.sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
                return;
            }
        } else {
            playerId = target.getUniqueId();
        }
        
        QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            sender.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
            return;
        }
        
        PlayerQuestState state = plugin.getPlayerDataStore().getQuestState(playerId, questId);
        
        sender.sendMessage(Component.text("Quest Info for ", NamedTextColor.GOLD)
                .append(Component.text(playerName, NamedTextColor.WHITE))
                .append(Component.text(":", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("  Quest: ", NamedTextColor.GRAY)
                .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW)));
        
        if (state == null) {
            sender.sendMessage(Component.text("  Status: ", NamedTextColor.GRAY)
                    .append(Component.text("NOT_STARTED", NamedTextColor.WHITE)));
        } else {
            sender.sendMessage(Component.text("  Status: ", NamedTextColor.GRAY)
                    .append(Component.text(state.getStatus().name(), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("  Objective Index: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(state.getObjectiveIndex()), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("  Progress: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(state.getObjectiveProgress()), NamedTextColor.WHITE)));
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("━━━━━ ", NamedTextColor.GRAY)
                .append(Component.text("QUEST ADMIN COMMANDS", NamedTextColor.GOLD))
                .append(Component.text(" ━━━━━", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/questadmin editor", NamedTextColor.YELLOW)
                .append(Component.text(" - Open quest editor GUI", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/questadmin reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload quests", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/questadmin give <player> <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Give quest to player", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/questadmin complete <player> <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Complete quest for player", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/questadmin reset <player> <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - Reset quest for player", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/questadmin list", NamedTextColor.YELLOW)
                .append(Component.text(" - List all quests", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/questadmin info <player> <id>", NamedTextColor.YELLOW)
                .append(Component.text(" - View player quest info", NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("editor");
            completions.add("reload");
            completions.add("give");
            completions.add("complete");
            completions.add("reset");
            completions.add("list");
            completions.add("info");
        } else if (args.length == 2) {
            // Player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3) {
            // Quest IDs
            for (QuestDefinition quest : plugin.getQuestManager().getAllQuests()) {
                completions.add(quest.getId());
            }
        }
        
        return completions;
    }
}
