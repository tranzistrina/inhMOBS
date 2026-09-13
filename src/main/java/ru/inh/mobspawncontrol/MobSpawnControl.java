package ru.inh.mobspawncontrol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MobSpawnControl extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private static final String PREFIX = ChatColor.DARK_AQUA + "[MobSpawn] " + ChatColor.RESET;
    private static final Set<String> CATEGORIES = Set.of("PEACEFUL", "HOSTILE", "NEUTRAL");
    private static final Set<EntityType> NEUTRAL_TYPES = EnumSet.of(
            EntityType.BEE, EntityType.DOLPHIN, EntityType.ENDERMAN,
            EntityType.FOX, EntityType.GOAT, EntityType.IRON_GOLEM,
            EntityType.LLAMA, EntityType.PANDA, EntityType.POLAR_BEAR,
            EntityType.PIGLIN, EntityType.WOLF, EntityType.ZOMBIFIED_PIGLIN
    );

    // world name -> chunk key ("chunkX,chunkZ") -> blocked targets
    private final Map<String, Map<String, Set<String>>> rules = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadRules();
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("mobspawn") != null) {
            getCommand("mobspawn").setExecutor(this);
            getCommand("mobspawn").setTabCompleter(this);
        }
        getLogger().info("MobSpawnControl включён. Загружено правил: " + countRules());
    }

    @Override
    public void onDisable() {
        saveRules();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        EntityType type = event.getEntityType();
        String world = event.getLocation().getWorld().getName();
        String chunk = chunkKey(event.getLocation().getChunk().getX(), event.getLocation().getChunk().getZ());
        Set<String> blocked = rules.getOrDefault(world, Collections.emptyMap()).get(chunk);
        if (blocked == null || blocked.isEmpty()) {
            return;
        }

        if (blocked.contains(type.name()) || blocked.stream().anyMatch(category -> matchesCategory(type, category))) {
            event.setCancelled(true);
        }
    }

    private boolean matchesCategory(EntityType type, String category) {
        return switch (category) {
            case "HOSTILE" -> type.getSpawnCategory() == SpawnCategory.MONSTER && !NEUTRAL_TYPES.contains(type);
            case "NEUTRAL" -> NEUTRAL_TYPES.contains(type);
            case "PEACEFUL" -> type.getSpawnCategory() != SpawnCategory.MONSTER
                    && type.getSpawnCategory() != SpawnCategory.MISC
                    && !NEUTRAL_TYPES.contains(type);
            default -> false;
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mobspawn.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Недостаточно прав.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add", "remove" -> changeRule(sender, args, args[0].equalsIgnoreCase("add"));
            case "list" -> listRules(sender);
            case "clear" -> clearRules(sender);
            case "reload" -> reloadRules(sender);
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void changeRule(CommandSender sender, String[] args, boolean add) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.YELLOW + "Укажи моба или категорию: PEACEFUL, HOSTILE, NEUTRAL.");
            return;
        }
        String target = normalizeTarget(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Неизвестный моб/категория: " + args[1]);
            return;
        }

        LocationTarget location = parseLocation(sender, args);
        if (location == null) {
            return;
        }

        Map<String, Set<String>> worldRules = rules.computeIfAbsent(location.world(), ignored -> new LinkedHashMap<>());
        Set<String> blocked = worldRules.computeIfAbsent(location.chunkKey(), ignored -> new LinkedHashSet<>());
        boolean changed = add ? blocked.add(target) : blocked.remove(target);
        if (blocked.isEmpty()) {
            worldRules.remove(location.chunkKey());
        }
        if (worldRules.isEmpty()) {
            rules.remove(location.world());
        }
        saveRules();

        String action = add ? "запрещён" : "разрешён";
        sender.sendMessage(PREFIX + (changed ? ChatColor.GREEN + target + " " + action
                : ChatColor.GRAY + "Такого правила не было/оно уже снято")
                + ChatColor.GRAY + " в мире " + location.world() + " в чанке " + location.chunkKey()
                + " (начало чанка: " + location.chunkOrigin() + ").");
    }

    private LocationTarget parseLocation(CommandSender sender, String[] args) {
        World world;
        int blockX;
        int blockZ;
        try {
            if (args.length == 2 && sender instanceof Player player) {
                world = player.getWorld();
                blockX = player.getLocation().getBlockX();
                blockZ = player.getLocation().getBlockZ();
            } else if (args.length == 4 && sender instanceof Player player) {
                world = player.getWorld();
                blockX = Integer.parseInt(args[2]);
                blockZ = Integer.parseInt(args[3]);
            } else if (args.length == 5) {
                world = Bukkit.getWorld(args[2]);
                blockX = Integer.parseInt(args[3]);
                blockZ = Integer.parseInt(args[4]);
                if (world == null) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Мир не найден: " + args[2]);
                    return null;
                }
            } else {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "Из игры: /mobspawn add zombie (текущий чанк) или /mobspawn add zombie 100 200");
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "Из консоли: /mobspawn add zombie world 100 200");
                return null;
            }
        } catch (NumberFormatException exception) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Координаты должны быть целыми числами.");
            return null;
        }
        return new LocationTarget(world.getName(), blockX >> 4, blockZ >> 4);
    }

    private String normalizeTarget(String input) {
        String normalized = input.toUpperCase(Locale.ROOT).replace("MINECRAFT:", "");
        if (normalized.equals("PASSIVE") || normalized.equals("ANIMAL")) {
            normalized = "PEACEFUL";
        }
        if (normalized.equals("AGGRESSIVE") || normalized.equals("MONSTER")) {
            normalized = "HOSTILE";
        }
        if (CATEGORIES.contains(normalized)) {
            return normalized;
        }
        try {
            EntityType type = EntityType.valueOf(normalized);
            return type.isAlive() ? type.name() : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void listRules(CommandSender sender) {
        if (countRules() == 0) {
            sender.sendMessage(PREFIX + ChatColor.GRAY + "Запретов нет.");
            return;
        }
        sender.sendMessage(PREFIX + ChatColor.AQUA + "Активные запреты естественного спавна:");
        rules.forEach((world, chunks) -> chunks.forEach((chunk, targets) ->
                sender.sendMessage(ChatColor.GRAY + "- " + world + " / чанк " + chunk + ": "
                        + ChatColor.WHITE + String.join(", ", targets))));
    }

    private void clearRules(CommandSender sender) {
        rules.clear();
        saveRules();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Все запреты очищены.");
    }

    private void reloadRules(CommandSender sender) {
        reloadConfig();
        loadRules();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Конфигурация перезагружена. Правил: " + countRules());
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(PREFIX + ChatColor.AQUA + "Команды:");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " add <моб|peaceful|hostile|neutral> [blockX blockZ]");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " remove <моб|категория> [blockX blockZ]");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " list | clear | reload");
        sender.sendMessage(ChatColor.DARK_GRAY + "Из игры без координат используется текущий чанк. Блокируется только NATURAL.");
    }

    private void loadRules() {
        rules.clear();
        if (getConfig().getConfigurationSection("rules") == null) {
            return;
        }
        for (String world : getConfig().getConfigurationSection("rules").getKeys(false)) {
            Map<String, Set<String>> worldRules = new LinkedHashMap<>();
            for (String chunk : getConfig().getConfigurationSection("rules." + world).getKeys(false)) {
                List<String> values = getConfig().getStringList("rules." + world + "." + chunk);
                if (!values.isEmpty()) {
                    worldRules.put(chunk, new LinkedHashSet<>(values));
                }
            }
            if (!worldRules.isEmpty()) {
                rules.put(world, worldRules);
            }
        }
    }

    private void saveRules() {
        getConfig().set("rules", null);
        rules.forEach((world, chunks) -> chunks.forEach((chunk, targets) ->
                getConfig().set("rules." + world + "." + chunk, new ArrayList<>(targets))));
        saveConfig();
    }

    private int countRules() {
        return rules.values().stream().mapToInt(chunks -> chunks.values().stream().mapToInt(Set::size).sum()).sum();
    }

    private String chunkKey(int x, int z) {
        return x + "," + z;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return partial(args[0], List.of("add", "remove", "list", "clear", "reload"));
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            List<String> values = new ArrayList<>(CATEGORIES);
            values.addAll(Arrays.stream(EntityType.values()).filter(EntityType::isAlive).map(EntityType::name).toList());
            return partial(args[1], values);
        }
        return Collections.emptyList();
    }

    private List<String> partial(String input, List<String> values) {
        String lower = input.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower)).sorted().toList();
    }

    private record LocationTarget(String world, int chunkX, int chunkZ) {
        String chunkKey() {
            return chunkX + "," + chunkZ;
        }

        String chunkOrigin() {
            return "(" + (chunkX * 16) + ", " + (chunkZ * 16) + ")";
        }
    }
}
