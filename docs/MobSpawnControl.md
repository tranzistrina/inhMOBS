# MobSpawnControl

## Purpose

`MobSpawnControl` cancels only `NATURAL` creature spawns in configured chunks. It does not interfere with spawn eggs, spawners, breeding, raids, commands or custom scenarios.

## Configuration

Rules are persisted in `plugins/MobSpawnControl/config.yml` by world and chunk key (`chunkX,chunkZ`). Coordinates passed to commands are block coordinates and are converted to the containing chunk.

## Categories

- `PEACEFUL`: standard animal/aquatic/ambient spawn categories;
- `HOSTILE`: `MONSTER`, excluding the neutral list;
- `NEUTRAL`: bee, dolphin, enderman, fox, goat, iron golem, llama, panda, polar bear, piglin, wolf and zombified piglin.

## Permissions

- `mobspawn.admin` — all commands; default: operator.

## Runtime

Paper `26.2.build.121-stable`, Java 25.
