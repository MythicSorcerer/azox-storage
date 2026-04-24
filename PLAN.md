# Azox Storage - Minecraft Plugin Plan

## Project Overview
- **Name**: azox-storage
- **Package**: net.azox.storage
- **Minecraft Version**: 1.21.11 (Paper API)
- **Build Tool**: Maven
- **Libraries**: Lombok, Gson

## Coding Conventions

### Style Rules
- Use **Lombok** for `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- Use fully qualified variable names: `player` not `p`, `inventory` not `inv`
- Always use `this.` prefix for instance fields
- Add `final` where applicable
- Handle null safety explicitly
- Store static plugin instance in main class; access via `AzoxStorage.getInstance()` instead of passing in constructors

## File Structure
```
src/main/java/net/azox/storage/
├── AzoxStorage.java              # Main plugin class
├── command/
│   ├── AdminCommand.java        # /azoxstorage reload
│   ├── BypassLocksCommand.java  # /bypasslocks
│   ├── ChestConfigCommand.java  # /chestconfig
│   └── VoidCommand.java         # /void
├── config/
│   └── PluginConfig.java       # Configuration management
├── container/
│   ├── ContainerData.java      # Container data model
│   └── ContainerManager.java   # Container management
├── data/
│   ├── PresetData.java         # Preset player lists
│   └── VoidData.java           # Despawned item tracking
├── grave/
│   ├── GraveData.java          # Grave data model
│   └── GraveManager.java       # Grave management
├── hook/
│   └── TrialChambersHook.java  # Trial Chambers integration
├── key/
│   ├── KeyData.java           # Key data model
│   └── KeyManager.java        # Key management
├── listener/
│   ├── ContainerListener.java  # Container interactions
│   ├── DropItemListener.java   # Dropped item handling
│   ├── GraveListener.java     # Grave interactions
│   ├── KeyListener.java      # Key interactions
│   └── PlayerListener.java  # Player state management
├── logger/
│   ├── ContainerLogger.java   # Container event logging
│   └── ItemTransferLogger.java # Item transfer logging
└── util/
    ├── ContainerUUID.java    # Container UUID generation
    └── MessageUtil.java      # Message formatting

src/main/resources/
├── config.yml                  # Default configuration
└── plugin.yml                # Plugin manifest
```

## Core Features

### 1. Container Ownership System
- Player places container → becomes their property
- Naming: `PlayerName [Chest|Barrel] <5-char-alphanumeric-uuid>`
- 5 chars = 62^5 ≈ 916M combinations

### 2. Container Interactions
| Action | Result |
|--------|--------|
| Right-click | Open container |
| Sneak + Right-click | Do nothing |
| Left-click + Empty hand | Open container properties |

### 3. Container Properties
- Edit display name (not UUID)
- Lock toggle (default: owner-only)
- Presets: Named player lists
- Keys: Physical/digital key management

### 4. Access Control
```
Player opens →
  Is owner? → YES → Allow
  NO → Has azox.storage.bypass + /bypasslocks? → YES → Allow
  NO → In preset? → YES → Allow
  NO → Has valid key? → YES → Allow
  NO → Deny
```

### 5. Key System
- **Physical Keys**: Inventory items, droppable, right-click to digitize
- **Digital Keys**: Plugin data storage, revocable
- **Key Menu**: View/manage/revoke keys

### 6. Grave System
- Trigger: Death with `keep-inventory: false`
- Structure: Skull on chest
- Contents: Inventory + armor + hotbar
- Quick Loot: Owner sneak-right-click

### 7. Void System
- `/void`: Access lost items

## Permissions
| Permission | Default | Description |
|------------|---------|-------------|
| azox.storage.use | true | Basic usage |
| azox.storage.bypass | op | Bypass locks |
| azox.storage.admin | op | Admin |
| azox.storage.grave.create | true | Create graves |
| azox.storage.key.create | true | Create keys |

## Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/azoxstorage reload` | azox.storage.admin | Reload config |
| `/bypasslocks` | azox.storage.bypass | Toggle bypass |
| `/chestconfig` | azox.storage.use | Chest config |
| `/void` | azox.storage.use | Void storage |