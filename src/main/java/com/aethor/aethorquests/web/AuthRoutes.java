package com.aethor.aethorquests.web;

import com.aethor.aethorquests.AethorQuestsPlugin;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Authentication routes for web interface
 */
public class AuthRoutes {
    private final AethorQuestsPlugin plugin;
    private final WebServer webServer;
    private final Gson gson = new Gson();
    
    public AuthRoutes(AethorQuestsPlugin plugin, WebServer webServer) {
        this.plugin = plugin;
        this.webServer = webServer;
    }
    
    public void register(Javalin app) {
        app.post("/api/auth/login", this::handleLogin);
        app.post("/api/auth/logout", this::handleLogout);
        app.get("/api/auth/verify", this::handleVerify);
    }
    
    private void handleLogin(Context ctx) {
        Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
        String username = body.get("username");
        String password = body.get("password");
        
        // Simple authentication - check if player is online and has permission
        var player = Bukkit.getServer().getPlayer(username);
        if (player != null && player.hasPermission("aethorquests.admin.webpanel")) {
            // Generate session token
            String token = UUID.randomUUID().toString();
            webServer.getSessions().put(token, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("username", username);
            
            ctx.json(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Invalid credentials or insufficient permissions");
            ctx.status(401).json(response);
        }
    }
    
    private void handleLogout(Context ctx) {
        String token = ctx.header("Authorization");
        if (token != null) {
            webServer.getSessions().remove(token);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        ctx.json(response);
    }
    
    private void handleVerify(Context ctx) {
        String token = ctx.header("Authorization");
        String username = webServer.getSessions().get(token);
        
        if (username != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            ctx.json(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            ctx.status(401).json(response);
        }
    }
    
    public static boolean isAuthenticated(Context ctx, WebServer webServer) {
        String token = ctx.header("Authorization");
        return token != null && webServer.getSessions().containsKey(token);
    }
}
