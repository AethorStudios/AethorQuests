package com.aethor.aethorquests.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.manager.QuestFileManager;
import com.aethor.aethorquests.model.QuestDefinition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Handles all quest editor GUI interactions
 */
public class QuestEditorListener implements Listener {
    private final AethorQuestsPlugin plugin;
    private final QuestFileManager fileManager;
    
    // Track players in edit mode
    private final Map<UUID, EditorSession> editorSessions = new HashMap<>();
    
    public QuestEditorListener(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
        this.fileManager = new QuestFileManager(plugin);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Component titleComponent = event.getView().title();
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(titleComponent);
        
        // Check if this is a quest editor inventory
        if (!title.contains("Quest Editor") && !title.contains("Edit:") && !title.contains("Create New Quest") && !title.contains("Assign Quests")) {
            return;
        }
        
        // Handle NPC selector menu
        if (title.contains("Select NPC")) {
            handleNpcSelectorClick(player, event.getCurrentItem(), title);
            event.setCancelled(true);
            return;
        }
        
        // Handle quest assignment menu
        if (title.contains("Assign Quests:")) {
            handleAssignmentClick(player, event.getCurrentItem(), title);
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;
        
        Inventory inv = event.getInventory();
        Component displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().displayName() : Component.empty();
        String displayText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(displayName);
        
        // Main quest editor menu
        if (title.contains("Quest Editor:")) {
            String npcId = extractNpcIdFromTitle(title);
            handleMainEditorClick(player, clicked, displayText, inv, npcId);
        }
        // Quest details editor
        else if (title.contains("Edit:")) {
            handleDetailsEditorClick(player, clicked, displayText);
        }
        // Quest creator
        else if (title.contains("Create New Quest")) {
            handleCreatorClick(player, clicked, displayText);
        }
    }
    
    private void handleMainEditorClick(Player player, ItemStack clicked, String displayText, Inventory inv, String npcId) {
        // Store NPC ID in session
        EditorSession session = editorSessions.computeIfAbsent(player.getUniqueId(), k -> new EditorSession());
        session.npcId = npcId;
        
        if (displayText.contains("Create New Quest")) {
            new QuestCreatorGUI(plugin, player, npcId).open();
        }
        else if (displayText.contains("Assign/Unassign Quests")) {
            new QuestAssignmentGUI(plugin, player, npcId).open();
        }
        else if (displayText.contains("Close")) {
            player.closeInventory();
        }
        else if (clicked.getType() == org.bukkit.Material.BOOK) {
            // Extract quest ID from lore
            String questId = extractQuestIdFromLore(clicked);
            if (questId != null) {
                QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
                if (quest != null) {
                    if (player.isSneaking()) {
                        // Delete quest
                        deleteQuest(player, quest, npcId);
                    } else {
                        // Edit quest
                        new QuestDetailsEditorGUI(plugin, player, npcId, quest).open();
                    }
                }
            }
        }
    }
    
    private void handleAssignmentClick(Player player, ItemStack clicked, String title) {
        if (clicked == null || clicked.getType().isAir()) return;
        
        Component displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().displayName() : Component.empty();
        String displayText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(displayName);
        
        String npcId = extractNpcIdFromTitle(title);
        
        if (displayText.contains("Back")) {
            new QuestEditorGUI(plugin, player, npcId).open();
        }
        else if (displayText.contains("Close")) {
            player.closeInventory();
        }
        else if (clicked.getType() == org.bukkit.Material.BOOK || clicked.getType() == org.bukkit.Material.ENCHANTED_BOOK) {
            // Extract quest ID from lore
            String questId = extractQuestIdFromLore(clicked);
            if (questId != null) {
                QuestDefinition quest = plugin.getQuestManager().getQuest(questId);
                if (quest != null) {
                    // Toggle assignment
                    if (quest.getGiverNpcId().equals(npcId)) {
                        // Unassign: set to empty or "none"
                        quest.setGiverNpcId("none");
                        player.sendMessage(Component.text("✓ Quest unassigned from " + npcId, NamedTextColor.YELLOW));
                    } else {
                        // Assign to this NPC
                        quest.setGiverNpcId(npcId);
                        player.sendMessage(Component.text("✓ Quest assigned to " + npcId, NamedTextColor.GREEN));
                    }
                    
                    // Save changes
                    fileManager.saveAllQuests();
                    
                    // Reload quest manager to reflect changes
                    plugin.getQuestManager().reload();
                    
                    // Reopen GUI to show updated state
                    new QuestAssignmentGUI(plugin, player, npcId).open();
                }
            }
        }
    }
    
    private void handleDetailsEditorClick(Player player, ItemStack clicked, String displayText) {
        if (displayText.contains("Back")) {
            // Get NPC ID from session
            EditorSession session = editorSessions.get(player.getUniqueId());
            if (session != null) {
                new QuestEditorGUI(plugin, player, session.npcId).open();
            } else {
                player.closeInventory();
            }
        }
        else if (displayText.contains("Save Changes")) {
            player.sendMessage(Component.text("✓ Quest saved to file!", NamedTextColor.GREEN));
            fileManager.saveAllQuests();
            player.closeInventory();
        }
        else if (displayText.contains("Edit Title")) {
            startChatEdit(player, "title");
        }
        else if (displayText.contains("Edit Description")) {
            startChatEdit(player, "description");
        }
        else if (displayText.contains("Edit Objectives")) {
            player.sendMessage(Component.text("Objective editor coming soon! Edit quests.yml manually for now.", NamedTextColor.YELLOW));
        }
        else if (displayText.contains("Edit Rewards")) {
            player.sendMessage(Component.text("Reward editor coming soon! Edit quests.yml manually for now.", NamedTextColor.YELLOW));
        }
        else if (displayText.contains("Edit Requirements")) {
            player.sendMessage(Component.text("Requirements editor coming soon! Edit quests.yml manually for now.", NamedTextColor.YELLOW));
        }
    }
    
    private void handleCreatorClick(Player player, ItemStack clicked, String displayText) {
        if (displayText.contains("Back")) {
            EditorSession session = editorSessions.get(player.getUniqueId());
            if (session != null) {
                new QuestEditorGUI(plugin, player, session.npcId).open();
            } else {
                player.closeInventory();
            }
        }
        else if (displayText.contains("Create Quest")) {
            startChatEdit(player, "create_quest");
        }
    }
    
    private void startChatEdit(Player player, String editType) {
        EditorSession session = editorSessions.get(player.getUniqueId());
        if (session == null) {
            session = new EditorSession();
            editorSessions.put(player.getUniqueId(), session);
        }
        session.editType = editType;
        session.awaitingInput = true;
        
        player.closeInventory();
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        
        switch (editType) {
            case "create_quest" -> {
                player.sendMessage(Component.text("Enter the quest title:", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Type 'cancel' to cancel", NamedTextColor.GRAY));
            }
            case "title" -> {
                player.sendMessage(Component.text("Enter the new quest title:", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Type 'cancel' to cancel", NamedTextColor.GRAY));
            }
            case "description" -> {
                player.sendMessage(Component.text("Enter description lines (one per message):", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Type 'done' when finished, 'cancel' to cancel", NamedTextColor.GRAY));
            }
        }
        
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        EditorSession session = editorSessions.get(player.getUniqueId());
        
        if (session == null || !session.awaitingInput) return;
        
        event.setCancelled(true);
        String input = event.getMessage();
        
        if (input.equalsIgnoreCase("cancel")) {
            session.awaitingInput = false;
            player.sendMessage(Component.text("✗ Cancelled", NamedTextColor.RED));
            return;
        }
        
        switch (session.editType) {
            case "create_quest" -> {
                QuestCreatorGUI creator = new QuestCreatorGUI(plugin, player, session.npcId);
                QuestDefinition newQuest = creator.createQuest(input);
                
                // Save the quest
                fileManager.addQuest(newQuest);
                plugin.getQuestManager().reload();
                
                player.sendMessage(Component.text("✓ Quest created: " + newQuest.getTitle(), NamedTextColor.GREEN));
                session.awaitingInput = false;
                
                // Reopen editor
                plugin.getServer().getScheduler().runTask(plugin, () -> 
                    new QuestEditorGUI(plugin, player, session.npcId).open());
            }
            case "enter_npc_id" -> {
                // Create quest editor for the entered NPC ID
                session.npcId = input;
                session.awaitingInput = false;
                
                player.sendMessage(Component.text("✓ Opening quest editor for NPC: " + input, NamedTextColor.GREEN));
                
                // Open quest editor
                plugin.getServer().getScheduler().runTask(plugin, () -> 
                    new QuestEditorGUI(plugin, player, input).open());
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            EditorSession session = editorSessions.get(player.getUniqueId());
            if (session != null && !session.awaitingInput) {
                editorSessions.remove(player.getUniqueId());
            }
        }
    }
    
    private String extractNpcIdFromTitle(String title) {
        // Extract NPC ID from title like "Quest Editor: my_npc" or "Assign Quests: my_npc"
        if (title.contains("Quest Editor:")) {
            String[] parts = title.split("Quest Editor:");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        } else if (title.contains("Assign Quests:")) {
            String[] parts = title.split("Assign Quests:");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return "unknown_npc";
    }
    
    private String extractQuestIdFromLore(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().lore() == null) return null;
        
        List<Component> lore = item.getItemMeta().lore();
        for (Component line : lore) {
            String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line);
            if (text.contains("Quest ID:")) {
                String[] parts = text.split("Quest ID:");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
        }
        return null;
    }
    
    private void deleteQuest(Player player, QuestDefinition quest, String npcId) {
        fileManager.removeQuest(quest.getId());
        plugin.getQuestManager().reload();
        
        player.sendMessage(Component.text("✓ Quest deleted: " + quest.getTitle(), NamedTextColor.RED));
        player.closeInventory();
        
        // Reopen editor
        plugin.getServer().getScheduler().runTask(plugin, () -> 
            new QuestEditorGUI(plugin, player, npcId).open());
    }
    
    private void handleNpcSelectorClick(Player player, ItemStack clicked, String title) {
        if (clicked == null || clicked.getType().isAir()) return;
        
        Component displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().displayName() : Component.empty();
        String displayText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(displayName);
        
        if (displayText.contains("Close")) {
            player.closeInventory();
        } else if (displayText.contains("Create Quest for New NPC")) {
            startNpcIdInput(player);
        } else if (clicked.getType() == org.bukkit.Material.PLAYER_HEAD) {
            // Extract NPC ID from lore
            String npcId = extractNpcIdFromNpcSelector(clicked);
            if (npcId != null) {
                EditorSession session = editorSessions.computeIfAbsent(player.getUniqueId(), k -> new EditorSession());
                session.npcId = npcId;
                new QuestEditorGUI(plugin, player, npcId).open();
            }
        }
    }
    
    private void startNpcIdInput(Player player) {
        EditorSession session = editorSessions.computeIfAbsent(player.getUniqueId(), k -> new EditorSession());
        session.editType = "enter_npc_id";
        session.awaitingInput = true;
        
        player.closeInventory();
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Enter the NPC ID:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("(This must match an AethorNPCS NPC ID)", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Type 'cancel' to cancel", NamedTextColor.GRAY));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
    }
    
    private String extractNpcIdFromNpcSelector(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().lore() == null) return null;
        
        List<Component> lore = item.getItemMeta().lore();
        for (Component line : lore) {
            String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line);
            if (text.contains("NPC ID:")) {
                String[] parts = text.split("NPC ID:");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
        }
        return null;
    }
    
    private static class EditorSession {
        String npcId;
        String editType;
        boolean awaitingInput = false;
        QuestDefinition currentQuest;
    }
}
