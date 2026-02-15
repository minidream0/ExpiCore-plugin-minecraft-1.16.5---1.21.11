package ru.expicore.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.MessageManager;

import java.util.*;

/**
 * Обработчик административных команд: /gamemode, /gmc, /gms, /gmsp, /god, /fly.
 * Управление игровым режимом, неуязвимостью и полётом.
 */
public class AdminCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /**
     * Множество UUID игроков с включённым режимом бога.
     * Используется слушателем GodListener для отмены урона.
     */
    private final Set<UUID> godPlayers = new HashSet<>();

    /**
     * Создать обработчик административных команд.
     * @param plugin экземпляр главного плагина
     */
    public AdminCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
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
            case "gamemode":
                return handleGamemode(player, args);
            case "gmc":
                return handleGamemodeShortcut(player, GameMode.CREATIVE);
            case "gms":
                return handleGamemodeShortcut(player, GameMode.SURVIVAL);
            case "gmsp":
                return handleGamemodeShortcut(player, GameMode.SPECTATOR);
            case "god":
                return handleGod(player);
            case "fly":
                return handleFly(player);
            default:
                return false;
        }
    }

    /**
     * Обработать команду /gamemode <режим>.
     * Изменяет игровой режим игрока.
     * @param player игрок
     * @param args аргументы (название режима)
     * @return true если команда обработана
     */
    private boolean handleGamemode(Player player, String[] args) {
        if (!player.hasPermission("expicore.gamemode")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /gamemode <survival|creative|adventure|spectator>");
            return true;
        }

        GameMode mode = parseGameMode(args[0]);
        if (mode == null) {
            player.sendMessage(MessageManager.GAMEMODE_INVALID);
            return true;
        }

        player.setGameMode(mode);
        player.sendMessage(msg.gamemodeChanged(getGameModeName(mode)));
        return true;
    }

    /**
     * Обработать сокращённую команду смены режима (/gmc, /gms, /gmsp).
     * @param player игрок
     * @param mode целевой игровой режим
     * @return true если команда обработана
     */
    private boolean handleGamemodeShortcut(Player player, GameMode mode) {
        if (!player.hasPermission("expicore.gamemode")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        player.setGameMode(mode);
        player.sendMessage(msg.gamemodeChanged(getGameModeName(mode)));
        return true;
    }

    /**
     * Обработать команду /god.
     * Переключает режим неуязвимости (бога).
     * @param player игрок
     * @return true если команда обработана
     */
    private boolean handleGod(Player player) {
        if (!player.hasPermission("expicore.god")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (godPlayers.contains(uuid)) {
            // Выключаем режим бога
            godPlayers.remove(uuid);
            player.setInvulnerable(false);
            player.sendMessage(MessageManager.GOD_DISABLED);
        } else {
            // Включаем режим бога
            godPlayers.add(uuid);
            player.setInvulnerable(true);
            player.sendMessage(MessageManager.GOD_ENABLED);
        }
        return true;
    }

    /**
     * Обработать команду /fly.
     * Переключает режим полёта.
     * @param player игрок
     * @return true если команда обработана
     */
    private boolean handleFly(Player player) {
        if (!player.hasPermission("expicore.fly")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (player.getAllowFlight()) {
            // Выключаем полёт
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(MessageManager.FLY_DISABLED);
        } else {
            // Включаем полёт
            player.setAllowFlight(true);
            player.sendMessage(MessageManager.FLY_ENABLED);
        }
        return true;
    }

    /**
     * Проверить, включён ли режим бога у игрока.
     * @param uuid UUID игрока
     * @return true если режим бога включён
     */
    public boolean isGod(UUID uuid) {
        return godPlayers.contains(uuid);
    }

    /**
     * Убрать режим бога при выходе (очистка).
     * @param uuid UUID игрока
     */
    public void removeGod(UUID uuid) {
        godPlayers.remove(uuid);
    }

    /**
     * Преобразовать текстовый аргумент в GameMode.
     * Поддерживает как полные названия так и числа.
     * @param input текстовый ввод
     * @return GameMode или null если не распознан
     */
    private GameMode parseGameMode(String input) {
        switch (input.toLowerCase()) {
            case "survival":
            case "s":
            case "0":
                return GameMode.SURVIVAL;
            case "creative":
            case "c":
            case "1":
                return GameMode.CREATIVE;
            case "adventure":
            case "a":
            case "2":
                return GameMode.ADVENTURE;
            case "spectator":
            case "sp":
            case "3":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }

    /**
     * Получить русское название игрового режима.
     * @param mode игровой режим
     * @return русское название
     */
    private String getGameModeName(GameMode mode) {
        switch (mode) {
            case SURVIVAL:
                return "Выживание";
            case CREATIVE:
                return "Творческий";
            case ADVENTURE:
                return "Приключение";
            case SPECTATOR:
                return "Наблюдатель";
            default:
                return mode.name();
        }
    }
}
