package com.aethor.aethorquests.web;

import com.aethor.aethorquests.AethorQuestsPlugin;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Embedded web server for quest management interface
 */
public class WebServer {
    private final AethorQuestsPlugin plugin;
    private Javalin app;
    private final int port;
    private final String host;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    
    public WebServer(AethorQuestsPlugin plugin, String host, int port) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
    }
    
    public void start() {
        try {
            app = Javalin.create(config -> {
                config.showJavalinBanner = false;
                config.staticFiles.add("/web", Location.CLASSPATH);
            }).start(host, port);
            
            // Register routes
            new QuestApiRoutes(plugin, this).register(app);
            new NpcApiRoutes(plugin, this).register(app);
            new AuthRoutes(plugin, this).register(app);
            
            plugin.getLogger().info("═══════════════════════════════════════════════════");
            plugin.getLogger().info("  Web Interface Started!");
            plugin.getLogger().info("  URL: http://" + host + ":" + port);
            plugin.getLogger().info("═══════════════════════════════════════════════════");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start web server", e);
        }
    }
    
    public void stop() {
        if (app != null) {
            app.stop();
            plugin.getLogger().info("Web server stopped");
        }
    }
    
    public Map<String, String> getSessions() {
        return sessions;
    }
    
    public AethorQuestsPlugin getPlugin() {
        return plugin;
    }
}
