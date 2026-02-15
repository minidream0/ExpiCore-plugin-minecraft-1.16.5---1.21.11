package ru.expicore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.MessageManager;

/**
 * Обработчик команд модерирования: /invsee, /broadcast, /clear.
 * Инструменты для администраторов и модераторов сервера.
 */
public class ModerationCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /**
     * Создать обработчик команд модерирования.
     * @param plugin экземпляр главного плагина
     */
    public ModerationCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "invsee":
                return handleInvsee(sender, args);
            case "broadcast":
                return handleBroadcast(sender, args);
            case "clear":
                return handleClear(sender, args);
            default:
                return false;
        }
    }

    // ==================== /invsee ====================

    /**
     * Обработать команду /invsee <игрок>.
     * Открывает инвентарь целевого игрока для просмотра и редактирования.
     * Изменения в инвентаре применяются в реальном времени.
     * @param sender отправитель
     * @param args аргументы (имя игрока)
     * @return true
     */
    private boolean handleInvsee(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.invsee")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /invsee <игрок>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            return true;
        }

        // Открываем инвентарь целевого игрока — изменения синхронизируются автоматически
        player.openInventory(target.getInventory());
        player.sendMessage(msg.invseeOpened(target.getName()));
        return true;
    }

    // ==================== /broadcast ====================

    /**
     * Обработать команду /broadcast <сообщение>.
     * Отправляет глобальное объявление всем игрокам на сервере.
     * Поддерживает цветовые коды через символ '&'.
     * @param sender отправитель
     * @param args аргументы (текст объявления)
     * @return true
     */
    private boolean handleBroadcast(CommandSender sender, String[] args) {
        if (!sender.hasPermission("expicore.broadcast")) {
            sender.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /broadcast <сообщение>");
            return true;
        }

        // Собираем сообщение из всех аргументов
        String message = String.join(" ", args);

        // Переводим цветовые коды (&c, &a и т.д.) в настоящие ChatColor
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

        // Формируем итоговое объявление
        String broadcast = MessageManager.BROADCAST_PREFIX + coloredMessage;

        // Отправляем всем игрокам
        Bukkit.broadcastMessage(broadcast);
        return true;
    }

    // ==================== /clear ====================

    /**
     * Обработать команду /clear [игрок].
     * Без аргумента — очищает свой инвентарь.
     * С аргументом — очищает инвентарь другого (требует expicore.clear.others).
     * @param sender отправитель
     * @param args аргументы (необязательное имя игрока)
     * @return true
     */
    private boolean handleClear(CommandSender sender, String[] args) {
        if (args.length > 0) {
            // Очищаем инвентарь другого игрока
            if (!sender.hasPermission("expicore.clear.others")) {
                sender.sendMessage(MessageManager.NO_PERMISSION);
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(MessageManager.PLAYER_NOT_FOUND);
                return true;
            }
            target.getInventory().clear();
            target.sendMessage(MessageManager.CLEAR_BY_OTHER);
            sender.sendMessage(msg.clearOther(target.getName()));
            return true;
        }

        // Очищаем свой инвентарь
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.clear")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }
        player.getInventory().clear();
        player.sendMessage(MessageManager.CLEAR_SELF);
        return true;
    }
}
