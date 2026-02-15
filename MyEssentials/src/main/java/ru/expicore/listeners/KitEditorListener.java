package ru.expicore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.expicore.ExpiCore;
import ru.expicore.managers.KitManager;
import ru.expicore.managers.MessageManager;

/**
 * Слушатель событий для GUI-редактора китов.
 * Обрабатывает клики в инвентаре редактора:
 * - ЛКМ — выдать кит игроку
 * - ПКМ — удалить кит (требует права expicore.kit.admin)
 */
public class KitEditorListener implements Listener {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /**
     * Создать слушатель GUI-редактора китов.
     * @param plugin экземпляр плагина
     */
    public KitEditorListener(ExpiCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработка клика в инвентаре.
     * Проверяем, является ли инвентарь редактором китов по заголовку.
     * @param event событие клика
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Проверяем что это наш GUI (по заголовку)
        String title = event.getView().getTitle();
        if (!title.equals(MessageManager.KIT_EDITOR_TITLE)) {
            return;
        }

        // Отменяем перемещение предметов в GUI
        event.setCancelled(true);

        // Проверяем что кликнул игрок
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        // Проверяем что кликнули на предмет
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Извлекаем название кита (убираем цветовой код)
        String kitName = ChatColor.stripColor(meta.getDisplayName());

        KitManager kitManager = plugin.getKitManager();
        MessageManager msg = plugin.getMessageManager();

        switch (event.getClick()) {
            case LEFT:
                // ЛКМ — выдать кит
                player.closeInventory();
                if (kitManager.giveKit(player, kitName)) {
                    player.sendMessage(msg.kitGiven(kitName));
                } else {
                    player.sendMessage(msg.kitNotFound(kitName));
                }
                break;

            case RIGHT:
                // ПКМ — удалить кит (только с правами)
                if (!player.hasPermission("expicore.kit.admin")) {
                    player.sendMessage(MessageManager.NO_PERMISSION);
                    return;
                }

                player.closeInventory();
                if (plugin.getDataManager().deleteKit(kitName)) {
                    player.sendMessage(msg.kitDeleted(kitName));
                } else {
                    player.sendMessage(msg.kitNotFound(kitName));
                }
                break;

            default:
                // Другие типы кликов игнорируем
                break;
        }
    }
}
