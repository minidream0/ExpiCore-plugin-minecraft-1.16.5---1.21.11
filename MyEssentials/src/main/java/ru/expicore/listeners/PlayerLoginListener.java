package ru.expicore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.expicore.ExpiCore;
import ru.expicore.managers.PunishmentManager;

/**
 * Слушатель подключения игрока.
 * Проверяет наличие бана при входе на сервер.
 * Если игрок забанен — отклоняем подключение с экраном бана.
 */
public class PlayerLoginListener implements Listener {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /**
     * Создать слушатель логина.
     * @param plugin экземпляр плагина
     */
    public PlayerLoginListener(ExpiCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработать событие предварительного входа (AsyncPlayerPreLoginEvent).
     * Проверяет бан по UUID. Если забанен — показывает экран бана с причиной и оставшимся временем.
     * @param event событие предварительного входа
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        PunishmentManager punishmentManager = plugin.getPunishmentManager();
        if (punishmentManager == null) return;

        if (!punishmentManager.isBanned(event.getUniqueId())) return;

        PunishmentManager.PunishmentData ban = punishmentManager.getBan(event.getUniqueId());
        if (ban == null) return;

        // Формируем экран бана
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.RED).append("§l").append("Вы забанены на этом сервере!\n\n");

        // Причина
        String reason = ban.reason != null ? ban.reason : "Не указана";
        message.append(ChatColor.YELLOW).append("Причина: ").append(ChatColor.WHITE).append(reason).append("\n");

        // Время
        if (ban.expireTime > 0) {
            long remaining = ban.expireTime - System.currentTimeMillis();
            if (remaining > 0) {
                String timeLeft = PunishmentManager.formatTime(remaining);
                message.append(ChatColor.YELLOW).append("Осталось: ")
                        .append(ChatColor.WHITE).append(timeLeft).append("\n");
            } else {
                // Бан истёк — автоматически снимаем
                punishmentManager.unban(event.getUniqueId());
                return;
            }
        } else {
            message.append(ChatColor.RED).append("Срок: ").append(ChatColor.DARK_RED).append("Навсегда\n");
        }

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message.toString());
    }
}
