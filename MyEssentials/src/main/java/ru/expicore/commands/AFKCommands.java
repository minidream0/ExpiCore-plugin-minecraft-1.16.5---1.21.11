package ru.expicore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.AFKManager;
import ru.expicore.managers.MessageManager;

/**
 * Обработчик команды /afk.
 * Переключает статус AFK (Away From Keyboard) вручную.
 */
public class AFKCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер AFK */
    private final AFKManager afkManager;

    /**
     * Создать обработчик команды AFK.
     * @param plugin экземпляр плагина
     */
    public AFKCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.afkManager = plugin.getAFKManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("expicore.afk")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        // Переключаем статус AFK
        afkManager.toggleAFK(player);
        return true;
    }
}
