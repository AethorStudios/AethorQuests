package com.aethor.aethorquests.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.QuestDefinition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Main GUI for editing NPC quests
 */
public class QuestEditorGUI {
    private final AethorQuestsPlugin plugin;
    private final String npcId;
    private final Player player;
    
    public QuestEditorGUI(AethorQuestsPlugin plugin, Player player, String npcId) {
        this.plugin = plugin;
        this.player = player;
        this.npcId = npcId;
    }
    
    /**
     * Open the main quest editor menu
     */
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, 
            Component.text("Quest Editor: ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
            .append(Component.text(npcId, NamedTextColor.GOLD)));
        
        // Get quests for this NPC
        List<QuestDefinition> quests = plugin.getQuestManager().getQuestsByGiverNpcId(npcId);
        
        // Display existing quests
        int slot = 0;
        for (QuestDefinition quest : quests) {
            if (slot >= 45) break; // Leave bottom row for controls
            
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            
            meta.displayName(Component.text(quest.getTitle(), NamedTextColor.YELLOW, TextDecoration.BOLD));
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Quest ID: " + quest.getId(), NamedTextColor.GRAY));
            lore.add(Component.empty());
            
            for (String line : quest.getDescription()) {
                lore.add(Component.text(line.replace("&", "§"), NamedTextColor.WHITE));
            }
            
            lore.add(Component.empty());
            lore.add(Component.text("Objectives: " + quest.getObjectives().size(), NamedTextColor.AQUA));
            lore.add(Component.text("Click to edit", NamedTextColor.GREEN));
            lore.add(Component.text("Shift+Click to delete", NamedTextColor.RED));
            
            meta.lore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot++, item);
        }
        
        // Add "Create New Quest" button
        ItemStack createNew = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta createMeta = createNew.getItemMeta();
        createMeta.displayName(Component.text("+ Create New Quest", NamedTextColor.GREEN, TextDecoration.BOLD));
        List<Component> createLore = new ArrayList<>();
        createLore.add(Component.text("Click to create a new quest", NamedTextColor.GRAY));
        createLore.add(Component.text("for this NPC", NamedTextColor.GRAY));
        createMeta.lore(createLore);
        createNew.setItemMeta(createMeta);
        inv.setItem(49, createNew);
        
        // Add "Assign Quests" button
        ItemStack assign = new ItemStack(Material.ENDER_EYE);
        ItemMeta assignMeta = assign.getItemMeta();
        assignMeta.displayName(Component.text("Assign/Unassign Quests", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        List<Component> assignLore = new ArrayList<>();
        assignLore.add(Component.text("Manage which quests", NamedTextColor.GRAY));
        assignLore.add(Component.text("are assigned to this NPC", NamedTextColor.GRAY));
        assignMeta.lore(assignLore);
        assign.setItemMeta(assignMeta);
        inv.setItem(48, assign);
        
        // Add "Close" button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED, TextDecoration.BOLD));
        close.setItemMeta(closeMeta);
        inv.setItem(53, close);
        
        player.openInventory(inv);
    }
    
    /**
     * Open editor for specific quest
     */
    public void openQuestEditor(QuestDefinition quest) {
        new QuestDetailsEditorGUI(plugin, player, npcId, quest).open();
    }
    
    /**
     * Open quest creation dialog
     */
    public void openQuestCreator() {
        new QuestCreatorGUI(plugin, player, npcId).open();
    }
    
    public String getNpcId() {
        return npcId;
    }
}
