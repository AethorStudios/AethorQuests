package com.aethor.aethorquests.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents quest rewards given upon completion
 */
public class Reward {
    private int xp;
    private double money;
    private List<String> commands;
    private List<ItemStack> items;
    
    public Reward() {
        this.xp = 0;
        this.money = 0.0;
        this.commands = new ArrayList<>();
        this.items = new ArrayList<>();
    }
    
    public int getXp() {
        return xp;
    }
    
    public void setXp(int xp) {
        this.xp = xp;
    }
    
    public double getMoney() {
        return money;
    }
    
    public void setMoney(double money) {
        this.money = money;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    
    public void addCommand(String command) {
        this.commands.add(command);
    }
    
    public List<ItemStack> getItems() {
        return items;
    }
    
    public void setItems(List<ItemStack> items) {
        this.items = items;
    }
    
    public void addItem(ItemStack item) {
        this.items.add(item);
    }
}
