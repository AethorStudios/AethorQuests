# AethorQuests Quick Reference

## 🎯 For Server Admins

### Installation
1. Install **AethorNPCS** first
2. Copy **AethorQuests.jar** to `plugins/`
3. Restart server
4. Edit `plugins/AethorQuests/quests.yml`

### Creating a Quest
```yaml
my_quest:
  id: my_quest
  title: "Quest Title"
  description: ["Line 1", "Line 2"]
  giverNpcId: "npc_id_from_aethornpcs"  # ← MUST MATCH!
  requirements:
    minLevel: 1
    requiredQuestsCompleted: []
  objectives:
    - type: KILL
      target: ZOMBIE
      amount: 10
      description: "Kill zombies"
  rewards:
    xp: 100
    commands:
      - "give {player} diamond 1"
```

### Admin Commands
```
/questadmin reload              → Reload quests
/questadmin give <player> <id>  → Force give quest
/questadmin complete <player> <id> → Force complete
/questadmin list                → List all quests
```

---

## 🎮 For Players

### Commands
```
/quest                  → Open quest journal
/quest view <id>        → View quest details
/quest accept <id>      → Accept a quest
/quest turnin <id>      → Turn in completed quest
/quest abandon <id>     → Abandon active quest
```

### How to Quest
1. **Find NPC** with quest marker
2. **Right-click** to see quests
3. **Click [Accept]** to start
4. **Complete objectives** (shown in journal)
5. **Return to NPC** when complete
6. **Turn in** for rewards!

---

## 🔧 Objective Types

### KILL
```yaml
- type: KILL
  target: ZOMBIE           # Entity type OR MythicMob name
  amount: 10
  description: "Kill 10 zombies"
```

### TALK
```yaml
- type: TALK
  target: "blacksmith"     # AethorNPCS NPC ID
  description: "Talk to the blacksmith"
```

### COLLECT
```yaml
- type: COLLECT
  target: IRON_INGOT       # Material name
  amount: 16
  description: "Collect 16 iron"
```

### VISIT
```yaml
- type: VISIT
  world: "world"
  x: 100
  y: 64
  z: 200
  radius: 10.0
  description: "Visit the ruins"
```

---

## ⚙️ Config Quick Settings

**config.yml**:
```yaml
debug: false              # Enable debug logs
turnInRequiresGiverNpc: true  # Must return to quest giver
visitCheck:
  periodTicks: 10         # Position check frequency
tracking:
  showActionBar: true     # Show progress on action bar
storage:
  autoSaveTicks: 6000     # Auto-save interval (5 min)
```

---

## 🔗 Integration with AethorNPCS

1. Create NPC in AethorNPCS with ID `"village_elder"`
2. Create quest with `giverNpcId: "village_elder"`
3. NPC now offers that quest when right-clicked!

**Multiple Quests per NPC**: ✅ Supported  
**Quest NPCs can be different from giver**: ✅ Use TALK objectives

---

## 🎁 Reward Placeholders

In command rewards:
- `{player}` → Player name
- `{uuid}` → Player UUID

Example:
```yaml
commands:
  - "give {player} diamond 5"
  - "broadcast &6{player} completed the quest!"
```

---

## 🐛 Troubleshooting

### Quest not showing
- ✓ Check `giverNpcId` matches AethorNPCS NPC ID exactly
- ✓ Use `/questadmin list` to verify quest loaded
- ✓ Check player meets requirements (level, prerequisites)

### NPC not responding
- ✓ Verify AethorNPCS is installed and enabled
- ✓ Check logs for API loading errors
- ✓ Ensure NPC exists in AethorNPCS

### Progress not tracking
- ✓ Check quest is ACTIVE status (`/quest journal`)
- ✓ Verify objective targets (entity types, materials, etc.)
- ✓ Enable debug mode to see tracking events

### Data not saving
- ✓ Check `storage.saveOnQuit: true` in config
- ✓ Verify write permissions on `playerdata/` folder
- ✓ Check logs for save errors

---

## 📊 Permissions

| Permission | Who | What |
|------------|-----|------|
| `aethorquests.use` | Everyone | Use quests |
| `aethorquests.admin` | Admins | Admin commands |

---

## 🚀 Performance Tips

1. **Large Servers**: Increase `visitCheck.periodTicks` to 20+
2. **Many Quests**: Increase `autoSaveTicks` to 12000 (10 min)
3. **Complex Objectives**: Keep objective chains under 10 steps
4. **Frequent Saves**: Use async save tasks (already implemented!)

---

## 📚 Where to Find Things

| File | Location | Purpose |
|------|----------|---------|
| Quests | `plugins/AethorQuests/quests.yml` | Quest definitions |
| Config | `plugins/AethorQuests/config.yml` | Plugin settings |
| Player Data | `plugins/AethorQuests/playerdata/` | Progress saves |
| Plugin Jar | `plugins/AethorQuests.jar` | Plugin binary |

---

## 🎯 Common Quest Patterns

### Simple Kill Quest
```yaml
kill_quest:
  objectives:
    - type: KILL
      target: ZOMBIE
      amount: 10
    - type: TALK
      target: "quest_giver"
```

### Collection Quest
```yaml
collect_quest:
  objectives:
    - type: COLLECT
      target: IRON_INGOT
      amount: 16
    - type: TALK
      target: "blacksmith"
```

### Exploration Quest
```yaml
explore_quest:
  objectives:
    - type: VISIT
      world: "world"
      x: 100
      y: 64
      z: 200
      radius: 10.0
    - type: TALK
      target: "explorer"
```

### Epic Chain Quest
```yaml
epic_quest:
  requirements:
    minLevel: 20
    requiredQuestsCompleted: ["quest1", "quest2"]
  objectives:
    - type: TALK
      target: "king"
    - type: COLLECT
      target: DIAMOND
      amount: 5
    - type: VISIT
      # ...location
    - type: KILL
      target: "BossName"
      amount: 1
    - type: TALK
      target: "king"
```

---

**Quick Start**: Create NPC → Edit quests.yml → `/questadmin reload` → Right-click NPC!

**Help**: Check README.md for full documentation
