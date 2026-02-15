package ru.expicore.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.expicore.ExpiCore;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер системы AFK (Away From Keyboard).
 * Отслеживает бездействие игроков и автоматически помечает их как AFK.
 * Сбрасывает статус при движении, чате, взаимодействии.
 */
public class AFKManager {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /**
     * Множество UUID игроков, находящихся в статусе AFK.
     */
    private final Set<UUID> afkPlayers = ConcurrentHashMap.newKeySet();

    /**
     * Временные метки последней активности игроков.
     * Ключ — UUID, значение — System.currentTimeMillis() последнего действия.
     */
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    /** Задача таймера для проверки бездействия */
    private BukkitTask idleCheckTask;

    /** Порог бездействия для авто-AFK (в миллисекундах), по умолчанию 300 секунд */
    private long afkThresholdMs = 300_000L;

    /**
     * Создать менеджер AFK и запустить таймер проверки бездействия.
     * @param plugin экземпляр плагина
     */
    public AFKManager(ExpiCore plugin) {
        this.plugin = plugin;
        startIdleChecker();
    }

    /**
     * Проверить, находится ли игрок в статусе AFK.
     * @param uuid UUID игрока
     * @return true если AFK
     */
    public boolean isAFK(UUID uuid) {
        return afkPlayers.contains(uuid);
    }

    /**
     * Переключить статус AFK вручную (по команде /afk).
     * Если игрок был AFK — снимает, если нет — устанавливает.
     * Рассылает сообщение в чат всего сервера.
     * @param player игрок
     */
    public void toggleAFK(Player player) {
        UUID uuid = player.getUniqueId();

        if (afkPlayers.contains(uuid)) {
            // Снимаем AFK
            removeAFK(player);
        } else {
            // Устанавливаем AFK
            setAFK(player);
        }
    }

    /**
     * Установить игроку статус AFK и уведомить сервер.
     * @param player игрок
     */
    public void setAFK(Player player) {
        UUID uuid = player.getUniqueId();
        if (afkPlayers.add(uuid)) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "* Игрок " + ChatColor.WHITE + player.getName()
                    + ChatColor.GRAY + " теперь AFK.");
        }
    }

    /**
     * Снять статус AFK с игрока и уведомить сервер.
     * @param player игрок
     */
    public void removeAFK(Player player) {
        UUID uuid = player.getUniqueId();
        if (afkPlayers.remove(uuid)) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "* Игрок " + ChatColor.WHITE + player.getName()
                    + ChatColor.GRAY + " вернулся из AFK.");
        }
    }

    /**
     * Зарегистрировать активность игрока (движение, чат, взаимодействие).
     * Если игрок был AFK — снимает статус автоматически.
     * @param player игрок
     */
    public void registerActivity(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());

        // Если был AFK — снимаем
        if (afkPlayers.contains(uuid)) {
            removeAFK(player);
        }
    }

    /**
     * Очистить данные игрока при выходе с сервера.
     * @param uuid UUID игрока
     */
    public void clearPlayer(UUID uuid) {
        afkPlayers.remove(uuid);
        lastActivity.remove(uuid);
    }

    /**
     * Запустить повторяющуюся задачу проверки бездействия.
     * Каждые 5 секунд (100 тиков) проверяет всех онлайн-игроков.
     * Если время бездействия превышает порог — помечает как AFK.
     */
    private void startIdleChecker() {
        // Проверка каждые 100 тиков (5 секунд)
        idleCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();

                // Инициализируем активность если не было записи
                lastActivity.putIfAbsent(uuid, now);

                // Пропускаем если уже AFK
                if (afkPlayers.contains(uuid)) continue;

                // Проверяем есть ли право на AFK
                if (!player.hasPermission("expicore.afk")) continue;

                // Проверяем порог бездействия
                long lastAct = lastActivity.getOrDefault(uuid, now);
                if (now - lastAct >= afkThresholdMs) {
                    setAFK(player);
                }
            }
        }, 100L, 100L);
    }

    /**
     * Остановить таймер проверки бездействия.
     * Вызывается при выключении плагина.
     */
    public void shutdown() {
        if (idleCheckTask != null) {
            idleCheckTask.cancel();
        }
    }

    /**
     * Установить порог бездействия для авто-AFK.
     * @param seconds количество секунд
     */
    public void setAfkThreshold(int seconds) {
        this.afkThresholdMs = seconds * 1000L;
    }
}
