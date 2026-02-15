package ru.expicore.managers;

import com.google.gson.*;
import ru.expicore.ExpiCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер игнорирования игроков.
 * Хранит списки заигнорированных UUID для каждого игрока.
 * Данные сохраняются в ignores.json при выключении и загружаются при старте.
 */
public class IgnoreManager {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Парсер/генератор JSON */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Хранилище списков игнорирования.
     * Ключ — UUID игрока, значение — множество UUID заигнорированных.
     */
    private final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();

    /**
     * Создать менеджер игнорирования и загрузить данные.
     * @param plugin экземпляр плагина
     */
    public IgnoreManager(ExpiCore plugin) {
        this.plugin = plugin;
        loadIgnores();
    }

    /**
     * Проверить, игнорирует ли игрок другого.
     * @param player UUID игрока, проверяющего игнор
     * @param target UUID потенциально заигнорированного
     * @return true если target заигнорирован player'ом
     */
    public boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored != null && ignored.contains(target);
    }

    /**
     * Переключить игнорирование: добавить или убрать.
     * @param player UUID игрока
     * @param target UUID цели
     * @return true если цель теперь заигнорирована, false если разигнорирована
     */
    public boolean toggleIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet());
        if (ignored.contains(target)) {
            ignored.remove(target);
            saveIgnores();
            return false; // Разигнорирован
        } else {
            ignored.add(target);
            saveIgnores();
            return true; // Заигнорирован
        }
    }

    /**
     * Загрузить списки игнорирования из ignores.json.
     */
    @SuppressWarnings("deprecation")
    private void loadIgnores() {
        File file = new File(plugin.getDataFolder(), "ignores.json");
        if (!file.exists()) return;

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonElement element = new JsonParser().parse(reader);
            if (element == null || !element.isJsonObject()) return;

            JsonObject root = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                UUID playerUUID = UUID.fromString(entry.getKey());
                Set<UUID> ignored = ConcurrentHashMap.newKeySet();

                if (entry.getValue().isJsonArray()) {
                    for (JsonElement el : entry.getValue().getAsJsonArray()) {
                        ignored.add(UUID.fromString(el.getAsString()));
                    }
                }
                ignoreMap.put(playerUUID, ignored);
            }
            plugin.getLogger().info("Загружено списков игнорирования: " + ignoreMap.size());
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка чтения ignores.json: " + e.getMessage());
        }
    }

    /**
     * Сохранить списки игнорирования в ignores.json.
     */
    public void saveIgnores() {
        File file = new File(plugin.getDataFolder(), "ignores.json");
        JsonObject root = new JsonObject();

        for (Map.Entry<UUID, Set<UUID>> entry : ignoreMap.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            JsonArray arr = new JsonArray();
            for (UUID uuid : entry.getValue()) {
                arr.add(uuid.toString());
            }
            root.add(entry.getKey().toString(), arr);
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка записи ignores.json: " + e.getMessage());
        }
    }
}
