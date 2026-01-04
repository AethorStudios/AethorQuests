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
 * GUI for assigning/unassigning quests to/from an NPC
 */
public class QuestAssignmentGUI {
    private final AethorQuestsPlugin plugin;
    private final String npcId;
    private final Player player;
    
    public QuestAssignmentGUI(AethorQuestsPlugin plugin, Player player, String npcId) {
        this.plugin = plugin;
        this.player = player;
        this.npcId = npcId;
    }
    
    /**
     * Open the quest assignment menu
     */
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, 
            Component.text("Assign Quests: ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
            .append(Component.text(npcId, NamedTextColor.GOLD)));
        
        // Get all quests
        List<QuestDefinition> allQuests = new ArrayList<>(plugin.getQuestManager().getAllQuests());
        
        // Display all quests with assignment status
        int slot = 0;
        for (QuestDefinition quest : allQuests) {
            if (slot >= 45) break; // Leave bottom row for controls
            
            boolean isAssigned = quest.getGiverNpcId().equals(npcId);
            
            ItemStack item = new ItemStack(isAssigned ? Material.ENCHANTED_BOOK : Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            
            if (isAssigned) {
                meta.displayName(Component.text("✓ " + quest.getTitle(), NamedTextColor.GREEN, TextDecoration.BOLD));
            } else {
                meta.displayName(Component.text(quest.getTitle(), NamedTextColor.YELLOW, TextDecoration.BOLD));
            }
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Quest ID: " + quest.getId(), NamedTextColor.GRAY));
            lore.add(Component.text("Currently assigned to: " + quest.getGiverNpcId(), NamedTextColor.GRAY));
            lore.add(Component.empty());
            
            // Show first line of description
            if (!quest.getDescription().isEmpty()) {
                String desc = quest.getDescription().get(0).replace("&", "§");
                if (desc.length() > 40) {
                    desc = desc.substring(0, 37) + "...";
                }
                lore.add(Component.text(desc, NamedTextColor.WHITE));
                lore.add(Component.empty());
            }
            
            if (isAssigned) {
                lore.add(Component.text("✓ Assigned to this NPC", NamedTextColor.GREEN));
                lore.add(Component.text("Click to unassign", NamedTextColor.RED));
            } else {
                lore.add(Component.text("✗ Not assigned to this NPC", NamedTextColor.RED));
                lore.add(Component.text("Click to assign", NamedTextColor.GREEN));
            }
            
            meta.lore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot++, item);
        }
        
        // Add "Back" button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("← Back", NamedTextColor.YELLOW, TextDecoration.BOLD));
        List<Component> backLore = new ArrayList<>();
        backLore.add(Component.text("Return to Quest Editor", NamedTextColor.GRAY));
        backMeta.lore(backLore);
        back.setItemMeta(backMeta);
        inv.setItem(49, back);
        
        // Add "Close" button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED, TextDecoration.BOLD));
        close.setItemMeta(closeMeta);
        inv.setItem(53, close);
        
        player.openInventory(inv);
    }
    
    public String getNpcId() {
        return npcId;
    }
}
