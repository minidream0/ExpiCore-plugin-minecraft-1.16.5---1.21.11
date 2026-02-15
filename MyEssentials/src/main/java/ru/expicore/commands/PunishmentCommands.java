package ru.expicore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.MessageManager;
import ru.expicore.managers.PunishmentManager;

/**
 * Обработчик команд системы наказаний:
 * /kick, /mute, /unmute, /ban, /unban, /tempban.
 * Все наказания сохраняются в punishments.json.
 */
public class PunishmentCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер наказаний */
    private final PunishmentManager punishmentManager;

    /**
     * Создать обработчик команд наказаний.
     * @param plugin экземпляр плагина
     */
    public PunishmentCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "kick":
                return handleKick(sender, args);
            case "mute":
                return handleMute(sender, args);
            case "unmute":
                return handleUnmute(sender, args);
            case "ban":
                return handleBan(sender, args);
            case "unban":
                return handleUnban(sender, args);
            case "tempban":
                return handleTempban(sender, args);
            default:
                return false;
        }
    }

    // ==================== /kick ====================

    /**
     * Обработать команду /kick <игрок> [причина].
     * Немедленно отключает игрока от сервера.
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("expicore.kick")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /kick <игрок> [причина]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            return true;
        }

        // Собираем причину из оставшихся аргументов
        String reason = args.length > 1 ? joinArgs(args, 1) : "Нарушение правил сервера";

        // Формируем экран кика с причиной
        String kickScreen = ChatColor.RED + "Вы были кикнуты с сервера!\n\n"
                + ChatColor.YELLOW + "Причина: " + ChatColor.WHITE + reason;

        target.kickPlayer(kickScreen);

        // Уведомляем отправителя
        sender.sendMessage(MessageManager.PREFIX + ChatColor.GREEN + "Игрок "
                + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " кикнут. Причина: "
                + ChatColor.WHITE + reason);

        return true;
    }

    // ==================== /mute ====================

    /**
     * Обработать команду /mute <игрок> [время_в_минутах] [причина].
     * Если время = 0 или не указано — мут перманентный.
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleMute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("expicore.mute")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /mute <игрок> [минуты] [причина]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            return true;
        }

        long expireTime = -1; // Перманентный по умолчанию
        String reason = "Нарушение правил чата";
        int reasonStartIdx = 1;

        // Пробуем разобрать время (второй аргумент)
        if (args.length > 1) {
            try {
                int minutes = Integer.parseInt(args[1]);
                if (minutes > 0) {
                    expireTime = System.currentTimeMillis() + (minutes * 60 * 1000L);
                }
                reasonStartIdx = 2;
            } catch (NumberFormatException e) {
                // Если не число — это уже часть причины
                reasonStartIdx = 1;
            }
        }

        // Собираем причину
        if (args.length > reasonStartIdx) {
            reason = joinArgs(args, reasonStartIdx);
        }

        // Применяем мут
        punishmentManager.mute(target.getUniqueId(), target.getName(), reason, expireTime);

        // Уведомляем цель
        String timeStr = expireTime == -1 ? "навсегда" : PunishmentManager.formatTime(expireTime - System.currentTimeMillis());
        target.sendMessage(MessageManager.PREFIX + ChatColor.RED + "Вы замучены! Причина: "
                + ChatColor.WHITE + reason + ChatColor.RED + ". Длительность: " + ChatColor.WHITE + timeStr);

        // Уведомляем отправителя
        sender.sendMessage(MessageManager.PREFIX + ChatColor.GREEN + "Игрок "
                + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " замучен на "
                + ChatColor.WHITE + timeStr + ChatColor.GREEN + ". Причина: " + ChatColor.WHITE + reason);

        return true;
    }

    // ==================== /unmute ====================

    /**
     * Обработать команду /unmute <игрок>.
     * Снимает мут с игрока.
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("expicore.unmute")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /unmute <игрок>");
            return true;
        }

        // Пробуем найти онлайн-игрока
        Player target = Bukkit.getPlayerExact(args[0]);
        boolean success;

        if (target != null) {
            success = punishmentManager.unmute(target.getUniqueId());
            if (success) {
                target.sendMessage(MessageManager.PREFIX + ChatColor.GREEN + "Ваш мут был снят.");
            }
        } else {
            // Офлайн-игрок — ищем по имени
            success = punishmentManager.unmuteByName(args[0]);
        }

        if (success) {
            sender.sendMessage(MessageManager.PREFIX + ChatColor.GREEN + "Мут с игрока "
                    + ChatColor.YELLOW + args[0] + ChatColor.GREEN + " снят.");
        } else {
            sender.sendMessage(MessageManager.PREFIX + ChatColor.RED + "Игрок "
                    + ChatColor.YELLOW + args[0] + ChatColor.RED + " не замучен.");
        }

        return true;
    }

    // ==================== /ban ====================

    /**
     * Обработать команду /ban <игрок> [причина].
     * Банит игрока перманентно. Если онлайн — кикает.
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleBan(CommandSender sender, String[] args) {
        if (!sender.hasPermission("expicore.ban")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /ban <игрок> [причина]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        String reason = args.length > 1 ? joinArgs(args, 1) : "Нарушение правил сервера";

        if (target != null) {
            // Игрок онлайн — баним и кикаем
            punishmentManager.ban(target.getUniqueId(), target.getName(), reason, -1);

            String banScreen = ChatColor.RED + "Вы забанены на сервере!\n\n"
                    + ChatColor.YELLOW + "Причина: " + ChatColor.WHITE + reason + "\n"
                    + ChatColor.YELLOW + "Срок: " + ChatColor.WHITE + "навсегда";

            target.kickPlayer(banScreen);

            sender.sendMessage(MessageManager.PREFIX + ChatColor.GREEN + "Игрок "
                    + ChatColor.YELLOW + target.getName() + ChatColor.GREEN
                    + " забанен навсегда. Причина: " + ChatColor.WHITE + reason);
        } else {
            // Офлайн-бан — нужен UUID: пробуем из кэша имён
            sender.sendMessage(MessageManager.PREFIX + ChatColor.RED
                    + "Игрок не в сети. Для офлайн-бана необходимо, чтобы игрок заходил ранее.");
        }

        return true;
    }

    // ==================== /unban ====================

    /**
     * Обработать команду /unban <игрок>.
     * Снимает бан с игрока.
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("expicore.unban")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /unban <игрок>");
            return true;
        }

        boolean success = punishmentManager.unbanByName(args[0]);

        if (success) {
            sender.sendMessage(MessageManager.PREFIX + ChatColor.GREEN + "Бан с игрока "
                    + ChatColor.YELLOW + args[0] + ChatColor.GREEN + " снят.");
        } else {
            sender.sendMessage(MessageManager.PREFIX + ChatColor.RED + "Игрок "
                    + ChatColor.YELLOW + args[0] + ChatColor.RED + " не забанен.");
        }

        return true;
    }

    // ==================== /tempban ====================

    /**
     * Обработать команду /tempban <игрок> <время> [причина].
     * Банит игрока на указанное время (формат: 1d, 12h, 30m).
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleTempban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("expicore.tempban")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /tempban <игрок> <время> [причина]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        // Разбираем время
        long durationMs = PunishmentManager.parseTime(args[1]);
        if (durationMs <= 0) {
            sender.sendMessage(MessageManager.PREFIX + ChatColor.RED
                    + "Неверный формат времени! Примеры: 1d, 12h, 30m, 1d12h30m");
            return true;
        }

        long expireTime = System.currentTimeMillis() + durationMs;
        String reason = args.length > 2 ? joinArgs(args, 2) : "Нарушение правил сервера";
        String timeStr = PunishmentManager.formatTime(durationMs);

        if (target != null) {
            // Игрок онлайн — баним и кикаем
            punishmentManager.ban(target.getUniqueId(), target.getName(), reason, expireTime);

            String banScreen = ChatColor.RED + "Вы временно забанены на сервере!\n\n"
                    + ChatColor.YELLOW + "Причина: " + ChatColor.WHITE + reason + "\n"
                    + ChatColor.YELLOW + "Срок: " + ChatColor.WHITE + timeStr;

            target.kickPlayer(banScreen);

            sender.sendMessage(MessageManager.PREFIX + ChatColor.GREEN + "Игрок "
                    + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " забанен на "
                    + ChatColor.WHITE + timeStr + ChatColor.GREEN + ". Причина: " + ChatColor.WHITE + reason);
        } else {
            sender.sendMessage(MessageManager.PREFIX + ChatColor.RED
                    + "Игрок не в сети. Для офлайн-бана необходимо, чтобы игрок заходил ранее.");
        }

        return true;
    }

    // ==================== УТИЛИТЫ ====================

    /**
     * Объединить аргументы в строку начиная с указанного индекса.
     * @param args массив аргументов
     * @param startIndex начальный индекс
     * @return объединённая строка
     */
    private String joinArgs(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
