# HyCitizens

A Hytale server plugin for in-game NPC (Citizen) management. Players can create, customize, and interact with NPCs through a UI or developer API.

- **Main class:** `com.electro.hycitizens.HyCitizensPlugin`
- **Version:** 1.6.1
- **Java:** 25
- **Hytale Server version:** 2026.02.19-1a311a592

## Build

```bash
./gradlew build        # Full build (creates fat JAR)
./gradlew fatJar       # Fat JAR only
./gradlew clean build  # Clean rebuild
```

Output: `build/libs/HyCitizens-1.6.1.jar`

## Test

```bash
./gradlew test         # JUnit 5 (no tests written yet)
```

## Dependencies

- **Hytale Server API** — `latest.release` from `maven.hytale.com` (compileOnly)
- **HyUI 0.8.9** — local JAR in `libs/` (UI framework for in-game pages)
- **JUnit Jupiter 5.10.0** — testing

## Architecture

### Plugin lifecycle
`HyCitizensPlugin` extends `JavaPlugin`. Lifecycle: `setup()` → `start()` → `shutdown()`. Singleton via `HyCitizensPlugin.get()`.

### Manager pattern
Core logic lives in manager classes:
- **CitizensManager** — NPC spawn/despawn, ECS components, nametags, movement, chunk tracking
- **PatrolManager** — waypoint paths, session tracking, stuck detection
- **ScheduleManager** — time-based behaviors synced to Hytale world time

### Data persistence
JSON via Gson. `ConfigManager` handles load/save to `mods/HyCitizensData/data.json`. Generated roles go to `mods/HyCitizensRoles/Server/NPC/Roles/`.

### Entity Component System (ECS)
Uses Hytale's ECS. Custom component: `CitizenNametagComponent`. Components registered via `getEntityStoreRegistry().registerComponent()`.

### Event system
Custom events: `CitizenAddedEvent`, `CitizenRemovedEvent`, `CitizenDeathEvent`, `CitizenInteractEvent`. Dedicated listener classes per event.

### UI
Built with HyUI framework — `PageBuilder` pattern, template processing, `CustomPageLifetime` lifecycle. UI pages defined in `src/main/resources/Common/UI/Custom/Pages/`.

### Role generation
`RoleGenerator` creates role JSON files from templates. Pre-generated variants in `src/main/resources/Server/NPC/Roles/` for different behavior/state combos (idle, wander, patrol, combat, schedule).

### Interaction system
`PlayerInteractionHandler` with 500ms cooldown. Packet watching via `PacketWatcher`. Supports left-click and right-click (F-key) interactions. `CitizenInteraction` chain system.

## Code conventions

- **Indentation:** 4 spaces
- **Naming:** PascalCase classes, camelCase methods/fields, UPPER_SNAKE constants
- **Nullability:** `@Nonnull` / `@Nullable` annotations
- **Thread safety:** `ConcurrentHashMap` for shared collections, `ScheduledExecutor` for async tasks, `CompletableFuture` for async operations
- **File I/O:** try-with-resources

## Key packages

| Package | Purpose |
|---|---|
| `actions` | Citizen interaction action handlers |
| `commands` | `/citizens` command handler |
| `components` | Hytale ECS components |
| `events` | Custom events and their listeners |
| `interactions` | Player-NPC interaction handling |
| `listeners` | World/entity/packet listeners |
| `managers` | Core business logic (Citizens, Patrol, Schedule) |
| `models` | Data models (`CitizenData`, configs, behaviors) |
| `roles` | NPC role file generation |
| `ui` | In-game UI (CitizensUI, SkinCustomizerUI) |
| `util` | ConfigManager, SkinUtilities, UpdateChecker |

## Resources

- `src/main/resources/manifest.json` — plugin metadata
- `src/main/resources/Common/UI/Custom/Pages/` — HyUI page definitions
- `src/main/resources/Server/NPC/Roles/` — pre-generated role JSONs
- `src/main/resources/Server/Languages/en-US/` — localization
