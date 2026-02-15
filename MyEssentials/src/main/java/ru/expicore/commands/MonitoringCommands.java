package ru.expicore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.MessageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик команд мониторинга и утилит:
 * /gc — статистика сервера (TPS, RAM, чанки).
 * /near — список ближайших игроков в радиусе.
 */
public class MonitoringCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /**
     * Создать обработчик команд мониторинга.
     * @param plugin экземпляр плагина
     */
    public MonitoringCommands(ExpiCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "gc":
                return handleGC(sender);
            case "near":
                return handleNear(sender, args);
            default:
                return false;
        }
    }

    // ==================== /gc ====================

    /**
     * Обработать команду /gc (Garbage Collector / Lag).
     * Выводит TPS, свободную/выделенную RAM и количество чанков по мирам.
     * TPS берётся через рефлексию из MinecraftServer (совместимо с Spigot 1.16+).
     * @param sender отправитель команды
     * @return true
     */
    private boolean handleGC(CommandSender sender) {
        if (!sender.hasPermission("expicore.gc")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        // Информация о памяти
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        // Получаем TPS через Bukkit (Spigot API)
        double[] tps = getTPS();

        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Статистика сервера"
                + ChatColor.GOLD + " ===");

        // TPS
        if (tps != null) {
            sender.sendMessage(ChatColor.YELLOW + "TPS: "
                    + formatTPS(tps[0]) + ChatColor.GRAY + " (1м), "
                    + formatTPS(tps[1]) + ChatColor.GRAY + " (5м), "
                    + formatTPS(tps[2]) + ChatColor.GRAY + " (15м)");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "TPS: " + ChatColor.RED + "Недоступно");
        }

        // Память
        sender.sendMessage(ChatColor.YELLOW + "Память: "
                + ChatColor.WHITE + usedMemory + "МБ" + ChatColor.GRAY + " / "
                + ChatColor.WHITE + totalMemory + "МБ"
                + ChatColor.GRAY + " (макс: " + ChatColor.WHITE + maxMemory + "МБ" + ChatColor.GRAY + ")");

        sender.sendMessage(ChatColor.YELLOW + "Свободно: "
                + ChatColor.GREEN + freeMemory + "МБ");

        // Чанки по мирам
        sender.sendMessage(ChatColor.YELLOW + "Миры:");
        for (World world : Bukkit.getWorlds()) {
            int chunks = world.getLoadedChunks().length;
            int entities = world.getEntities().size();
            sender.sendMessage(ChatColor.GRAY + "  " + ChatColor.WHITE + world.getName()
                    + ChatColor.GRAY + ": " + ChatColor.GREEN + chunks + " чанков"
                    + ChatColor.GRAY + ", " + ChatColor.GREEN + entities + " сущностей");
        }

        // Онлайн
        sender.sendMessage(ChatColor.YELLOW + "Онлайн: "
                + ChatColor.WHITE + Bukkit.getOnlinePlayers().size()
                + ChatColor.GRAY + " / " + ChatColor.WHITE + Bukkit.getMaxPlayers());

        return true;
    }

    /**
     * Получить TPS сервера через рефлексию (Spigot).
     * Возвращает массив из 3 значений: 1 мин, 5 мин, 15 мин.
     * @return массив TPS или null при ошибке
     */
    private double[] getTPS() {
        try {
            // Spigot предоставляет метод Bukkit.getServer().getTPS() начиная с 1.16
            Object server = Bukkit.getServer();
            java.lang.reflect.Method method = server.getClass().getMethod("getTPS");
            return (double[]) method.invoke(server);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Отформатировать значение TPS с цветовой индикацией.
     * >= 18 — зелёный, >= 15 — жёлтый, < 15 — красный.
     * @param tps значение TPS
     * @return цветная строка
     */
    private String formatTPS(double tps) {
        // Ограничиваем до 20.0
        tps = Math.min(tps, 20.0);
        ChatColor color;
        if (tps >= 18.0) {
            color = ChatColor.GREEN;
        } else if (tps >= 15.0) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }
        return color + String.format("%.1f", tps);
    }

    // ==================== /near ====================

    /**
     * Обработать команду /near [радиус].
     * Показывает список игроков поблизости (по умолчанию 100 блоков).
     * @param sender отправитель команды
     * @param args аргументы (необязательный радиус)
     * @return true
     */
    private boolean handleNear(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("expicore.near")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        // Определяем радиус (по умолчанию 100)
        int radius = 100;
        if (args.length > 0) {
            try {
                radius = Integer.parseInt(args[0]);
                if (radius <= 0 || radius > 10000) {
                    player.sendMessage(MessageManager.PREFIX + ChatColor.RED + "Радиус должен быть от 1 до 10000.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(MessageManager.PREFIX + ChatColor.RED + "Неверный формат радиуса!");
                return true;
            }
        }

        // Ищем ближайших игроков
        List<String> nearbyPlayers = new ArrayList<>();
        double radiusSquared = (double) radius * radius;

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (!other.getWorld().equals(player.getWorld())) continue;

            double distSquared = player.getLocation().distanceSquared(other.getLocation());
            if (distSquared <= radiusSquared) {
                int dist = (int) Math.sqrt(distSquared);
                nearbyPlayers.add(ChatColor.YELLOW + other.getName() + ChatColor.GRAY + " (" + dist + "м)");
            }
        }

        if (nearbyPlayers.isEmpty()) {
            player.sendMessage(MessageManager.PREFIX + ChatColor.YELLOW
                    + "Нет игроков поблизости (радиус: " + radius + " блоков).");
        } else {
            player.sendMessage(MessageManager.PREFIX + ChatColor.GREEN
                    + "Игроки поблизости (" + radius + " блоков): ");
            player.sendMessage(ChatColor.GRAY + String.join(ChatColor.GRAY + ", ", nearbyPlayers));
        }

        return true;
    }
}
