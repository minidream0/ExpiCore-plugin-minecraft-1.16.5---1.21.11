package ru.expicore.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.DataManager;
import ru.expicore.managers.MessageManager;

/**
 * Обработчик команд спавна: /spawn, /setspawn.
 * Управляет глобальной точкой спавна сервера.
 * Точка сохраняется в JSON-файле.
 */
public class SpawnCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер данных */
    private final DataManager data;

    /**
     * Создать обработчик команд спавна.
     * @param plugin экземпляр главного плагина
     */
    public SpawnCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.data = plugin.getDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "spawn":
                return handleSpawn(player);
            case "setspawn":
                return handleSetSpawn(player);
            default:
                return false;
        }
    }

    /**
     * Обработать команду /spawn.
     * Телепортирует игрока на глобальную точку спавна.
     * @param player игрок
     * @return true
     */
    private boolean handleSpawn(Player player) {
        if (!player.hasPermission("expicore.spawn")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        Location spawn = data.getSpawn();
        if (spawn == null) {
            player.sendMessage(MessageManager.SPAWN_NOT_SET);
            return true;
        }

        player.teleport(spawn);
        player.sendMessage(MessageManager.SPAWN_TELEPORTED);
        return true;
    }

    /**
     * Обработать команду /setspawn.
     * Устанавливает глобальную точку спавна на текущую позицию.
     * @param player игрок
     * @return true
     */
    private boolean handleSetSpawn(Player player) {
        if (!player.hasPermission("expicore.setspawn")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        data.setSpawn(player.getLocation());
        player.sendMessage(MessageManager.SPAWN_SET);
        return true;
    }
}
