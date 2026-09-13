# MobSpawnControl / Gods

Paper 26.2 plugins for the `inhMOBS` server.

## MobSpawnControl

Disables only natural mob spawning (`CreatureSpawnEvent.SpawnReason.NATURAL`) by:

- individual entity type;
- category: `peaceful`, `hostile`, `neutral`;
- world and chunk.

Commands:

```text
/mobspawn add <mob|peaceful|hostile|neutral>
/mobspawn add <mob|category> <blockX> <blockZ>
/mobspawn add <mob|category> <world> <blockX> <blockZ>
/mobspawn remove <mob|category> [blockX blockZ]
/mobspawn list
/mobspawn clear
/mobspawn reload
```

The command without coordinates uses the executing player's current chunk.
Spawners, spawn eggs, breeding, raids and command-created entities are not blocked.

## Build

Requires Java 25 and the Paper 26.2 server libraries copied into `libraries/`:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@25 gradle clean jar --no-daemon
```

The resulting jar is copied to `plugins/MobSpawnControl.jar` for the local server and to `dist/` for distribution.

## Gods

`Gods` is the personal-reality/phantom system described in `docs/TZ_Gods_Paper_26_2.md`.
The implementation, public API and commands are documented in `docs/Gods.md`; its standalone Gradle project is under `gods/` and the distributable jar is in `dist/Gods-1.0.0.jar`.
