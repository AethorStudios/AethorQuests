# Quest Marker System - DecentHolograms Integration Guide

## Overview
The quest marker system is fully implemented with placeholder methods for DecentHolograms. To complete the integration, you need to:

1. Add DecentHolograms as a dependency
2. Implement the three hologram methods in `MarkerHologramController`

## Step 1: Add DecentHolograms Dependency

Add to `pom.xml`:

```xml
<repositories>
    <!-- Existing repositories -->
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- Existing dependencies -->
    <dependency>
        <groupId>com.github.decentsoftware-eu</groupId>
        <artifactId>decentholograms</artifactId>
        <version>2.8.6</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Add to `plugin.yml`:

```yaml
depend: [AethorNPCS, DecentHolograms]
```

## Step 2: Implement Hologram Methods

In `MarkerHologramController.java`, replace the TODO methods with actual DecentHolograms API calls:

### Method 1: createHologram

```java
private void createHologram(String name, Location location, String text, Player viewer) {
    try {
        // Create hologram using DecentHolograms API
        DHAPI.createHologram(name, location, false); // false = not saved to file
        DHAPI.setHologramLine(name, 0, text);
        
        // Make it only visible to this player
        DHAPI.addHologramViewer(name, viewer);
        
        if (plugin.isDebug()) {
            plugin.getLogger().info("Created marker hologram '" + name + "' at " + 
                    location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + 
                    " for player " + viewer.getName());
        }
    } catch (Exception e) {
        plugin.getLogger().warning("Failed to create hologram: " + e.getMessage());
    }
}
```

### Method 2: updateHologram

```java
private void updateHologram(String name, Location location, String text) {
    try {
        // Check if hologram exists
        if (DHAPI.getHologram(name) == null) {
            return;
        }
        
        // Update position and text
        DHAPI.moveHologram(name, location);
        DHAPI.setHologramLine(name, 0, text);
        
        if (plugin.isDebug()) {
            plugin.getLogger().info("Updated marker hologram '" + name + "'");
        }
    } catch (Exception e) {
        plugin.getLogger().warning("Failed to update hologram: " + e.getMessage());
    }
}
```

### Method 3: deleteHologram

```java
private void deleteHologram(String name) {
    try {
        DHAPI.removeHologram(name);
        
        if (plugin.isDebug()) {
            plugin.getLogger().info("Deleted marker hologram '" + name + "'");
        }
    } catch (Exception e) {
        plugin.getLogger().warning("Failed to delete hologram: " + e.getMessage());
    }
}
```

### Required Import

Add to the imports at the top of `MarkerHologramController.java`:

```java
import eu.decentsoftware.holograms.api.DHAPI;
```

## System Architecture

### Components Created

1. **MarkerState** - Enum with 3 states: NONE, AVAILABLE (green !), TURNIN (yellow ?)
2. **QuestMarkerService** - Computes marker state based on quest availability and player progress
3. **MarkerHologramController** - Manages per-player holograms with 48-block visibility
4. **MarkerUpdateListener** - Handles automatic updates on player movement and events

### Automatic Updates

Markers automatically update when:
- Player joins the server
- Player changes worlds
- Player teleports
- Player moves to a new chunk
- Quest is accepted
- Quest objective is completed
- Quest is turned in
- Quest is abandoned
- Every 2 seconds (periodic refresh task)

### Performance Optimizations

- **Range limiting**: Only shows markers within 48 blocks
- **Chunk-based updates**: Only refreshes when player moves to new chunk
- **State tracking**: Avoids flicker by only updating when state changes
- **Per-player holograms**: Each player has their own hologram instances
- **Automatic cleanup**: Removes holograms on player quit

### Configuration

The system uses these constants in `MarkerHologramController`:

```java
private static final double MARKER_Y_OFFSET = 2.5; // Above NPC nameplate
private static final double VISIBILITY_RANGE = 48.0;
private static final long REFRESH_INTERVAL_TICKS = 40L; // 2 seconds
```

Adjust these values as needed for your server.

## Testing

After implementing the DecentHolograms integration:

1. Start server with both AethorNPCS and DecentHolograms installed
2. Approach an NPC with quests - should see green "!" marker above their head
3. Accept the quest - marker should disappear
4. Complete the quest objectives - yellow "?" marker should appear
5. Turn in the quest - marker should disappear
6. Move far away (>48 blocks) - markers should not be visible

## Troubleshooting

### Markers not showing
- Check console for errors
- Verify DecentHolograms is installed and enabled
- Enable debug mode: `debug: true` in config.yml
- Check logs for hologram creation messages

### Markers flickering
- This should be prevented by state tracking
- If it occurs, increase `REFRESH_INTERVAL_TICKS`

### Performance issues
- Reduce `VISIBILITY_RANGE`
- Increase `REFRESH_INTERVAL_TICKS`
- Check number of quest NPCs (system is optimized for normal amounts)

## Notes

- Holograms are created with `false` (not saved to file) to avoid persistence
- Each hologram has a unique name: `quest_marker_{playerUUID}_{npcId}`
- Markers are positioned 2.5 blocks above NPC location (above nameplate)
- The system works independently from global NPC name holograms
