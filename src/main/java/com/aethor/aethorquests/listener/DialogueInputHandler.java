package com.aethor.aethorquests.listener;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.manager.DialogueManager;
import com.aethor.aethorquests.model.dialogue.DialogueOption;
import com.aethor.aethorquests.model.dialogue.DialogueSession;
import com.aethor.aethorquests.ui.DialogueRenderer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Handles player input for dialogue progression.
 * Listens for sneak events, chat messages (for number input), and player disconnects.
 */
public class DialogueInputHandler implements Listener {
    
    private final AethorQuestsPlugin plugin;
    private final DialogueManager dialogueManager;
    private final DialogueRenderer renderer;
    
    public DialogueInputHandler(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
        this.dialogueManager = plugin.getDialogueManager();
        this.renderer = new DialogueRenderer(plugin.getConfig());
    }
    
    /**
     * Handles sneak events for dialogue advancement.
     * When a player starts sneaking and has an active dialogue, advance to the next line.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Only respond to sneak start, not sneak end
        if (!event.isSneaking()) {
            return;
        }
        
        // Check if player has active dialogue
        DialogueSession session = dialogueManager.getSession(playerId);
        if (session == null) {
            return;
        }
        
        // Don't advance if waiting for a choice
        if (session.isWaitingForChoice()) {
            return;
        }
        
        // Advance dialogue
        boolean advanced = dialogueManager.advanceDialogue(playerId);
        
        if (advanced) {
            // Render the new line
            renderer.renderCurrentLine(player, session);
            
            // Check if we should now show choices
            if (session.isWaitingForChoice()) {
                renderer.renderChoices(player, session);
            }
        } else {
            // Dialogue ended or needs choice selection
            if (session.isComplete()) {
                renderer.renderDialogueEnd(player);
                dialogueManager.endSession(playerId);
                
                // Execute any post-dialogue actions (handled by quest system)
                executePostDialogueAction(player, session);
            } else if (session.isWaitingForChoice()) {
                renderer.renderChoices(player, session);
            }
        }
        
        // Cancel the sneak event to prevent crouching
        event.setCancelled(true);
    }
    
    /**
     * Handles chat messages for numeric dialogue choice selection.
     * If a player has active dialogue choices and types 1-9, select that option.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        DialogueSession session = dialogueManager.getSession(playerId);
        if (session == null || !session.isWaitingForChoice()) {
            return;
        }
        
        String message = event.getMessage().trim();
        
        // Check if message is a single digit 1-9
        if (!message.matches("[1-9]")) {
            return;
        }
        
        int optionId = Integer.parseInt(message);
        
        // Try to select the option
        DialogueOption option = session.getCurrentNode().getOptionById(optionId);
        if (option == null) {
            return;
        }
        
        // Cancel the chat event so the number doesn't appear in chat
        event.setCancelled(true);
        
        // Handle selection synchronously on main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            handleOptionSelection(player, session, option);
        });
    }
    
    /**
     * Handles dialogue option selection.
     *
     * @param player The player who selected the option
     * @param session The active dialogue session
     * @param option The selected option
     */
    private void handleOptionSelection(Player player, DialogueSession session, DialogueOption option) {
        // Execute any command associated with the option
        if (option.hasCommand()) {
            executeDialogueCommand(player, session, option.getCommand());
        }
        
        // Select the option and transition
        boolean selected = session.selectOption(option.getId());
        
        if (selected) {
            // If there's a next node, render its first line
            if (!session.isComplete()) {
                renderer.renderCurrentLine(player, session);
            } else {
                // Dialogue ended
                renderer.renderDialogueEnd(player);
                dialogueManager.endSession(player.getUniqueId());
                executePostDialogueAction(player, session);
            }
        }
    }
    
    /**
     * Executes a command associated with a dialogue option.
     *
     * @param player The player
     * @param session The dialogue session
     * @param command The command to execute (e.g., "accept_quest", "decline_quest")
     */
    private void executeDialogueCommand(Player player, DialogueSession session, String command) {
        switch (command.toLowerCase()) {
            case "accept_quest" -> {
                if (session.getQuestId() != null) {
                    plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            "quest accept " + session.getQuestId() + " " + player.getName()
                    );
                }
            }
            case "decline_quest" -> {
                player.sendMessage(net.kyori.adventure.text.Component.text(
                        "Perhaps another time...",
                        net.kyori.adventure.text.format.NamedTextColor.GRAY
                ));
            }
            case "turn_in_quest" -> {
                if (session.getQuestId() != null) {
                    plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            "quest turnin " + session.getQuestId() + " " + player.getName()
                    );
                }
            }
        }
    }
    
    /**
     * Executes any actions that should happen after dialogue completes.
     *
     * @param player The player
     * @param session The completed dialogue session
     */
    private void executePostDialogueAction(Player player, DialogueSession session) {
        // This can be extended by quest system for custom post-dialogue logic
        // For now, it's just a hook for future expansion
    }
    
    /**
     * Clean up dialogue sessions when players disconnect.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dialogueManager.endSession(event.getPlayer().getUniqueId());
    }
}
