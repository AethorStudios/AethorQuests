package com.aethor.aethorquests.listener;

import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.api.payload.InteractPayload;
import com.aethor.aethorquests.AethorQuestsPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Optional;

/**
 * Listens for player interactions with NPC entities from AethorNPCS
 */
public class NpcInteractionListener implements Listener {
    private final AethorQuestsPlugin plugin;
    
    public NpcInteractionListener(AethorQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        
        // Try to get NPC from API using entity UUID
        Optional<Npc> npcOpt = plugin.getNpcHook().getApi().getNpcByEntity(entity.getUniqueId());
        
        if (npcOpt.isEmpty()) {
            return;
        }
        
        Npc npc = npcOpt.get();
        
        // Handle the interaction
        plugin.getNpcHook().handleNpcInteraction(
            player,
            npc.getId(),
            InteractPayload.ClickType.RIGHT_CLICK
        );
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        
        // Check if damager is a player
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        
        // Try to get NPC from API using entity UUID
        Optional<Npc> npcOpt = plugin.getNpcHook().getApi().getNpcByEntity(entity.getUniqueId());
        
        if (npcOpt.isEmpty()) {
            return;
        }
        
        Npc npc = npcOpt.get();
        
        // Handle the interaction
        plugin.getNpcHook().handleNpcInteraction(
            player,
            npc.getId(),
            InteractPayload.ClickType.LEFT_CLICK
        );
        
        // Cancel damage to NPCs
        event.setCancelled(true);
    }
}
