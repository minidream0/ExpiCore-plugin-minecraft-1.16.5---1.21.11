package ru.expicore.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.expicore.ExpiCore;
import ru.expicore.managers.DataManager;
import ru.expicore.managers.MessageManager;

/**
 * Слушатель событий для управления спавном.
 * При первом заходе игрока на сервер — автоматически телепортирует на спавн.
 * Определяет «первый заход» по флагу Player.hasPlayedBefore().
 */
public class SpawnListener implements Listener {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер данных */
    private final DataManager data;

    /**
     * Создать слушатель спавна.
     * @param plugin экземпляр плагина
     */
    public SpawnListener(ExpiCore plugin) {
        this.plugin = plugin;
        this.data = plugin.getDataManager();
    }

    /**
     * Обработка входа игрока на сервер.
     * Если игрок заходит впервые — телепортируем на спавн.
     * Приоритет HIGHEST чтобы выполниться после других плагинов.
     * @param event событие входа
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Проверяем, заходил ли игрок ранее
        if (!player.hasPlayedBefore()) {
            Location spawn = data.getSpawn();
            if (spawn != null) {
                // Телепортируем на спавн с задержкой в 1 тик (гарантирует корректную загрузку мира)
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.teleport(spawn);
                        player.sendMessage(MessageManager.SPAWN_FIRST_JOIN);
                    }
                }, 1L);
            }
        }
    }
}
