package ru.expicore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.expicore.ExpiCore;
import ru.expicore.managers.AFKManager;

/**
 * Слушатель движения игрока.
 * Сбрасывает таймер AFK при движении.
 * Очищает данные AFK при выходе игрока.
 */
public class PlayerMoveListener implements Listener {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /**
     * Создать слушатель движения.
     * @param plugin экземпляр плагина
     */
    public PlayerMoveListener(ExpiCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработать событие движения игрока.
     * Регистрируем активность только при реальном перемещении (не повороте головы).
     * @param event событие движения
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Проверяем, что игрок действительно сдвинулся (а не просто повернул голову)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        AFKManager afkManager = plugin.getAFKManager();
        if (afkManager != null) {
            afkManager.registerActivity(event.getPlayer());
        }
    }

    /**
     * Обработать событие выхода игрока.
     * Очищаем данные AFK для вышедшего игрока.
     * @param event событие выхода
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        AFKManager afkManager = plugin.getAFKManager();
        if (afkManager != null) {
            afkManager.clearPlayer(event.getPlayer().getUniqueId());
        }
    }
}
