package com.aethor.aethorquests.listener;

import com.aethor.aethornpcs.plugin.event.AethorNpcInteractEvent;
import com.aethor.aethorquests.AethorQuestsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for NPC interaction events from AethorNPCS
 */
public class NpcInteractionListener implements Listener {
    private final AethorQuestsPlugin plugin;
    
    public NpcInteractionListener(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onNpcInteract(AethorNpcInteractEvent event) {
        // Delegate to the NPC hook handler
        plugin.getNpcHook().handleNpcInteraction(
            event.getPlayer(),
            event.getNpc().getId(),
            event.getPayload().getClickType()
        );
    }
}
