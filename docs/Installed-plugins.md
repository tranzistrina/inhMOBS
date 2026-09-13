# Установленные Bukkit-плагины Paper 26.2

Проверено на Paper `26.2-121` (`API 26.2.build.121-stable`) 13 сентября 2026.

Рабочие загрузки:

- ChunkLoader 1.0.7
- DisablePortals 1.1.0
- Geyser-Spigot 2.11.2-b1235
- Gods 1.0.0
- GSit 3.5.1
- InvSeePlusPlus 0.31.15
- MobSpawnControl 1.0.0
- NBTAPI 2.16.0
- PacketEvents 2.13.0
- SkinsRestorer 15.12.5
- WolfyUtilities 4.17-beta.7
- WorldEdit 7.4.5
- inhGraphics 1.0.0

Все эти плагины подтвердили `Enabling` в `logs/latest.log`; NBTAPI отдельно подтвердил NMS support `MC26_2`.

## Плагины из исходного списка, которые нельзя честно считать установленными

- `AxionPaper`, `FOG`, `FFAntiFreeCam`, `PatPatPlugin`, `ProximityChat`, `SimpleLogin` и `Skinsfiestorer` — точные публичные артефакты с такими именами не найдены. `Skinsfiestorer` трактован как опечатка `SkinsRestorer`.
- `CustomCrafting` найден, но доступная официальная сборка требует `scafall` и собрана под 1.21.8. После установки зависимостей она отключилась на Paper 26.2 с `ClassNotFoundException: net.minecraft.commands.arguments.ResourceLocationArgument`. Не оставлен в активном `plugins/`, чтобы сервер не стартовал с ошибками.
- `LuckFerms NBTAFI` трактован как `LuckPerms + NBTAPI`; NBTAPI установлен. Точное имя `LuckFerms` не подтверждено.

Нерабочие/неподтверждённые jars сохранены отдельно только как временные загрузки, но не включаются сервером.
