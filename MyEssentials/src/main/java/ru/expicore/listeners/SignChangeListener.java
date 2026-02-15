package ru.expicore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Слушатель изменения табличек (Sign).
 * Позволяет игрокам с разрешением использовать цветовые коды (&) на табличках.
 * Разрешение expicore.signs.color — обычные цвета (&0-&f, &l, &n, &o, &r).
 * Разрешение expicore.signs.magic — магический/обфускационный стиль (&k).
 */
public class SignChangeListener implements Listener {

    /**
     * Обработать событие изменения таблички.
     * Заменяет &-коды на цветовые коды ChatColor, если у игрока есть разрешение.
     * @param event событие изменения таблички
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        boolean hasColor = event.getPlayer().hasPermission("expicore.signs.color");
        boolean hasMagic = event.getPlayer().hasPermission("expicore.signs.magic");

        if (!hasColor && !hasMagic) return;

        for (int i = 0; i < 4; i++) {
            String line = event.getLine(i);
            if (line == null || line.isEmpty()) continue;

            if (hasColor) {
                // Заменяем все &-коды на цветовые, кроме &k если нет разрешения на магию
                if (hasMagic) {
                    // Разрешены все коды
                    line = ChatColor.translateAlternateColorCodes('&', line);
                } else {
                    // Убираем &k (магический стиль) перед заменой
                    line = line.replaceAll("(?i)&k", "");
                    line = ChatColor.translateAlternateColorCodes('&', line);
                }
            } else if (hasMagic) {
                // Только &k разрешён
                line = line.replaceAll("(?i)&k", ChatColor.MAGIC.toString());
            }

            event.setLine(i, line);
        }
    }
}
