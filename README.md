# AethorQuests

**Production-quality quest system for Paper Minecraft 1.21.8**

[![Build](https://img.shields.io/github/actions/workflow/status/AethorStudios/AethorQuests/build.yml?branch=main)](https://github.com/AethorStudios/AethorQuests/actions)
[![Release](https://img.shields.io/github/v/release/AethorStudios/AethorQuests)](https://github.com/AethorStudios/AethorQuests/releases/latest)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red.svg)](LICENSE)

AethorQuests is a comprehensive quest plugin that seamlessly integrates with **AethorNPCS** to provide a rich questing experience. Players can interact with NPCs to accept, track, and complete quests with multiple objective types.

---

## 📋 Features

- **Full AethorNPCS Integration**: Consumes AethorNPCS API via Bukkit ServicesManager
- **In-Game Quest Editor GUI**: Command-based GUI to edit quests for any NPC from anywhere
- **Multiple Objective Types**:
  - 🗡️ **KILL**: Kill specific mobs (vanilla or MythicMobs)
  - 💬 **TALK**: Interact with specific NPCs
  - 📦 **COLLECT**: Gather items
  - 🗺️ **VISIT**: Travel to locations
- **Rich Text UI**: Adventure-based chat interface with clickable components
- **Quest Requirements**: Level requirements and prerequisite quests
- **Progress Tracking**: Real-time progress updates and action bar notifications
- **Flexible Rewards**: XP, items, and custom commands
- **Persistent Storage**: Per-player quest progress saved to YAML
- **Admin Tools**: Full admin commands for quest management
- **File Persistence**: GUI changes automatically save to quests.yml

---

## 🔧 Requirements

- **Server**: Paper 1.21.8 (or compatible)
- **Java**: 21+
- **Dependencies**: 
  - **AethorNPCS** (required) - Must be installed on the server

---

## 📦 Installation

### From Releases (Recommended)

1. **Install AethorNPCS** on your server first
2. Download the latest **AethorQuests.jar** from [Releases](https://github.com/YOUR_ORG/AethorQuests/releases/latest)
3. Place the jar in your `plugins/` folder
4. Restart the server
5. Configure quests in `plugins/AethorQuests/quests.yml`

### From Source

```bash
git clone https://github.com/YOUR_ORG/AethorQuests.git
cd AethorQuests
./mvnw clean package
# Output: target/AethorQuests-1.0.0-SNAPSHOT.jar
```

---

## 🎮 How It Works

### For Players

1. **Find NPCs with Quests**: Right-click NPCs that have quests assigned
2. **View Available Quests**: See quest details, requirements, and rewards
3. **Accept Quests**: Click the `[Accept]` button to start a quest
4. **Track Progress**: Use `/quest journal` to see active quests
5. **Complete Objectives**: 
   - Kill mobs for KILL objectives
   - Talk to NPCs for TALK objectives
   - Collect items for COLLECT objectives
   - Visit locations for VISIT objectives
6. **Turn In**: Return to the quest giver NPC when complete

### For Server Owners

1. **Define Quests** in `quests.yml` OR use the in-game GUI editor
2. **Bind Quests to NPCs** using the `giverNpcId` field (must match AethorNPCS NPC ID)
3. **Configure Objectives** with multiple types
4. **Set Rewards** including XP, items, and console commands

#### Using the In-Game Quest Editor

1. **Permission Required**: `aethorquests.admin.edit` (default: op)
2. **Open Editor**: Use `/questadmin editor` command
3. **Select NPC**: Click on an NPC in the list to edit its quests
4. **Create Quests**: Click the "Create New Quest" button
5. **Edit Quests**: Click on existing quests to modify them
6. **Delete Quests**: Shift+click on quests to remove them
7. **Auto-Save**: All changes automatically save to `quests.yml`

> **Benefits**: Edit quests from anywhere, no need to be near NPCs. See all NPCs at once in a convenient list.

---

## 📝 Commands

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/quest` | Opens quest journal | `aethorquests.use` |
| `/quest journal` | View active quests | `aethorquests.use` |
| `/quest view <id>` | View quest details | `aethorquests.use` |
| `/quest accept <id>` | Accept a quest | `aethorquests.use` |
| `/quest turnin <id>` | Turn in completed quest | `aethorquests.use` |
| `/quest abandon <id>` | Abandon a quest | `aethorquests.use` |
| `/quest track <id>` | Track a quest | `aethorquests.use` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/questadmin editor` | Open quest editor GUI | `aethorquests.admin.edit` |
| `/questadmin reload` | Reload quests from file | `aethorquests.admin` |
| `/questadmin give <player> <id>` | Give quest to player | `aethorquests.admin` |
| `/questadmin complete <player> <id>` | Complete quest for player | `aethorquests.admin` |
| `/questadmin reset <player> <id>` | Reset quest for player | `aethorquests.admin` |
| `/questadmin list` | List all quests | `aethorquests.admin` |
| `/questadmin info <player> <id>` | View player quest info | `aethorquests.admin` |

### Quest Editor GUI

| Command | Description | Permission |
|---------|-------------|---------|
| `/questadmin editor` | Opens NPC selector to edit quests | `aethorquests.admin.edit` (default: op) |

**Usage**: Run the command to see all NPCs with quests, then click one to edit.

---

## 🎨 Quest Editor GUI Guide

The in-game quest editor provides a user-friendly interface for creating and managing quests without editing YAML files.

### Opening the Editor

1. Run `/questadmin editor` (or `/qa editor`, `/qa edit`, `/qa gui`)
3. The NPC selector will open

### NPC Selector Menu

The main menu displays:
- **All NPCs that have quests** (as player head icons)
- **Create Quest for New NPC button** (name tag icon)
- **Close button** (barrier icon)

Click on any NPC to edit its quests, or click the name tag to create quests for a new NPC ID.

### Creating Quests for a New NPC

1. Click the **"+ Create Quest for New NPC"** button
2. Enter the NPC ID in chat (must match an AethorNPCS NPC ID)
3. The quest editor for that NPC will openitable book icon)
- **Close button** (barrier icon)

### Creating a New Quest

1. Click the **"+ Create New Quest"** button
2. Enter the quest title in chat
3. A new quest will be created with default values:
   - Auto-generated quest ID from the title
   - Basic description template
   - Assigned to the NPC you clicked
   - Default rewards (100 XP)
   - Minimum level 1

### Editing an Existing Quest

1. Click on any quest book in the main menu
2. The quest details editor opens with options for:
   - **Edit Title**: Change the quest name (chat input)
   - **Edit Description**: Modify quest description (chat input)
   - **Edit Objectives**: Manage quest objectives (coming soon - use YAML for now)
   - **Edit Rewards**: Configure XP, money, commands (coming soon - use YAML for now)
   - **Edit Requirements**: Set level and prerequisite quests (coming soon - use YAML for now)
   - **Save Changes**: Saves all modifications to `quests.yml`

### Deleting a Quest

1. In the main quest editor menu
2. Hold **Shift** and **click** on the quest you want to delete
3. The quest will be removed from the file and reloaded

### Auto-Save and Reload

- All GUI actions automatically save to `quests.yml`
- The quest system automatically reloads after changes
- No need to manually run `/questadmin reload`
- Changes persist across server restarts

### Current Limitations

The GUI editor is in its initial version and currently supports:
- ✅ Creating new quests
- ✅ Editing quest titles
- ✅ Editing quest descriptions  
- ✅ Deleting quests
- ✅ Viewing quest properties

For advanced features, you can still edit `quests.yml` directly:
- Complex objective configurations
- Multiple reward types
- Item rewards
- Custom requirement chains

---

## 📖 Quest Configuration

### Example Quest Definition

```yaml
quests:
  slime_hunter:
    id: slime_hunter
    title: "Slime Hunter"
    description:
      - "&7The village elder needs help dealing"
      - "&7with the slime infestation in the swamp."
      - "&7Hunt down 10 slimes and report back."
    giverNpcId: "village_elder"  # MUST match AethorNPCS NPC ID
    requirements:
      minLevel: 1
      requiredQuestsCompleted: []
    objectives:
      - type: KILL
        target: SLIME
        amount: 10
        description: "Kill slimes"
      - type: TALK
        target: "village_elder"
        description: "Return to the village elder"
    rewards:
      xp: 100
      money: 50.0
      commands:
        - "give {player} diamond 2"
      items: []
```

### Objective Types

#### KILL Objective
```yaml
- type: KILL
  target: ZOMBIE              # EntityType (vanilla) OR MythicMob internal name
  amount: 10
  description: "Kill 10 zombies"
```

For MythicMobs:
```yaml
- type: KILL
  target: "CorruptedGuardian"  # MythicMobs internal name
  amount: 1
  description: "Defeat the Corrupted Guardian"
```

#### TALK Objective
```yaml
- type: TALK
  target: "blacksmith"         # AethorNPCS NPC ID
  description: "Speak with the blacksmith"
```

#### COLLECT Objective
```yaml
- type: COLLECT
  target: IRON_INGOT           # Material type
  amount: 16
  description: "Collect 16 iron ingots"
```

#### VISIT Objective
```yaml
- type: VISIT
  world: "world"
  x: 100
  y: 64
  z: 200
  radius: 10.0
  description: "Visit the ancient ruins"
```

### Quest Requirements

```yaml
requirements:
  minLevel: 10                           # Minimum player level
  requiredQuestsCompleted:               # List of quest IDs that must be completed first
    - "slime_hunter"
    - "gather_resources"
```

### Rewards

```yaml
rewards:
  xp: 500                                # Experience points
  money: 100.0                           # Money (placeholder for economy plugins)
  commands:                              # Console commands to execute
    - "give {player} diamond_sword 1"
    - "broadcast &6{player} completed the quest!"
  items: []                              # ItemStack rewards (optional)
```

#### Placeholder Support in Commands
- `{player}` - Player name
- `{uuid}` - Player UUID

---

## ⚙️ Configuration

### config.yml

```yaml
# UI mode for quest interactions
uiMode: CHAT

# Enable debug logging
debug: false

# Require players to turn in quests at the giver NPC
turnInRequiresGiverNpc: true

# Visit objective checking settings
visitCheck:
  periodTicks: 10           # How often to check positions (20 ticks = 1 second)
  radiusDefault: 5.0        # Default radius if not specified in quest

# Quest tracking settings
tracking:
  showActionBar: true       # Show action bar updates
  actionBarUpdateTicks: 40  # How often to update action bar

# Storage settings
storage:
  autoSaveTicks: 6000       # Auto-save interval (6000 ticks = 5 minutes)
  saveOnQuit: true          # Save on player quit
```

---

## 🔗 Integration with AethorNPCS

AethorQuests is designed to **consume** the AethorNPCS API rather than directly manage NPCs.

### How Integration Works

1. **API Loading**: On plugin enable, AethorQuests retrieves the AethorNPCS API from the Bukkit ServicesManager:
   ```java
   ServicesManager sm = getServer().getServicesManager();
   AethorNpcApi api = sm.load(AethorNpcApi.class);
   ```

2. **API Structure**: The AethorNPCS API uses a payload-based system rather than Bukkit events:
   - `InteractPayload` - Contains player interaction data
   - `SpawnPayload` - Contains NPC spawn information
   - `DespawnPayload` - Contains NPC despawn information
   
   The plugin provides hooks that can be integrated based on how AethorNPCS exposes interaction callbacks.

3. **NPC Binding**: Quests are bound to NPCs via the `giverNpcId` field in quest definitions, which must match the NPC's ID in AethorNPCS

### Example Workflow

1. Create an NPC in AethorNPCS with ID `"village_elder"`
2. Create a quest in `quests.yml` with `giverNpcId: "village_elder"`
3. When players interact with the NPC, quest dialogs appear
4. Quest objectives can reference other NPCs by their AethorNPCS IDs

> **Note**: The specific integration mechanism depends on how AethorNPCS exposes interaction events/callbacks in your version. The plugin is designed to adapt to the API structure.

---

## 🗂️ File Structure

```
AethorQuests/
├── src/
│   └── main/
│       ├── java/com/aethor/aethorquests/
│       │   ├── AethorQuestsPlugin.java          # Main plugin class
│       │   ├── command/
│       │   │   ├── QuestCommand.java            # /quest command
│       │   │   └── QuestAdminCommand.java       # /questadmin command
│       │   ├── hook/
│       │   │   └── AethorNpcHook.java           # AethorNPCS API integration
│       │   ├── listener/
│       │   │   └── PlayerListener.java          # Player join/quit events
│       │   ├── manager/
│       │   │   ├── QuestManager.java            # Quest definition management
│       │   │   └── PlayerDataStore.java         # Player progress persistence
│       │   ├── model/
│       │   │   ├── Objective.java               # Objective data model
│       │   │   ├── ObjectiveType.java           # Objective types enum
│       │   │   ├── PlayerQuestState.java        # Player quest progress
│       │   │   ├── QuestDefinition.java         # Quest definition model
│       │   │   ├── QuestRequirements.java       # Quest requirements model
│       │   │   ├── QuestStatus.java             # Quest status enum
│       │   │   └── Reward.java                  # Reward data model
│       │   ├── tracker/
│       │   │   ├── CollectObjectiveTracker.java # COLLECT validation
│       │   │   ├── KillObjectiveTracker.java    # KILL tracking
│       │   │   ├── TalkObjectiveTracker.java    # TALK tracking
│       │   │   └── VisitObjectiveTracker.java   # VISIT tracking
│       │   └── ui/
│       │       └── QuestUI.java                 # Adventure text UI
│       └── resources/
│           ├── plugin.yml                       # Plugin metadata
│           ├── config.yml                       # Main configuration
│           └── quests.yml                       # Quest definitions
├── pom.xml                                      # Maven build file
└── README.md                                    # This file
```

---

## 🛠️ Building from Source

### Prerequisites
- Java 21 JDK
- AethorNPCS API jar (included: `aethornpcs-api-1.0.0.jar`)
- Maven wrapper included (no Maven installation required!)

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd AethorQuests
   ```

2. **Ensure AethorNPCS API jar is present**:
   - The file `aethornpcs-api-1.0.0.jar` should be in the project root
   - Already included in the repository

3. **Build with Maven wrapper**:
   
   **Windows**:
   ```bash
   .\mvnw.cmd clean package
   ```
   
   **Linux/Mac**:
   ```bash
   ./mvnw clean package
   ```

4. **Output**: Find the jar in `target/AethorQuests-1.0.0-SNAPSHOT.jar`

> **Note**: The first build will download Maven automatically. Subsequent builds will be faster.

---

## 🎯 Objective Tracking Details

### KILL Objectives
- Tracks `EntityDeathEvent` for vanilla mobs
- Supports MythicMobs via persistent data container detection
- Progress increments per kill
- Real-time notifications with progress counter

### TALK Objectives
- Completes when player right-clicks the specified NPC
- Instant completion (no progress counter)
- Can chain multiple TALK objectives

### COLLECT Objectives
- Validated at turn-in time
- Checks player inventory for required items
- Items are consumed when quest is turned in
- Must have all items before completion

### VISIT Objectives
- Periodic position checking (configurable interval)
- Checks if player is within radius of target location
- Completes immediately when player enters the area
- World-aware (player must be in correct world)

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `aethorquests.use` | Use player quest commands | `true` |
| `aethorquests.admin` | Use admin commands | `op` |

---

## 📊 Data Storage

### Player Data Location
- Path: `plugins/AethorQuests/playerdata/<uuid>.yml`
- Format: YAML
- Contains: Quest status, objectives, progress, timestamps

### Example Player Data
```yaml
quests:
  slime_hunter:
    status: ACTIVE
    objectiveIndex: 0
    objectiveProgress: 5
    acceptedTimestamp: 1704398400000
    completedTimestamp: 0
    turnedInTimestamp: 0
```

---

## 🐛 Debugging

Enable debug mode in `config.yml`:
```yaml
debug: true
```

Debug logging includes:
- Quest loading details
- NPC interaction events
- Player data loading/saving
- Objective progress updates

---

## 🤝 Support

For issues, questions, or suggestions:
- Check the [wiki](link-to-wiki) for detailed guides
- Join our [Discord](link-to-discord) for community support
- Report bugs on [GitHub Issues](link-to-issues)

---

## 📜 License

This project is licensed under the MIT License. See LICENSE file for details.

---

## 🙏 Credits

**Author**: Aethor Development Team  
**Version**: 1.0.0-SNAPSHOT  
**Built for**: Paper Minecraft 1.21.8  

Special thanks to the Paper and Adventure library teams for their excellent APIs.

---

## 🚀 Roadmap

- [ ] Inventory GUI mode (alternative to chat UI)
- [ ] MySQL/PostgreSQL storage backend
- [ ] Quest chains and branching paths
- [ ] Timed quests with expiration
- [ ] Party/group quest support
- [ ] Custom objective types via API
- [ ] PlaceholderAPI integration
- [ ] Quest journal book item

---

## 📚 Additional Resources

### Example Multi-Stage Quest
```yaml
epic_adventure:
  id: epic_adventure
  title: "&6&lEpic Adventure"
  description:
    - "&7Embark on a legendary journey across"
    - "&7the realm to defeat the ancient evil."
  giverNpcId: "king"
  requirements:
    minLevel: 20
    requiredQuestsCompleted: ["slime_hunter", "gather_resources"]
  objectives:
    - type: TALK
      target: "wizard"
      description: "Consult the wizard"
    - type: COLLECT
      target: DIAMOND
      amount: 5
      description: "Collect magical diamonds"
    - type: VISIT
      world: "world"
      x: -500
      y: 70
      z: 300
      radius: 15.0
      description: "Journey to the Dark Tower"
    - type: KILL
      target: "DarkLord"
      amount: 1
      description: "Defeat the Dark Lord"
    - type: TALK
      target: "king"
      description: "Report to the king"
  rewards:
    xp: 5000
    money: 1000.0
    commands:
      - "give {player} diamond_sword 1"
      - "give {player} elytra 1"
      - "broadcast &6&l{player} has completed the Epic Adventure!"
```

### Performance Tips

1. **Visit Check Frequency**: Increase `visitCheck.periodTicks` for less frequent checks (better performance)
2. **Auto-Save Interval**: Increase `storage.autoSaveTicks` for less frequent saves
3. **Quest Count**: Keep quest count reasonable (< 100 active quests per player)
4. **Objective Complexity**: Avoid excessive chained objectives (recommend < 10 per quest)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/AethorQuests.git`
3. Create a feature branch: `git checkout -b feature/my-feature`
4. Make your changes
5. Build and test: `./mvnw clean package`
6. Commit: `git commit -m "Add my feature"`
7. Push: `git push origin feature/my-feature`
8. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details.

---

## 📄 License

Copyright (c) 2026 Aethor. All Rights Reserved.

This software is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited. See the [LICENSE](LICENSE) file for details.

---

## 🔗 Links

- **GitHub Repository**: [https://github.com/YOUR_ORG/AethorQuests](https://github.com/YOUR_ORG/AethorQuests)
- **Releases**: [https://github.com/YOUR_ORG/AethorQuests/releases](https://github.com/YOUR_ORG/AethorQuests/releases)
- **Issues**: [https://github.com/YOUR_ORG/AethorQuests/issues](https://github.com/YOUR_ORG/AethorQuests/issues)
- **Wiki**: [https://github.com/YOUR_ORG/AethorQuests/wiki](https://github.com/YOUR_ORG/AethorQuests/wiki)

For GitHub Actions setup (Discord webhooks, releases), see [.github/SETUP.md](.github/SETUP.md)

---

## 🙏 Credits

Built with ❤️ for the Aethor server network.

**Dependencies:**
- [AethorNPCS](https://github.com/YOUR_ORG/AethorNPCS) - NPC management system
- [Paper](https://papermc.io/) - High-performance Minecraft server
- [Adventure API](https://github.com/KyoriPowered/adventure) - Text components and messages

---

**Happy Questing! 🎉**
