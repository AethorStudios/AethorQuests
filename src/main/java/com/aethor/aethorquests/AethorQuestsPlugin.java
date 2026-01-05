package com.aethor.aethorquests;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.aethor.aethorquests.command.QuestAdminCommand;
import com.aethor.aethorquests.command.QuestCommand;
import com.aethor.aethorquests.hook.AethorNpcHook;
import com.aethor.aethorquests.listener.PlayerListener;
import com.aethor.aethorquests.manager.PlayerDataStore;
import com.aethor.aethorquests.manager.QuestManager;
import com.aethor.aethorquests.tracker.KillObjectiveTracker;
import com.aethor.aethorquests.tracker.TalkObjectiveTracker;
import com.aethor.aethorquests.tracker.VisitObjectiveTracker;
import com.aethor.aethorquests.ui.QuestUI;
import com.aethor.aethorquests.web.WebServer;

/**
 * Main plugin class for AethorQuests
 * Production-quality quest system for Paper Minecraft 1.21.8
 */
public class AethorQuestsPlugin extends JavaPlugin {
    
    // Managers
    private QuestManager questManager;
    private PlayerDataStore playerDataStore;
    
    // UI
    private QuestUI questUI;
    
    // Web Server
    private WebServer webServer;
    
    // Hook
    private AethorNpcHook npcHook;
    
    // Trackers
    private TalkObjectiveTracker talkTracker;
    private VisitObjectiveTracker visitTracker;
    private BukkitTask autoSaveTask;
    
    // Debug mode
    private boolean debug;
    
    @Override
    public void onEnable() {
        logBanner();
        
        // Save default config
        saveDefaultConfig();
        debug = getConfig().getBoolean("debug", false);
        
        // Initialize managers
        questManager = new QuestManager(this);
        playerDataStore = new PlayerDataStore(this);
        questUI = new QuestUI(this);
        
        // Initialize AethorNPCS hook
        npcHook = new AethorNpcHook(this);
        if (!npcHook.loadApi()) {
            getLogger().severe("Failed to load AethorNPCS API. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Load quests
        questManager.loadQuests();
        
        // Register event listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        // Start periodic tasks
        startPeriodicTasks();
        
        // Load data for online players (in case of reload)
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            playerDataStore.loadPlayerData(player.getUniqueId());
        }
        
        // Start web server if enabled
        if (getConfig().getBoolean("webPanel.enabled", true)) {
            String host = getConfig().getString("webPanel.host", "0.0.0.0");
            int port = getConfig().getInt("webPanel.port", 8080);
            webServer = new WebServer(this, host, port);
            webServer.start();
        }
        
        getLogger().info("AethorQuests v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Loaded " + questManager.getAllQuests().size() + " quest(s)");
    }
    
    @Override
    public void onDisable() {
        // Stop web server
        if (webServer != null) {
            webServer.stop();
        }
        
        // Cancel all tasks
        if (visitTracker != null) {
            visitTracker.cancel();
        }
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        
        // Save all player data
        getLogger().info("Saving all player data...");
        playerDataStore.saveAll();
        
        getLogger().info("AethorQuests disabled.");
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new KillObjectiveTracker(this), this);
        getServer().getPluginManager().registerEvents(new com.aethor.aethorquests.gui.QuestEditorListener(this), this);
        getServer().getPluginManager().registerEvents(new com.aethor.aethorquests.listener.NpcInteractionListener(this), this);
        
        // Initialize talk tracker (not event-based)
        talkTracker = new TalkObjectiveTracker(this);
    }
    
    private void registerCommands() {
        PluginCommand questCmd = getCommand("quest");
        if (questCmd != null) {
            QuestCommand questCommand = new QuestCommand(this);
            questCmd.setExecutor(questCommand);
            questCmd.setTabCompleter(questCommand);
        }
        
        PluginCommand questAdminCmd = getCommand("questadmin");
        if (questAdminCmd != null) {
            QuestAdminCommand questAdminCommand = new QuestAdminCommand(this);
            questAdminCmd.setExecutor(questAdminCommand);
            questAdminCmd.setTabCompleter(questAdminCommand);
        }
    }
    
    private void startPeriodicTasks() {
        // Visit objective tracker
        visitTracker = new VisitObjectiveTracker(this);
        visitTracker.start();
        
        // Auto-save task
        long autoSaveTicks = getConfig().getLong("storage.autoSaveTicks", 6000);
        if (autoSaveTicks > 0) {
            autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                if (debug) {
                    getLogger().info("Auto-saving player data...");
                }
                playerDataStore.saveAll();
            }, autoSaveTicks, autoSaveTicks);
        }
    }
    
    private void logBanner() {
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("   ___       _   _             ____                  _       ");
        getLogger().info("  / _ \\  ___| |_| |__   ___  _ __ / ___|  _   _  ___ ___| |_ ___ ");
        getLogger().info(" / /_\\ \\/ _ \\ __| '_ \\ / _ \\| '__\\ ___ \\ | | | |/ _ / __| __/ __|");
        getLogger().info("/ /_\\\\ \\  __/ |_| | | | (_) | |   ___) || |_| |  __\\__ \\ |_\\__ \\");
        getLogger().info("\\____/ \\___|\\__|_| |_|\\___/|_|  |____/  \\__,_|\\___|___/\\__|___/");
        getLogger().info("");
        getLogger().info("  Production Quest System for Paper 1.21.8");
        getLogger().info("  Version: " + getDescription().getVersion());
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    // Getters
    
    public QuestManager getQuestManager() {
        return questManager;
    }
    
    public PlayerDataStore getPlayerDataStore() {
        return playerDataStore;
    }
    
    public QuestUI getQuestUI() {
        return questUI;
    }
    
    public AethorNpcHook getNpcHook() {
        return npcHook;
    }
    
    public TalkObjectiveTracker getTalkTracker() {
        return talkTracker;
    }
    
    public boolean isDebug() {
        return debug;
    }
}
