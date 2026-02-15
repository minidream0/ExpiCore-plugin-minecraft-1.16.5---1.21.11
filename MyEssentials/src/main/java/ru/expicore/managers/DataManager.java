package ru.expicore.managers;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.expicore.ExpiCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер данных плагина.
 * Отвечает за загрузку и сохранение JSON-файлов:
 * - homes.json — дома игроков
 * - warps.json — глобальные варпы
 * - kits.json — киты (предметы в Base64)
 * - spawn.json — глобальная точка спавна
 *
 * Все операции с файлами используют UTF-8 для корректной работы с кириллицей.
 */
public class DataManager {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Парсер JSON */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Дома игроков.
     * Структура: UUID игрока -> (название дома -> локация)
     */
    private final Map<String, Map<String, LocationData>> homes = new ConcurrentHashMap<>();

    /**
     * Глобальные варпы.
     * Структура: название варпа -> локация
     */
    private final Map<String, LocationData> warps = new ConcurrentHashMap<>();

    /**
     * Киты.
     * Структура: название кита -> список Base64-строк (предметы)
     */
    private final Map<String, List<String>> kits = new ConcurrentHashMap<>();

    /**
     * Глобальная точка спавна.
     * null если не установлена.
     */
    private volatile LocationData spawnLocation = null;

    /**
     * Создать менеджер данных и загрузить все файлы.
     * @param plugin экземпляр главного плагина
     */
    public DataManager(ExpiCore plugin) {
        this.plugin = plugin;
        // Создаём папку данных если не существует
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        loadAll();
    }

    // ==================== ЗАГРУЗКА И СОХРАНЕНИЕ ====================

    /**
     * Загрузить все данные из JSON-файлов.
     */
    public void loadAll() {
        loadHomes();
        loadWarps();
        loadKits();
        loadSpawn();
    }

    /**
     * Сохранить все данные в JSON-файлы.
     */
    public void saveAll() {
        saveHomes();
        saveWarps();
        saveKits();
        saveSpawn();
    }

    /**
     * Прочитать JSON из файла.
     * @param fileName имя файла
     * @return JsonElement или null если файл не существует / ошибка
     */
    @SuppressWarnings("deprecation")
    private JsonElement readJsonFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) return null;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            // Используем конструктор JsonParser для совместимости со старым Gson в Spigot 1.16.5
            return new JsonParser().parse(reader);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка чтения файла " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Записать JSON в файл.
     * @param fileName имя файла
     * @param element объект для записи
     */
    private void writeJsonFile(String fileName, JsonElement element) {
        File file = new File(plugin.getDataFolder(), fileName);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(element, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка записи файла " + fileName + ": " + e.getMessage());
        }
    }

    // ==================== ДОМА ====================

    /**
     * Загрузить дома из homes.json.
     */
    private void loadHomes() {
        JsonElement element = readJsonFile("homes.json");
        if (element == null || !element.isJsonObject()) return;

        JsonObject root = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> playerEntry : root.entrySet()) {
            String uuid = playerEntry.getKey();
            Map<String, LocationData> playerHomes = new HashMap<>();

            if (playerEntry.getValue().isJsonObject()) {
                JsonObject homesObj = playerEntry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> homeEntry : homesObj.entrySet()) {
                    LocationData loc = gson.fromJson(homeEntry.getValue(), LocationData.class);
                    playerHomes.put(homeEntry.getKey(), loc);
                }
            }
            homes.put(uuid, playerHomes);
        }
        plugin.getLogger().info("Загружено домов для " + homes.size() + " игроков.");
    }

    /**
     * Сохранить дома в homes.json.
     */
    private void saveHomes() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Map<String, LocationData>> entry : homes.entrySet()) {
            JsonObject playerHomes = new JsonObject();
            for (Map.Entry<String, LocationData> homeEntry : entry.getValue().entrySet()) {
                playerHomes.add(homeEntry.getKey(), gson.toJsonTree(homeEntry.getValue()));
            }
            root.add(entry.getKey(), playerHomes);
        }
        writeJsonFile("homes.json", root);
    }

    /**
     * Установить дом игрока.
     * @param uuid UUID игрока
     * @param name название дома
     * @param location локация
     */
    public void setHome(String uuid, String name, Location location) {
        homes.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(name.toLowerCase(), LocationData.fromBukkit(location));
        saveHomes();
    }

    /**
     * Получить локацию дома игрока.
     * @param uuid UUID игрока
     * @param name название дома
     * @return Location или null если не найден
     */
    public Location getHome(String uuid, String name) {
        Map<String, LocationData> playerHomes = homes.get(uuid);
        if (playerHomes == null) return null;
        LocationData data = playerHomes.get(name.toLowerCase());
        return data != null ? data.toBukkit() : null;
    }

    /**
     * Удалить дом игрока.
     * @param uuid UUID игрока
     * @param name название дома
     * @return true если дом был удалён
     */
    public boolean deleteHome(String uuid, String name) {
        Map<String, LocationData> playerHomes = homes.get(uuid);
        if (playerHomes == null) return false;
        boolean removed = playerHomes.remove(name.toLowerCase()) != null;
        if (removed) saveHomes();
        return removed;
    }

    /**
     * Получить список домов игрока.
     * @param uuid UUID игрока
     * @return набор названий домов
     */
    public Set<String> getHomeNames(String uuid) {
        Map<String, LocationData> playerHomes = homes.get(uuid);
        return playerHomes != null ? playerHomes.keySet() : Collections.emptySet();
    }

    // ==================== ВАРПЫ ====================

    /**
     * Загрузить варпы из warps.json.
     */
    private void loadWarps() {
        JsonElement element = readJsonFile("warps.json");
        if (element == null || !element.isJsonObject()) return;

        JsonObject root = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            LocationData loc = gson.fromJson(entry.getValue(), LocationData.class);
            warps.put(entry.getKey().toLowerCase(), loc);
        }
        plugin.getLogger().info("Загружено " + warps.size() + " варпов.");
    }

    /**
     * Сохранить варпы в warps.json.
     */
    private void saveWarps() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, LocationData> entry : warps.entrySet()) {
            root.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
        }
        writeJsonFile("warps.json", root);
    }

    /**
     * Создать или обновить варп.
     * @param name название варпа
     * @param location локация
     */
    public void setWarp(String name, Location location) {
        warps.put(name.toLowerCase(), LocationData.fromBukkit(location));
        saveWarps();
    }

    /**
     * Получить локацию варпа.
     * @param name название варпа
     * @return Location или null если не найден
     */
    public Location getWarp(String name) {
        LocationData data = warps.get(name.toLowerCase());
        return data != null ? data.toBukkit() : null;
    }

    /**
     * Удалить варп.
     * @param name название варпа
     * @return true если варп был удалён
     */
    public boolean deleteWarp(String name) {
        boolean removed = warps.remove(name.toLowerCase()) != null;
        if (removed) saveWarps();
        return removed;
    }

    /**
     * Получить список всех варпов.
     * @return набор названий варпов
     */
    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    // ==================== КИТЫ ====================

    /**
     * Загрузить киты из kits.json.
     */
    private void loadKits() {
        JsonElement element = readJsonFile("kits.json");
        if (element == null || !element.isJsonObject()) return;

        JsonObject root = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            List<String> items = new ArrayList<>();
            if (entry.getValue().isJsonArray()) {
                for (JsonElement item : entry.getValue().getAsJsonArray()) {
                    items.add(item.getAsString());
                }
            }
            kits.put(entry.getKey().toLowerCase(), items);
        }
        plugin.getLogger().info("Загружено " + kits.size() + " китов.");
    }

    /**
     * Сохранить киты в kits.json.
     */
    private void saveKits() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, List<String>> entry : kits.entrySet()) {
            JsonArray arr = new JsonArray();
            for (String item : entry.getValue()) {
                arr.add(item);
            }
            root.add(entry.getKey(), arr);
        }
        writeJsonFile("kits.json", root);
    }

    /**
     * Сохранить кит.
     * @param name название кита
     * @param items список Base64-строк предметов
     */
    public void setKit(String name, List<String> items) {
        kits.put(name.toLowerCase(), items);
        saveKits();
    }

    /**
     * Получить предметы кита.
     * @param name название кита
     * @return список Base64-строк или null если не найден
     */
    public List<String> getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    /**
     * Удалить кит.
     * @param name название кита
     * @return true если кит был удалён
     */
    public boolean deleteKit(String name) {
        boolean removed = kits.remove(name.toLowerCase()) != null;
        if (removed) saveKits();
        return removed;
    }

    /**
     * Получить список всех китов.
     * @return набор названий китов
     */
    public Set<String> getKitNames() {
        return kits.keySet();
    }

    // ==================== СПАВН ====================

    /**
     * Загрузить точку спавна из spawn.json.
     */
    private void loadSpawn() {
        JsonElement element = readJsonFile("spawn.json");
        if (element == null || !element.isJsonObject()) return;
        spawnLocation = gson.fromJson(element, LocationData.class);
        plugin.getLogger().info("Точка спавна загружена.");
    }

    /**
     * Сохранить точку спавна в spawn.json.
     */
    private void saveSpawn() {
        if (spawnLocation == null) return;
        writeJsonFile("spawn.json", gson.toJsonTree(spawnLocation));
    }

    /**
     * Установить глобальную точку спавна.
     * @param location локация
     */
    public void setSpawn(Location location) {
        spawnLocation = LocationData.fromBukkit(location);
        saveSpawn();
    }

    /**
     * Получить глобальную точку спавна.
     * @return Location или null если не установлена
     */
    public Location getSpawn() {
        return spawnLocation != null ? spawnLocation.toBukkit() : null;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ====================

    /**
     * Класс для сериализации локации в JSON.
     * Хранит мир, координаты и углы обзора.
     */
    public static class LocationData {
        /** Название мира */
        public String world;
        /** Координата X */
        public double x;
        /** Координата Y */
        public double y;
        /** Координата Z */
        public double z;
        /** Угол поворота (горизонтальный) */
        public float yaw;
        /** Угол поворота (вертикальный) */
        public float pitch;

        /**
         * Создать LocationData из Bukkit Location.
         * @param loc Bukkit-локация
         * @return объект LocationData
         */
        public static LocationData fromBukkit(Location loc) {
            LocationData data = new LocationData();
            data.world = loc.getWorld().getName();
            data.x = loc.getX();
            data.y = loc.getY();
            data.z = loc.getZ();
            data.yaw = loc.getYaw();
            data.pitch = loc.getPitch();
            return data;
        }

        /**
         * Преобразовать в Bukkit Location.
         * @return Bukkit-локация или null если мир не найден
         */
        public Location toBukkit() {
            World w = Bukkit.getWorld(world);
            if (w == null) return null;
            return new Location(w, x, y, z, yaw, pitch);
        }
    }
}
