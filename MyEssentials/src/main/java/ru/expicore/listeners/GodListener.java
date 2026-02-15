package ru.expicore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.expicore.ExpiCore;
import ru.expicore.commands.AdminCommands;

/**
 * Слушатель событий для режима неуязвимости (бога).
 * Отменяет весь входящий урон для игроков с включённым режимом бога.
 * При выходе игрока из сервера — режим бога снимается для очистки памяти.
 */
public class GodListener implements Listener {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /**
     * Создать слушатель режима бога.
     * @param plugin экземпляр плагина
     */
    public GodListener(ExpiCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработка получения урона сущностью.
     * Если это игрок с включённым режимом бога — отменяем событие.
     * Приоритет HIGHEST для перехвата после других плагинов.
     * @param event событие урона
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // Получаем AdminCommands через команду /god
        // Используем setInvulnerable, поэтому Bukkit обрабатывает сам,
        // но добавляем доп. защиту через слушатель для надёжности
        if (player.isInvulnerable()) {
            event.setCancelled(true);
        }
    }

    /**
     * Обработка выхода игрока с сервера.
     * Снимаем режим бога и очищаем данные последнего собеседника.
     * @param event событие выхода
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().setInvulnerable(false);
        // Очищаем данные последнего собеседника для /r
        if (plugin.getMessageCommands() != null) {
            plugin.getMessageCommands().clearLastMessenger(event.getPlayer().getUniqueId());
        }
    }
}
