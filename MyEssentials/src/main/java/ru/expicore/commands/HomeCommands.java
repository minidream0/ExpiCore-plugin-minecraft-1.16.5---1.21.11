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
 * Обработчик команд домов: /sethome, /home, /delhome.
 * Позволяет игрокам устанавливать, использовать и удалять точки дома.
 * Данные сохраняются в JSON-файле.
 */
public class HomeCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /** Менеджер данных */
    private final DataManager data;

    /**
     * Создать обработчик команд домов.
     * @param plugin экземпляр главного плагина
     */
    public HomeCommands(ExpiCore plugin) {
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

        // Проверка прав
        if (!player.hasPermission("expicore.home")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "sethome":
                return handleSetHome(player, args);
            case "home":
                return handleHome(player, args);
            case "delhome":
                return handleDelHome(player, args);
            default:
                return false;
        }
    }

    /**
     * Обработать команду /sethome [название].
     * Устанавливает точку дома. Если название не указано, используется "home".
     * @param player игрок
     * @param args аргументы (необязательное название)
     * @return true если команда обработана
     */
    private boolean handleSetHome(Player player, String[] args) {
        // Название по умолчанию — "home"
        String name = args.length > 0 ? args[0] : "home";
        String uuid = player.getUniqueId().toString();

        data.setHome(uuid, name, player.getLocation());
        player.sendMessage(msg.homeSet(name));
        return true;
    }

    /**
     * Обработать команду /home [название].
     * Телепортирует игрока к указанному дому.
     * Если название не указано, телепортирует к "home".
     * Если "home" не найден, показывает список домов.
     * @param player игрок
     * @param args аргументы (необязательное название)
     * @return true если команда обработана
     */
    private boolean handleHome(Player player, String[] args) {
        String uuid = player.getUniqueId().toString();
        String name = args.length > 0 ? args[0] : "home";

        Location loc = data.getHome(uuid, name);
        if (loc == null) {
            // Если запрошен "home" без аргументов и не существует — показать список
            Set<String> homeNames = data.getHomeNames(uuid);
            if (homeNames.isEmpty()) {
                player.sendMessage(MessageManager.NO_HOMES);
            } else {
                player.sendMessage(msg.homeNotFound(name));
                player.sendMessage(MessageManager.PREFIX + "§eВаши дома: §f" + String.join(", ", homeNames));
            }
            return true;
        }

        player.teleport(loc);
        player.sendMessage(msg.homeTeleported(name));
        return true;
    }

    /**
     * Обработать команду /delhome [название].
     * Удаляет указанный дом. Если название не указано, удаляет "home".
     * @param player игрок
     * @param args аргументы (необязательное название)
     * @return true если команда обработана
     */
    private boolean handleDelHome(Player player, String[] args) {
        String uuid = player.getUniqueId().toString();
        String name = args.length > 0 ? args[0] : "home";

        if (data.deleteHome(uuid, name)) {
            player.sendMessage(msg.homeDeleted(name));
        } else {
            player.sendMessage(msg.homeNotFound(name));
        }
        return true;
    }
}
