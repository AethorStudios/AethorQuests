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
import com.aethor.aethorquests.model.Objective;
import com.aethor.aethorquests.model.QuestDefinition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * GUI for editing quest details
 */
public class QuestDetailsEditorGUI {
    private final AethorQuestsPlugin plugin;
    private final Player player;
    private final String npcId;
    private final QuestDefinition quest;
    
    public QuestDetailsEditorGUI(AethorQuestsPlugin plugin, Player player, String npcId, QuestDefinition quest) {
        this.plugin = plugin;
        this.player = player;
        this.npcId = npcId;
        this.quest = quest;
    }
    
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, 
            Component.text("Edit: ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
            .append(Component.text(quest.getTitle(), NamedTextColor.GOLD)));
        
        // Edit Title
        ItemStack title = new ItemStack(Material.NAME_TAG);
        ItemMeta titleMeta = title.getItemMeta();
        titleMeta.displayName(Component.text("Edit Title", NamedTextColor.YELLOW, TextDecoration.BOLD));
        List<Component> titleLore = new ArrayList<>();
        titleLore.add(Component.text("Current: " + quest.getTitle(), NamedTextColor.WHITE));
        titleLore.add(Component.empty());
        titleLore.add(Component.text("Click to change in chat", NamedTextColor.GRAY));
        titleMeta.lore(titleLore);
        title.setItemMeta(titleMeta);
        inv.setItem(10, title);
        
        // Edit Description
        ItemStack desc = new ItemStack(Material.PAPER);
        ItemMeta descMeta = desc.getItemMeta();
        descMeta.displayName(Component.text("Edit Description", NamedTextColor.AQUA, TextDecoration.BOLD));
        List<Component> descLore = new ArrayList<>();
        descLore.add(Component.text("Current description:", NamedTextColor.WHITE));
        for (String line : quest.getDescription()) {
            descLore.add(Component.text(" " + line, NamedTextColor.GRAY));
        }
        descLore.add(Component.empty());
        descLore.add(Component.text("Click to edit lines", NamedTextColor.GRAY));
        descMeta.lore(descLore);
        desc.setItemMeta(descMeta);
        inv.setItem(11, desc);
        
        // Edit Objectives
        ItemStack objectives = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta objMeta = objectives.getItemMeta();
        objMeta.displayName(Component.text("Edit Objectives", NamedTextColor.GREEN, TextDecoration.BOLD));
        List<Component> objLore = new ArrayList<>();
        objLore.add(Component.text("Objectives: " + quest.getObjectives().size(), NamedTextColor.WHITE));
        for (int i = 0; i < quest.getObjectives().size(); i++) {
            Objective obj = quest.getObjectives().get(i);
            objLore.add(Component.text((i + 1) + ". " + obj.getType() + " - " + obj.getDescription(), NamedTextColor.GRAY));
        }
        objLore.add(Component.empty());
        objLore.add(Component.text("Click to manage objectives", NamedTextColor.GRAY));
        objMeta.lore(objLore);
        objectives.setItemMeta(objMeta);
        inv.setItem(12, objectives);
        
        // Edit Rewards
        ItemStack rewards = new ItemStack(Material.CHEST);
        ItemMeta rewardMeta = rewards.getItemMeta();
        rewardMeta.displayName(Component.text("Edit Rewards", NamedTextColor.GOLD, TextDecoration.BOLD));
        List<Component> rewardLore = new ArrayList<>();
        rewardLore.add(Component.text("Current rewards:", NamedTextColor.WHITE));
        if (quest.getRewards().getXp() > 0) {
            rewardLore.add(Component.text(" • " + quest.getRewards().getXp() + " XP", NamedTextColor.GREEN));
        }
        if (quest.getRewards().getMoney() > 0) {
            rewardLore.add(Component.text(" • $" + quest.getRewards().getMoney(), NamedTextColor.GOLD));
        }
        if (!quest.getRewards().getCommands().isEmpty()) {
            rewardLore.add(Component.text(" • " + quest.getRewards().getCommands().size() + " commands", NamedTextColor.AQUA));
        }
        rewardLore.add(Component.empty());
        rewardLore.add(Component.text("Click to edit rewards", NamedTextColor.GRAY));
        rewardMeta.lore(rewardLore);
        rewards.setItemMeta(rewardMeta);
        inv.setItem(13, rewards);
        
        // Edit Requirements
        ItemStack requirements = new ItemStack(Material.IRON_DOOR);
        ItemMeta reqMeta = requirements.getItemMeta();
        reqMeta.displayName(Component.text("Edit Requirements", NamedTextColor.RED, TextDecoration.BOLD));
        List<Component> reqLore = new ArrayList<>();
        reqLore.add(Component.text("Min Level: " + quest.getRequirements().getMinLevel(), NamedTextColor.WHITE));
        reqLore.add(Component.text("Required Quests: " + quest.getRequirements().getRequiredQuestsCompleted().size(), NamedTextColor.WHITE));
        reqLore.add(Component.empty());
        reqLore.add(Component.text("Click to edit requirements", NamedTextColor.GRAY));
        reqMeta.lore(reqLore);
        requirements.setItemMeta(reqMeta);
        inv.setItem(14, requirements);
        
        // Save button
        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.displayName(Component.text("Save Changes", NamedTextColor.GREEN, TextDecoration.BOLD));
        List<Component> saveLore = new ArrayList<>();
        saveLore.add(Component.text("Click to save quest to file", NamedTextColor.GRAY));
        saveMeta.lore(saveLore);
        save.setItemMeta(saveMeta);
        inv.setItem(22, save);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back", NamedTextColor.YELLOW, TextDecoration.BOLD));
        back.setItemMeta(backMeta);
        inv.setItem(18, back);
        
        player.openInventory(inv);
    }
}
