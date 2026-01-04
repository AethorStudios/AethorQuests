package com.aethor.aethorquests.web;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API routes for NPC management
 */
public class NpcApiRoutes {
    private final AethorQuestsPlugin plugin;
    private final WebServer webServer;
    private final Gson gson = new Gson();
    
    public NpcApiRoutes(AethorQuestsPlugin plugin, WebServer webServer) {
        this.plugin = plugin;
        this.webServer = webServer;
    }
    
    public void register(Javalin app) {
        app.get("/api/npcs", this::getAllNpcs);
        app.get("/api/npcs/{id}", this::getNpc);
    }
    
    private void getAllNpcs(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        List<Map<String, Object>> npcs = new ArrayList<>();
        
        // Get NPCs from AethorNPCS API
        if (plugin.getNpcHook().isEnabled() && plugin.getNpcHook().getApi() != null) {
            try {
                var api = plugin.getNpcHook().getApi();
                var allNpcs = api.getAllNpcs();
                
                for (var npc : allNpcs) {
                    Map<String, Object> npcData = new HashMap<>();
                    npcData.put("id", npc.getId());
                    npcData.put("displayName", npc.getDisplayName());
                    npcData.put("spawned", npc.isSpawned());
                    npcData.put("questCount", plugin.getQuestManager().getQuestsByGiverNpcId(npc.getId()).size());
                    npcs.add(npcData);
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Failed to fetch NPCs: " + e.getMessage()));
                return;
            }
        }
        
        ctx.json(Map.of("npcs", npcs));
    }
    
    private void getNpc(Context ctx) {
        if (!AuthRoutes.isAuthenticated(ctx, webServer)) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        
        String id = ctx.pathParam("id");
        
        if (plugin.getNpcHook().isEnabled() && plugin.getNpcHook().getApi() != null) {
            try {
                var api = plugin.getNpcHook().getApi();
                var npcOpt = api.getNpc(id);
                
                if (npcOpt.isPresent()) {
                    var npc = npcOpt.get();
                    Map<String, Object> npcData = new HashMap<>();
                    npcData.put("id", npc.getId());
                    npcData.put("displayName", npc.getDisplayName());
                    npcData.put("spawned", npc.isSpawned());
                    npcData.put("questCount", plugin.getQuestManager().getQuestsByGiverNpcId(npc.getId()).size());
                    
                    ctx.json(npcData);
                } else {
                    ctx.status(404).json(Map.of("error", "NPC not found"));
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Failed to fetch NPC: " + e.getMessage()));
            }
        } else {
            ctx.status(503).json(Map.of("error", "AethorNPCS API not available"));
        }
    }
}
