package ru.expicore.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.DataManager;
import ru.expicore.managers.MessageManager;

import java.util.Set;

/**
 * Обработчик команд варпов: /setwarp, /warp, /delwarp.
 * Позволяет создавать глобальные точки телепортации (варпы).
 * Данные сохраняются в JSON-файле.
 */
public class WarpCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /** Менеджер данных */
    private final DataManager data;

    /**
     * Создать обработчик команд варпов.
     * @param plugin экземпляр главного плагина
     */
    public WarpCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
        this.data = plugin.getDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Только игроки могут использовать эти команды
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "setwarp":
                return handleSetWarp(player, args);
            case "warp":
                return handleWarp(player, args);
            case "delwarp":
                return handleDelWarp(player, args);
            default:
                return false;
        }
    }

    /**
     * Обработать команду /setwarp <название>.
     * Создаёт варп с указанным названием в текущей позиции.
     * @param player игрок
     * @param args аргументы (обязательное название)
     * @return true если команда обработана
     */
    private boolean handleSetWarp(Player player, String[] args) {
        // Проверка прав на создание варпов
        if (!player.hasPermission("expicore.warp.set")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /setwarp <название>");
            return true;
        }

        String name = args[0];
        data.setWarp(name, player.getLocation());
        player.sendMessage(msg.warpSet(name));
        return true;
    }

    /**
     * Обработать команду /warp <название>.
     * Телепортирует игрока к указанному варпу.
     * Если название не указано, показывает список варпов.
     * @param player игрок
     * @param args аргументы (название варпа)
     * @return true если команда обработана
     */
    private boolean handleWarp(Player player, String[] args) {
        // Проверка прав на использование варпов
        if (!player.hasPermission("expicore.warp")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        // Если нет аргументов — показать список варпов
        if (args.length < 1) {
            Set<String> warpNames = data.getWarpNames();
            if (warpNames.isEmpty()) {
                player.sendMessage(MessageManager.PREFIX + "§cВарпы не найдены.");
            } else {
                player.sendMessage(MessageManager.PREFIX + "§eДоступные варпы: §f" + String.join(", ", warpNames));
            }
            return true;
        }

        String name = args[0];
        Location loc = data.getWarp(name);

        if (loc == null) {
            player.sendMessage(msg.warpNotFound(name));
            return true;
        }

        player.teleport(loc);
        player.sendMessage(msg.warpTeleported(name));
        return true;
    }

    /**
     * Обработать команду /delwarp <название>.
     * Удаляет варп с указанным названием.
     * @param player игрок
     * @param args аргументы (обязательное название)
     * @return true если команда обработана
     */
    private boolean handleDelWarp(Player player, String[] args) {
        // Проверка прав на удаление варпов
        if (!player.hasPermission("expicore.warp.delete")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /delwarp <название>");
            return true;
        }

        String name = args[0];
        if (data.deleteWarp(name)) {
            player.sendMessage(msg.warpDeleted(name));
        } else {
            player.sendMessage(msg.warpNotFound(name));
        }
        return true;
    }
}
