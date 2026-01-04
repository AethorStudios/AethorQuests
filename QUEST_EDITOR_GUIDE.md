# Quest Editor GUI - Feature Documentation

## Overview

The Quest Editor GUI is an in-game graphical interface that allows server administrators to create, edit, and delete quests without manually editing YAML files. This feature was added to AethorQuests to streamline quest management and make it more accessible.

## How to Access

1. **Permission Required**: `aethorquests.admin.edit` (default: op)
2. **Trigger**: Hold **Shift** and **right-click** any AethorNPC
3. The quest editor GUI will open automatically

## Architecture

### New Classes Added

1. **QuestEditorGUI** (`gui/QuestEditorGUI.java`)
   - Main quest list interface
   - Displays all quests for the clicked NPC
   - Handles quest creation and deletion

2. **QuestDetailsEditorGUI** (`gui/QuestDetailsEditorGUI.java`)
   - Detail view for individual quests
   - Provides buttons for editing quest properties
   - Shows current quest configuration

3. **QuestCreatorGUI** (`gui/QuestCreatorGUI.java`)
   - Quest creation wizard
   - Generates unique quest IDs
   - Creates default quest templates

4. **QuestEditorListener** (`gui/QuestEditorListener.java`)
   - Handles all inventory click events for the GUI
   - Manages chat input sessions
   - Coordinates between GUI screens

5. **QuestFileManager** (`manager/QuestFileManager.java`)
   - Manages reading/writing quests to `quests.yml`
   - Provides methods for adding, updating, and removing quests
   - Handles YAML serialization

### Integration Points

- **AethorNpcHook**: Modified to detect shift+right-click and open the GUI
- **AethorQuestsPlugin**: Registers the QuestEditorListener
- **plugin.yml**: Added `aethorquests.admin.edit` permission

## Features

### Current Functionality (v1.0)

✅ **Quest Browsing**
- View all quests assigned to an NPC
- See quest titles, IDs, descriptions, and objective counts

✅ **Quest Creation**
- Create new quests via chat input
- Auto-generates quest ID from title
- Creates quest with sensible defaults:
  - Basic description template
  - Assigned to the clicked NPC
  - 100 XP reward
  - Minimum level 1 requirement

✅ **Quest Deletion**
- Shift+click quests to delete them
- Confirms deletion and reloads automatically

✅ **Title Editing**
- Click "Edit Title" to change quest name via chat

✅ **Description Editing**
- Click "Edit Description" to modify quest description
- Supports multi-line descriptions

✅ **Auto-Save**
- All changes automatically save to `quests.yml`
- Quest system automatically reloads after changes
- No manual `/questadmin reload` needed

### Planned Features (Future Versions)

⏳ **Objective Editor**
- Visual interface for adding/removing objectives
- Dropdown selectors for objective types
- Location picker for VISIT objectives

⏳ **Reward Editor**
- Item reward configuration via inventory GUI
- Command editor with variable support
- Money and XP sliders

⏳ **Requirements Editor**
- Level requirement slider
- Prerequisite quest selector
- Permission requirement input

⏳ **Dialogue Management**
- NPC dialogue editor for quest interactions
- Multi-line dialogue support
- Color code support

## Technical Details

### GUI Structure

```
Quest Editor Menu (54 slots)
├── Slot 0-44: Quest book items
├── Slot 49: Create New Quest button
└── Slot 53: Close button

Quest Details Editor (27 slots)
├── Slot 10: Edit Title
├── Slot 11: Edit Description
├── Slot 12: Edit Objectives
├── Slot 13: Edit Rewards
├── Slot 14: Edit Requirements
├── Slot 18: Back button
└── Slot 22: Save button
```

### Data Flow

1. Player shift+right-clicks NPC
2. `AethorNpcHook.handleNpcInteraction()` detects shift+click
3. Checks `aethorquests.admin.edit` permission
4. Opens `QuestEditorGUI` with NPC ID
5. GUI displays quests from `QuestManager.getQuestsByGiverNpcId()`
6. Player clicks item in GUI
7. `QuestEditorListener` handles click event
8. For edits: Opens chat input session
9. Player enters data in chat
10. `QuestEditorListener.onPlayerChat()` processes input
11. `QuestFileManager` saves changes to `quests.yml`
12. `QuestManager.reload()` reloads all quests

### File Persistence

The `QuestFileManager` handles all YAML operations:

```java
// Add new quest
questFileManager.addQuest(quest);

// Update existing quest
questFileManager.updateQuest(quest);

// Delete quest
questFileManager.removeQuest(questId);

// Save all loaded quests
questFileManager.saveAllQuests();
```

After any file operation, the plugin automatically calls:
```java
plugin.getQuestManager().reload();
```

This ensures in-memory quest data stays synchronized with the file.

## Usage Examples

### Example 1: Creating a Simple Quest

1. Shift+right-click the "village_elder" NPC
2. Click "Create New Quest"
3. Type in chat: `Find the Lost Sword`
4. Quest created with ID: `find_the_lost_sword`
5. Click on the new quest book
6. Click "Edit Description"
7. Type: `The blacksmith's sword was stolen!`
8. Type: `done`
9. Click "Save Changes"

Result: New quest appears in `quests.yml`:
```yaml
find_the_lost_sword:
  id: find_the_lost_sword
  title: "Find the Lost Sword"
  description:
    - "The blacksmith's sword was stolen!"
  giverNpcId: "village_elder"
  requirements:
    minLevel: 1
    requiredQuestsCompleted: []
  objectives: []
  rewards:
    xp: 100
    money: 0.0
    commands: []
    items: []
```

### Example 2: Deleting a Quest

1. Shift+right-click any NPC
2. Find the quest you want to delete
3. Hold Shift and click on the quest book
4. Quest is removed from `quests.yml`
5. GUI automatically refreshes

## Best Practices

### For Server Owners

1. **Use GUI for Basic Management**
   - Quest creation
   - Title/description changes
   - Quick deletions

2. **Use YAML for Complex Configuration**
   - Multiple objectives
   - Custom rewards
   - Complex requirement chains

3. **Always Have Backups**
   - The GUI modifies `quests.yml` directly
   - Keep backup copies before making major changes

### For Developers Extending This Feature

1. **Editor Sessions**: Always use `EditorSession` to track player state across inventory closes
2. **Plain Text Serialization**: Use `PlainTextComponentSerializer` to extract text from Adventure components
3. **Async Chat Events**: Chat input is async, use `runTask()` to open GUIs from chat listeners
4. **NPC ID Extraction**: Store NPC ID in `EditorSession` when opening the main menu

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `aethorquests.admin` | Parent admin permission | `op` |
| `aethorquests.admin.edit` | Access quest editor GUI | `op` |

The `aethorquests.admin` permission includes `aethorquests.admin.edit` as a child permission.

## Configuration

No configuration is required for the GUI editor. It works out of the box once the plugin is installed.

## Known Limitations

1. **Objective Editing**: Currently requires manual YAML editing
2. **Reward Configuration**: Advanced rewards must be configured in YAML
3. **Item Rewards**: No GUI for item configuration yet
4. **Dialogue System**: NPC dialogue editing not yet implemented
5. **Multi-line Chat Input**: Description editing is line-by-line, not multi-line editor

## Troubleshooting

### GUI Won't Open

- **Check Permission**: Ensure you have `aethorquests.admin.edit`
- **Verify NPC**: Make sure you're clicking an AethorNPC
- **Check Shift**: Must hold Shift while right-clicking
- **API Integration**: Ensure AethorNPCS is installed and loaded

### Quest Not Saving

- **File Permissions**: Check that `quests.yml` is writable
- **Syntax Errors**: Check console for YAML parsing errors
- **Reload Manually**: Try `/questadmin reload` if auto-reload fails

### Quest ID Conflicts

- The system automatically appends `_1`, `_2`, etc. to duplicate IDs
- If you see unexpected IDs, check for name collisions in `quests.yml`

## Future Enhancements

### Short-term (Next Release)

- Complete objective editor with all 4 types (KILL, TALK, COLLECT, VISIT)
- Basic reward editor (XP and money only)
- Level requirement editor

### Medium-term

- Item reward configuration via inventory GUI
- Command reward editor with variable autocomplete
- Prerequisite quest selector with dependency tree

### Long-term

- Dialogue tree editor for NPC interactions
- Quest chain visualizer
- Import/export quests to shareable format
- Multi-language support for quest text

## API for Developers

If you want to programmatically open the quest editor:

```java
AethorQuestsPlugin plugin = ...; // Get plugin instance
Player player = ...; // Player to show GUI to
String npcId = "village_elder"; // NPC ID

QuestEditorGUI editor = new QuestEditorGUI(plugin, player, npcId);
editor.open();
```

Check permission first:
```java
if (player.hasPermission("aethorquests.admin.edit")) {
    new QuestEditorGUI(plugin, player, npcId).open();
}
```

## Changelog

### v1.0.0 - Initial Release
- Added quest editor GUI system
- Implemented quest creation wizard
- Added shift+right-click detection
- Created file persistence layer
- Integrated with AethorNPCS hook
- Added admin permission: `aethorquests.admin.edit`
