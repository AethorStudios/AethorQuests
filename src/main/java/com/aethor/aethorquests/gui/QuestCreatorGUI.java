package com.aethor.aethorquests.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.model.QuestDefinition;
import com.aethor.aethorquests.model.QuestRequirements;
import com.aethor.aethorquests.model.Reward;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * GUI for creating a new quest
 */
public class QuestCreatorGUI {
    private final AethorQuestsPlugin plugin;
    private final Player player;
    private final String npcId;
    
    public QuestCreatorGUI(AethorQuestsPlugin plugin, Player player, String npcId) {
        this.plugin = plugin;
        this.player = player;
        this.npcId = npcId;
    }
    
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, 
            Component.text("Create New Quest", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        
        // Create quest with default values
        ItemStack create = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.displayName(Component.text("Create Quest", NamedTextColor.GREEN, TextDecoration.BOLD));
        List<Component> createLore = new ArrayList<>();
        createLore.add(Component.text("Creates a new quest with", NamedTextColor.GRAY));
        createLore.add(Component.text("default values that you can", NamedTextColor.GRAY));
        createLore.add(Component.text("then edit.", NamedTextColor.GRAY));
        createLore.add(Component.empty());
        createLore.add(Component.text("Quest ID will be generated", NamedTextColor.YELLOW));
        createLore.add(Component.text("from the title you enter.", NamedTextColor.YELLOW));
        createLore.add(Component.empty());
        createLore.add(Component.text("Click to enter quest title", NamedTextColor.GREEN));
        createMeta.lore(createLore);
        create.setItemMeta(createMeta);
        inv.setItem(13, create);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back", NamedTextColor.YELLOW, TextDecoration.BOLD));
        back.setItemMeta(backMeta);
        inv.setItem(18, back);
        
        player.openInventory(inv);
    }
    
    /**
     * Create a new quest with the given title
     */
    public QuestDefinition createQuest(String title) {
        // Generate ID from title
        String questId = title.toLowerCase()
            .replaceAll("[^a-z0-9_]", "_")
            .replaceAll("_+", "_");
        
        // Make sure ID is unique
        int suffix = 1;
        String originalId = questId;
        while (plugin.getQuestManager().getQuest(questId) != null) {
            questId = originalId + "_" + suffix++;
        }
        
        QuestDefinition quest = new QuestDefinition(questId);
        quest.setTitle(title);
        quest.setDescription(Arrays.asList("&7A new quest.", "&7Edit this description."));
        quest.setGiverNpcId(npcId);
        
        QuestRequirements requirements = new QuestRequirements();
        requirements.setMinLevel(1);
        quest.setRequirements(requirements);
        
        Reward reward = new Reward();
        reward.setXp(100);
        quest.setRewards(reward);
        
        return quest;
    }
}
