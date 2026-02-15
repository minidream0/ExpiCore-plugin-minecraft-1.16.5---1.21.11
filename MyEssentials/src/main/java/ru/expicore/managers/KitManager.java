package ru.expicore.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import ru.expicore.ExpiCore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Менеджер китов (наборов предметов).
 * Использует Base64 сериализацию для полного сохранения NBT-данных предметов
 * (зачарования, названия, описания и т.д.).
 * Также отвечает за создание GUI-редактора китов.
 */
public class KitManager {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер данных для сохранения/загрузки */
    private final DataManager dataManager;

    /**
     * Создать менеджер китов.
     * @param plugin экземпляр плагина
     * @param dataManager менеджер данных
     */
    public KitManager(ExpiCore plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    // ==================== СЕРИАЛИЗАЦИЯ Base64 ====================

    /**
     * Сериализовать ItemStack в Base64-строку.
     * Используется BukkitObjectOutputStream для полного сохранения NBT-данных.
     * @param item предмет для сериализации
     * @return Base64-строка
     */
    public String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка сериализации предмета: " + e.getMessage());
            return null;
        }
    }

    /**
     * Десериализовать ItemStack из Base64-строки.
     * @param base64 Base64-строка
     * @return ItemStack или null при ошибке
     */
    public ItemStack itemFromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка десериализации предмета: " + e.getMessage());
            return null;
        }
    }

    // ==================== ОПЕРАЦИИ С КИТАМИ ====================

    /**
     * Создать кит из текущего инвентаря игрока.
     * Пустые слоты пропускаются.
     * @param player игрок
     * @param name название кита
     * @return true если кит успешно создан
     */
    public boolean createKitFromInventory(Player player, String name) {
        ItemStack[] contents = player.getInventory().getContents();
        List<String> items = new ArrayList<>();

        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                String base64 = itemToBase64(item);
                if (base64 != null) {
                    items.add(base64);
                }
            }
        }

        if (items.isEmpty()) {
            return false;
        }

        dataManager.setKit(name, items);
        return true;
    }

    /**
     * Выдать кит игроку.
     * Предметы добавляются в инвентарь. Если инвентарь полон, предметы выбрасываются на землю.
     * @param player игрок
     * @param name название кита
     * @return true если кит найден и выдан
     */
    public boolean giveKit(Player player, String name) {
        List<String> items = dataManager.getKit(name);
        if (items == null) {
            return false;
        }

        for (String base64 : items) {
            ItemStack item = itemFromBase64(base64);
            if (item != null) {
                // Добавляем предмет; если инвентарь полон — выбрасываем на землю
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                for (ItemStack leftover : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        }

        return true;
    }

    // ==================== GUI РЕДАКТОР ====================

    /**
     * Открыть GUI-редактор китов.
     * Отображает все доступные киты в виде инвентаря.
     * Каждый кит представлен как сундук с названием и количеством предметов.
     * Доступны действия: ЛКМ — получить, ПКМ — удалить (с правами).
     * @param player игрок
     */
    public void openKitEditor(Player player) {
        Set<String> kitNames = dataManager.getKitNames();

        // Размер GUI (кратно 9, максимум 54)
        int size = Math.min(54, ((kitNames.size() / 9) + 1) * 9);
        if (size < 9) size = 9;

        Inventory gui = Bukkit.createInventory(null, size, MessageManager.KIT_EDITOR_TITLE);

        int slot = 0;
        for (String kitName : kitNames) {
            if (slot >= size) break;

            List<String> items = dataManager.getKit(kitName);
            int itemCount = items != null ? items.size() : 0;

            // Создаём предмет-иконку для кита
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + kitName);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Предметов: " + ChatColor.WHITE + itemCount);
                lore.add("");
                lore.add(ChatColor.GREEN + "ЛКМ — получить кит");
                if (player.hasPermission("expicore.kit.admin")) {
                    lore.add(ChatColor.RED + "ПКМ — удалить кит");
                }
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }

            gui.setItem(slot, icon);
            slot++;
        }

        player.openInventory(gui);
    }
}
