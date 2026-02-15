package ru.expicore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.expicore.ExpiCore;
import ru.expicore.managers.AFKManager;
import ru.expicore.managers.MessageManager;
import ru.expicore.managers.PunishmentManager;

/**
 * Слушатель чата игрока.
 * Проверяет мут перед отправкой сообщения в чат.
 * Сбрасывает таймер AFK при написании в чат.
 */
public class PlayerChatListener implements Listener {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /**
     * Создать слушатель чата.
     * @param plugin экземпляр плагина
     */
    public PlayerChatListener(ExpiCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработать событие чата (высокий приоритет).
     * Если игрок замучен — отменяем событие и показываем причину.
     * Если нет — регистрируем активность для AFK-системы.
     * @param event событие чата
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        PunishmentManager punishmentManager = plugin.getPunishmentManager();
        AFKManager afkManager = plugin.getAFKManager();

        // Проверка мута
        if (punishmentManager != null && punishmentManager.isMuted(event.getPlayer().getUniqueId())) {
            PunishmentManager.PunishmentData mute = punishmentManager.getMute(event.getPlayer().getUniqueId());
            event.setCancelled(true);

            if (mute != null) {
                String reason = mute.reason != null ? mute.reason : "Не указана";

                if (mute.expireTime > 0) {
                    // Временный мут
                    long remaining = mute.expireTime - System.currentTimeMillis();
                    if (remaining > 0) {
                        String timeLeft = PunishmentManager.formatTime(remaining);
                        event.getPlayer().sendMessage(MessageManager.PREFIX + ChatColor.RED
                                + "Вы замучены! Причина: " + ChatColor.WHITE + reason
                                + ChatColor.RED + ". Осталось: " + ChatColor.WHITE + timeLeft);
                    }
                } else {
                    // Перманентный мут
                    event.getPlayer().sendMessage(MessageManager.PREFIX + ChatColor.RED
                            + "Вы замучены навсегда! Причина: " + ChatColor.WHITE + reason);
                }
            } else {
                event.getPlayer().sendMessage(MessageManager.PREFIX + ChatColor.RED + "Вы замучены!");
            }
            return;
        }

        // Регистрация активности для AFK-системы
        if (afkManager != null) {
            afkManager.registerActivity(event.getPlayer());
        }
    }
}
