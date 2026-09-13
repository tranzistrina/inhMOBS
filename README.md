# inhMOBS

Paper 26.2 plugin for controlling natural mob spawning by world and chunk.

## Features

- blocks only `CreatureSpawnEvent.SpawnReason.NATURAL`;
- individual entity types;
- categories `peaceful`, `hostile`, `neutral`;
- rules per world and chunk;
- commands usable in-game and from console;
- rules persisted in the plugin YAML configuration.

Spawners, spawn eggs, breeding, raids and command-created entities are not blocked.

## Commands

```text
/mobspawn add <mob|peaceful|hostile|neutral>
/mobspawn add <mob|category> <blockX> <blockZ>
/mobspawn add <mob|category> <world> <blockX> <blockZ>
/mobspawn remove <mob|category> [blockX blockZ]
/mobspawn list
/mobspawn clear
/mobspawn reload
```

Without coordinates, the current chunk of the executing player is used.

## Build

Requires Java 25 and the Paper 26.2 server libraries in `libraries/`:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@25 gradle clean jar --no-daemon
```

Output: `build/libs/inhMOBS-1.0.0.jar`.
The server copy is installed as `plugins/inhMOBS.jar` and the distributable build is in `dist/`.
