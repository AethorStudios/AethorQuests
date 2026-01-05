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
import org.bukkit.persistence.PersistentDataType;

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
        
        // Check if entity is an NPC
        if (!isNpc(entity)) {
            return;
        }
        
        // Get NPC from API
        Optional<Npc> npcOpt = plugin.getNpcHook().getApi().getAllNpcs().stream()
            .filter(npc -> npc.getEntityUuid() != null && npc.getEntityUuid().equals(entity.getUniqueId()))
            .findFirst();
        
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
        
        // Check if entity is an NPC
        if (!isNpc(entity)) {
            return;
        }
        
        // Check if damager is a player
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        
        // Get NPC from API
        Optional<Npc> npcOpt = plugin.getNpcHook().getApi().getAllNpcs().stream()
            .filter(npc -> npc.getEntityUuid() != null && npc.getEntityUuid().equals(entity.getUniqueId()))
            .findFirst();
        
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
    
    private boolean isNpc(Entity entity) {
        // Check scoreboard tag
        if (entity.getScoreboardTags().contains("aethornpcs:npc")) {
            return true;
        }
        
        // Check PDC (need to get the key from API)
        try {
            return entity.getPersistentDataContainer().has(
                plugin.getNpcHook().getApi().getNpcIdKey(), 
                PersistentDataType.STRING
            );
        } catch (Exception e) {
            return false;
        }
    }
}
