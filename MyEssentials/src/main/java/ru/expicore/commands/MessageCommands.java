package ru.expicore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.expicore.ExpiCore;
import ru.expicore.managers.IgnoreManager;
import ru.expicore.managers.MessageManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Обработчик команд личных сообщений и социальных функций:
 * /msg, /r, /ignore, /socialspy.
 * Позволяет игрокам общаться приватно, игнорировать других
 * и даёт администраторам возможность читать чужие ЛС.
 */
public class MessageCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /** Менеджер игнорирования */
    private final IgnoreManager ignoreManager;

    /**
     * Хранилище последних собеседников для /r.
     * Ключ — UUID получателя, значение — UUID последнего отправителя.
     */
    private final Map<UUID, UUID> lastMessenger = new ConcurrentHashMap<>();

    /**
     * Множество UUID игроков с включённым SocialSpy.
     * Эти игроки видят чужие личные сообщения.
     */
    private final Set<UUID> socialSpies = ConcurrentHashMap.newKeySet();

    /**
     * Создать обработчик команд личных сообщений.
     * @param plugin экземпляр главного плагина
     */
    public MessageCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
        this.ignoreManager = plugin.getIgnoreManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "msg":
                return handleMsg(sender, args);
            case "r":
                return handleReply(sender, args);
            case "ignore":
                return handleIgnore(sender, args);
            case "socialspy":
                return handleSocialSpy(sender);
            default:
                return false;
        }
    }

    // ==================== /msg ====================

    /**
     * Обработать команду /msg <игрок> <текст>.
     * Отправляет приватное сообщение указанному игроку.
     * Проверяет список игнорирования и рассылает SocialSpy.
     * @param sender отправитель
     * @param args аргументы (имя игрока + текст)
     * @return true
     */
    private boolean handleMsg(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("expicore.msg")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /msg <игрок> <текст>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            return true;
        }

        // Нельзя написать самому себе
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(MessageManager.MSG_SELF);
            return true;
        }

        // Проверка: получатель игнорирует отправителя
        if (ignoreManager.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(MessageManager.IGNORE_BLOCKED);
            return true;
        }

        // Собираем текст из оставшихся аргументов
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();

        // Отправляем сообщения обоим игрокам
        player.sendMessage(msg.msgTo(target.getName(), message));
        target.sendMessage(msg.msgFrom(player.getName(), message));

        // Сохраняем отправителя как последнего собеседника для получателя
        lastMessenger.put(target.getUniqueId(), player.getUniqueId());
        // И наоборот — для удобства ответа отправителю тоже
        lastMessenger.put(player.getUniqueId(), target.getUniqueId());

        // Рассылка SocialSpy
        broadcastSocialSpy(player, target, message);

        return true;
    }

    // ==================== /r ====================

    /**
     * Обработать команду /r <текст>.
     * Отвечает на последнее полученное личное сообщение.
     * @param sender отправитель
     * @param args аргументы (текст ответа)
     * @return true
     */
    private boolean handleReply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("expicore.msg")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /r <текст>");
            return true;
        }

        // Ищем последнего собеседника
        UUID targetUUID = lastMessenger.get(player.getUniqueId());
        if (targetUUID == null) {
            player.sendMessage(MessageManager.MSG_NO_REPLY);
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            player.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            // Убираем устаревшую запись
            lastMessenger.remove(player.getUniqueId());
            return true;
        }

        // Проверка: получатель игнорирует отправителя
        if (ignoreManager.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(MessageManager.IGNORE_BLOCKED);
            return true;
        }

        // Собираем текст ответа
        String message = String.join(" ", args);

        // Отправляем сообщения обоим игрокам
        player.sendMessage(msg.msgTo(target.getName(), message));
        target.sendMessage(msg.msgFrom(player.getName(), message));

        // Обновляем последнего собеседника
        lastMessenger.put(target.getUniqueId(), player.getUniqueId());
        lastMessenger.put(player.getUniqueId(), target.getUniqueId());

        // Рассылка SocialSpy
        broadcastSocialSpy(player, target, message);

        return true;
    }

    // ==================== /ignore ====================

    /**
     * Обработать команду /ignore <игрок>.
     * Переключает игнорирование: если уже игнорируется — убирает, иначе — добавляет.
     * Игроки с правом expicore.ignore.exempt не могут быть заигнорены.
     * @param sender отправитель
     * @param args аргументы (имя игрока)
     * @return true
     */
    private boolean handleIgnore(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("expicore.ignore")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MessageManager.INVALID_USAGE + " Используйте: /ignore <игрок>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(MessageManager.PLAYER_NOT_FOUND);
            return true;
        }

        // Нельзя заигнорить себя
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(MessageManager.IGNORE_SELF);
            return true;
        }

        // Проверка: цель имеет защиту от игнорирования
        if (target.hasPermission("expicore.ignore.exempt")) {
            player.sendMessage(MessageManager.IGNORE_EXEMPT);
            return true;
        }

        // Переключаем статус игнорирования
        boolean nowIgnoring = ignoreManager.toggleIgnore(player.getUniqueId(), target.getUniqueId());

        if (nowIgnoring) {
            player.sendMessage(msg.ignoreAdded(target.getName()));
        } else {
            player.sendMessage(msg.ignoreRemoved(target.getName()));
        }

        return true;
    }

    // ==================== /socialspy ====================

    /**
     * Обработать команду /socialspy.
     * Переключает режим просмотра чужих личных сообщений.
     * @param sender отправитель
     * @return true
     */
    private boolean handleSocialSpy(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("expicore.socialspy")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (socialSpies.contains(uuid)) {
            socialSpies.remove(uuid);
            player.sendMessage(MessageManager.SOCIALSPY_DISABLED);
        } else {
            socialSpies.add(uuid);
            player.sendMessage(MessageManager.SOCIALSPY_ENABLED);
        }

        return true;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Разослать перехваченное сообщение всем игрокам с включённым SocialSpy.
     * Отправитель и получатель сообщения не получают дублирование.
     * @param sender отправитель ЛС
     * @param target получатель ЛС
     * @param message текст сообщения
     */
    private void broadcastSocialSpy(Player sender, Player target, String message) {
        if (socialSpies.isEmpty()) return;

        String spyMessage = msg.socialSpyFormat(sender.getName(), target.getName(), message);

        for (UUID spyUUID : socialSpies) {
            // Не дублируем сообщение отправителю и получателю
            if (spyUUID.equals(sender.getUniqueId()) || spyUUID.equals(target.getUniqueId())) {
                continue;
            }
            Player spy = Bukkit.getPlayer(spyUUID);
            if (spy != null && spy.isOnline()) {
                spy.sendMessage(spyMessage);
            }
        }
    }

    /**
     * Очистить данные о последнем собеседнике и SocialSpy при выходе игрока.
     * Вызывается из слушателя при выходе игрока.
     * @param uuid UUID вышедшего игрока
     */
    public void clearLastMessenger(UUID uuid) {
        lastMessenger.remove(uuid);
        socialSpies.remove(uuid);
    }
}
