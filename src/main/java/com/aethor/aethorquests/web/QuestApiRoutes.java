package com.aethor.aethorquests.web;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.aethor.aethorquests.manager.QuestFileManager;
import com.aethor.aethorquests.model.QuestDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API routes for quest management
 */
public class QuestApiRoutes {
    private final AethorQuestsPlugin plugin;
    private final WebServer webServer;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final QuestFileManager fileManager;
    
    public QuestApiRoutes(AethorQuestsPlugin plugin, WebServer webServer) {
        this.plugin = plugin;
        this.webServer = webServer;
        this.fileManager = new QuestFileManager(plugin);
    }
    
    public void register(Javalin app) {
        // Quest CRUD operations
        app.get("/api/quests", this::getAllQuests);
        app.get("/api/quests/{id}", this::getQuest);
        app.post("/api/quests", this::createQuest);
        app.put("/api/quests/{id}", this::updateQuest);
        app.delete("/api/quests/{id}", this::deleteQuest);
        
        // Quest by NPC
        app.get("/api/npcs/{npcId}/quests", this::getQuestsByNpc);
    }
    
    private void getAllQuests(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        var quests = new ArrayList<>(plugin.getQuestManager().getAllQuests());
        ctx.json(Map.of("quests", quests));
    }
    
    private void getQuest(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        String id = ctx.pathParam("id");
        QuestDefinition quest = plugin.getQuestManager().getQuest(id);
        
        if (quest != null) {
            ctx.json(quest);
        } else {
            ctx.status(404).json(Map.of("error", "Quest not found"));
        }
    }
    
    private void createQuest(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        try {
            QuestDefinition quest = gson.fromJson(ctx.body(), QuestDefinition.class);
            
            // Save to file and reload
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                fileManager.saveQuest(quest);
                plugin.getQuestManager().reload();
            });
            
            ctx.status(201).json(Map.of("success", true, "quest", quest));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Invalid quest data: " + e.getMessage()));
        }
    }
    
    private void updateQuest(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        String id = ctx.pathParam("id");
        QuestDefinition existingQuest = plugin.getQuestManager().getQuest(id);
        
        if (existingQuest == null) {
            ctx.status(404).json(Map.of("error", "Quest not found"));
            return;
        }
        
        try {
            QuestDefinition updatedQuest = gson.fromJson(ctx.body(), QuestDefinition.class);
            // Note: Quest ID cannot be changed via this endpoint
            
            // Save to file and reload
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                fileManager.saveQuest(updatedQuest);
                plugin.getQuestManager().reload();
            });
            
            ctx.json(Map.of("success", true, "quest", updatedQuest));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Invalid quest data: " + e.getMessage()));
        }
    }
    
    private void deleteQuest(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        String id = ctx.pathParam("id");
        QuestDefinition quest = plugin.getQuestManager().getQuest(id);
        
        if (quest == null) {
            ctx.status(404).json(Map.of("error", "Quest not found"));
            return;
        }
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            fileManager.removeQuest(id);
            plugin.getQuestManager().reload();
        });
        
        ctx.json(Map.of("success", true));
    }
    
    private void getQuestsByNpc(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        String npcId = ctx.pathParam("npcId");
        var quests = plugin.getQuestManager().getQuestsByGiverNpcId(npcId);
        ctx.json(Map.of("quests", quests));
    }
}
