package com.aethor.aethorquests.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.aethor.aethorquests.AethorQuestsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * GUI for selecting which NPC to edit quests for
 */
public class NpcSelectorGUI {
    private final AethorQuestsPlugin plugin;
    private final Player player;
    
    public NpcSelectorGUI(AethorQuestsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    /**
     * Open the NPC selector menu
     */
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, 
            Component.text("Quest Editor - Select NPC", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        
        // Get all unique NPC IDs - combine quest-assigned NPCs and actual server NPCs
        Set<String> npcIds = new java.util.HashSet<>(plugin.getQuestManager().getAllNpcIds());
        
        // Get all NPCs from AethorNPCS API if available
        if (plugin.getNpcHook().isEnabled() && plugin.getNpcHook().getApi() != null) {
            try {
                var api = plugin.getNpcHook().getApi();
                var allNpcs = api.getAllNpcs();
                for (var npc : allNpcs) {
                    npcIds.add(npc.getId());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get NPCs from AethorNPCS API: " + e.getMessage());
            }
        }
        
        int slot = 0;
        for (String npcId : npcIds) {
            if (slot >= 45) break; // Leave bottom row for controls
            
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = item.getItemMeta();
            
            // Try to get display name from NPC API
            String displayName = npcId;
            if (plugin.getNpcHook().isEnabled() && plugin.getNpcHook().getApi() != null) {
                try {
                    var npcOpt = plugin.getNpcHook().getApi().getNpc(npcId);
                    if (npcOpt.isPresent()) {
                        String npcDisplayName = npcOpt.get().getDisplayName();
                        if (npcDisplayName != null && !npcDisplayName.isEmpty()) {
                            displayName = npcDisplayName;
                        }
                    }
                } catch (Exception e) {
                    // Use NPC ID as fallback
                }
            }
            
            meta.displayName(Component.text(displayName, NamedTextColor.YELLOW, TextDecoration.BOLD));
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("NPC ID: " + npcId, NamedTextColor.GRAY));
            lore.add(Component.empty());
            
            int questCount = plugin.getQuestManager().getQuestsByGiverNpcId(npcId).size();
            lore.add(Component.text("Quests: " + questCount, NamedTextColor.AQUA));
            lore.add(Component.empty());
            lore.add(Component.text("Click to edit quests", NamedTextColor.GREEN));
            
            meta.lore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot++, item);
        }
        
        // Add "Create for New NPC" button
        ItemStack createNew = new ItemStack(Material.NAME_TAG);
        ItemMeta createMeta = createNew.getItemMeta();
        createMeta.displayName(Component.text("+ Create Quest for New NPC", NamedTextColor.GREEN, TextDecoration.BOLD));
        List<Component> createLore = new ArrayList<>();
        createLore.add(Component.text("Click to enter NPC ID in chat", NamedTextColor.GRAY));
        createLore.add(Component.text("to create quests for a new NPC", NamedTextColor.GRAY));
        createMeta.lore(createLore);
        createNew.setItemMeta(createMeta);
        inv.setItem(49, createNew);
        
        // Add "Close" button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED, TextDecoration.BOLD));
        close.setItemMeta(closeMeta);
        inv.setItem(53, close);
        
        player.openInventory(inv);
    }
}
