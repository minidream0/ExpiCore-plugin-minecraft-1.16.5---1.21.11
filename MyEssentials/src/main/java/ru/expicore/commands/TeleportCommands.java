package ru.expicore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.expicore.ExpiCore;
import ru.expicore.managers.MessageManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Обработчик команд телепортации: /tpa, /tpaccept.
 * Реализует систему запросов на телепортацию с таймаутом 60 секунд.
 */
public class TeleportCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /**
     * Хранилище запросов на ТП.
     * Ключ — UUID получателя (target), значение — UUID отправителя (sender).
     */
    private final Map<UUID, UUID> tpaRequests = new ConcurrentHashMap<>();

    /**
     * Создать обработчик команд телепортации.
     * @param plugin экземпляр главного плагина
     */
    public TeleportCommands(ExpiCore plugin) {
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
            case "tpa":
                return handleTpa(player, args);
            case "tpaccept":
                return handleTpAccept(player);
            default:
                return false;
        }
    }

    /**
     * Обработать команду /tpa <игрок>.
     * Отправляет запрос на телепортацию с таймаутом 60 секунд.
     * @param player отправитель
     * @param args аргументы команды
     * @return true если команда обработана
     */
    private boolean handleTpa(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /tpa <игрок>");
            return true;
        }

        // Проверка прав
        if (!player.hasPermission("expicore.tpa")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        // Ищем целевого игрока
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            return true;
        }

        // Нельзя отправить запрос самому себе
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(MessageManager.TPA_SELF);
            return true;
        }

        // Проверяем, нет ли уже входящего запроса у цели
        if (tpaRequests.containsKey(target.getUniqueId())) {
            player.sendMessage(MessageManager.TPA_ALREADY_PENDING);
            return true;
        }

        // Сохраняем запрос
        tpaRequests.put(target.getUniqueId(), player.getUniqueId());

        // Уведомляем обоих игроков
        player.sendMessage(msg.tpaSent(target.getName()));
        target.sendMessage(msg.tpaReceived(player.getName()));

        // Таймаут 60 секунд (1200 тиков)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Убираем запрос если он всё ещё существует
                UUID storedSender = tpaRequests.get(target.getUniqueId());
                if (storedSender != null && storedSender.equals(player.getUniqueId())) {
                    tpaRequests.remove(target.getUniqueId());
                    // Уведомляем об истечении если игроки онлайн
                    if (player.isOnline()) {
                        player.sendMessage(MessageManager.TPA_EXPIRED);
                    }
                    if (target.isOnline()) {
                        target.sendMessage(MessageManager.TPA_EXPIRED);
                    }
                }
            }
        }.runTaskLater(plugin, 1200L);

        return true;
    }

    /**
     * Обработать команду /tpaccept.
     * Принимает входящий запрос на телепортацию.
     * @param player получатель запроса (target)
     * @return true если команда обработана
     */
    private boolean handleTpAccept(Player player) {
        if (!player.hasPermission("expicore.tpa")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        // Проверяем наличие входящего запроса
        UUID senderUUID = tpaRequests.remove(player.getUniqueId());
        if (senderUUID == null) {
            player.sendMessage(MessageManager.TPA_NO_PENDING);
            return true;
        }

        // Ищем отправителя запроса
        Player requester = Bukkit.getPlayer(senderUUID);
        if (requester == null || !requester.isOnline()) {
            player.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            return true;
        }

        // Телепортируем отправителя к получателю
        requester.teleport(player.getLocation());
        requester.sendMessage(MessageManager.TPA_ACCEPTED_SENDER);
        player.sendMessage(MessageManager.TPA_ACCEPTED_TARGET);

        return true;
    }
}
