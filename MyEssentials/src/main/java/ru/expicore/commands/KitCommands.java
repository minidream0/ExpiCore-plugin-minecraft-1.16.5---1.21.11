package ru.expicore.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.KitManager;
import ru.expicore.managers.MessageManager;

import java.util.Set;

/**
 * Обработчик команд китов: /kit <название|create|delete|editor>.
 * Управляет созданием, выдачей и удалением китов (наборов предметов).
 */
public class KitCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /** Менеджер китов */
    private final KitManager kitManager;

    /**
     * Создать обработчик команд китов.
     * @param plugin экземпляр главного плагина
     */
    public KitCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
        this.kitManager = plugin.getKitManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Только игроки могут использовать эти команды
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }

        Player player = (Player) sender;

        // Базовая проверка прав на использование китов
        if (!player.hasPermission("expicore.kit")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        // Без аргументов — показать список доступных китов
        if (args.length == 0) {
            return handleListKits(player);
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreateKit(player, args);
            case "delete":
                return handleDeleteKit(player, args);
            case "editor":
                return handleEditor(player);
            default:
                // Если не подкоманда — считаем это названием кита
                return handleGiveKit(player, args[0]);
        }
    }

    /**
     * Показать список доступных китов.
     * @param player игрок
     * @return true
     */
    private boolean handleListKits(Player player) {
        Set<String> kitNames = plugin.getDataManager().getKitNames();
        if (kitNames.isEmpty()) {
            player.sendMessage(MessageManager.KIT_NO_KITS);
        } else {
            player.sendMessage(MessageManager.PREFIX + ChatColor.YELLOW + "Доступные киты: "
                    + ChatColor.WHITE + String.join(", ", kitNames));
        }
        return true;
    }

    /**
     * Выдать кит игроку.
     * @param player игрок
     * @param name название кита
     * @return true
     */
    private boolean handleGiveKit(Player player, String name) {
        if (kitManager.giveKit(player, name)) {
            player.sendMessage(msg.kitGiven(name));
        } else {
            player.sendMessage(msg.kitNotFound(name));
        }
        return true;
    }

    /**
     * Создать кит из текущего инвентаря.
     * Требует право expicore.kit.admin.
     * @param player игрок
     * @param args аргументы (args[1] — название кита)
     * @return true
     */
    private boolean handleCreateKit(Player player, String[] args) {
        // Проверка административных прав
        if (!player.hasPermission("expicore.kit.admin")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /kit create <название>");
            return true;
        }

        String name = args[1];

        if (kitManager.createKitFromInventory(player, name)) {
            player.sendMessage(msg.kitCreated(name));
        } else {
            player.sendMessage(MessageManager.KIT_EMPTY_INVENTORY);
        }
        return true;
    }

    /**
     * Удалить кит.
     * Требует право expicore.kit.admin.
     * @param player игрок
     * @param args аргументы (args[1] — название кита)
     * @return true
     */
    private boolean handleDeleteKit(Player player, String[] args) {
        // Проверка административных прав
        if (!player.hasPermission("expicore.kit.admin")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /kit delete <название>");
            return true;
        }

        String name = args[1];

        if (plugin.getDataManager().deleteKit(name)) {
            player.sendMessage(msg.kitDeleted(name));
        } else {
            player.sendMessage(msg.kitNotFound(name));
        }
        return true;
    }

    /**
     * Открыть GUI-редактор китов.
     * Требует право expicore.kit.admin.
     * @param player игрок
     * @return true
     */
    private boolean handleEditor(Player player) {
        if (!player.hasPermission("expicore.kit.admin")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        kitManager.openKitEditor(player);
        return true;
    }
}
