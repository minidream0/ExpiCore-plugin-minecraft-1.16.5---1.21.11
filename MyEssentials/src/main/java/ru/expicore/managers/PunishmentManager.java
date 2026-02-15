package ru.expicore.managers;

import com.google.gson.*;
import ru.expicore.ExpiCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер наказаний (баны, муты).
 * Хранит активные наказания в JSON-файле punishments.json.
 * Поддерживает временные и перманентные баны/муты.
 * Проверка истечения происходит при логине (бан) и при отправке сообщения (мут).
 */
public class PunishmentManager {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** JSON-сериализатор с красивым форматированием */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Активные баны.
     * Ключ — UUID игрока (строка), значение — данные бана.
     */
    private final Map<String, PunishmentData> bans = new ConcurrentHashMap<>();

    /**
     * Активные муты.
     * Ключ — UUID игрока (строка), значение — данные мута.
     */
    private final Map<String, PunishmentData> mutes = new ConcurrentHashMap<>();

    /**
     * Кэш имя->UUID для разбанов/анмутов офлайн-игроков.
     * Заполняется при сохранении наказания.
     */
    private final Map<String, String> nameToUUID = new ConcurrentHashMap<>();

    /**
     * Создать менеджер наказаний и загрузить данные.
     * @param plugin экземпляр плагина
     */
    public PunishmentManager(ExpiCore plugin) {
        this.plugin = plugin;
        loadPunishments();
    }

    // ==================== ОПЕРАЦИИ С БАНАМИ ====================

    /**
     * Забанить игрока.
     * @param uuid UUID игрока
     * @param playerName имя игрока (для кэша)
     * @param reason причина бана
     * @param expireTime время истечения (мс), -1 для перманентного
     */
    public void ban(UUID uuid, String playerName, String reason, long expireTime) {
        PunishmentData data = new PunishmentData();
        data.playerName = playerName;
        data.reason = reason;
        data.expireTime = expireTime;
        data.createdTime = System.currentTimeMillis();
        bans.put(uuid.toString(), data);
        nameToUUID.put(playerName.toLowerCase(), uuid.toString());
        savePunishments();
    }

    /**
     * Разбанить игрока по UUID.
     * @param uuid UUID игрока
     * @return true если бан был снят
     */
    public boolean unban(UUID uuid) {
        boolean removed = bans.remove(uuid.toString()) != null;
        if (removed) savePunishments();
        return removed;
    }

    /**
     * Разбанить игрока по имени.
     * Используется когда игрок офлайн и нет UUID.
     * @param playerName имя игрока
     * @return true если бан был снят
     */
    public boolean unbanByName(String playerName) {
        String uuidStr = nameToUUID.get(playerName.toLowerCase());
        if (uuidStr != null) {
            boolean removed = bans.remove(uuidStr) != null;
            if (removed) savePunishments();
            return removed;
        }
        // Поиск по имени в данных бана
        for (Map.Entry<String, PunishmentData> entry : bans.entrySet()) {
            if (entry.getValue().playerName.equalsIgnoreCase(playerName)) {
                bans.remove(entry.getKey());
                savePunishments();
                return true;
            }
        }
        return false;
    }

    /**
     * Проверить, забанен ли игрок.
     * Автоматически снимает истёкшие баны.
     * @param uuid UUID игрока
     * @return данные бана или null если не забанен
     */
    public PunishmentData getBan(UUID uuid) {
        PunishmentData data = bans.get(uuid.toString());
        if (data == null) return null;

        // Проверка истечения (если не перманентный)
        if (data.expireTime != -1 && System.currentTimeMillis() >= data.expireTime) {
            bans.remove(uuid.toString());
            savePunishments();
            return null;
        }
        return data;
    }

    /**
     * Проверить, забанен ли игрок (без автоочистки).
     * @param uuid UUID игрока
     * @return true если забанен
     */
    public boolean isBanned(UUID uuid) {
        return getBan(uuid) != null;
    }

    // ==================== ОПЕРАЦИИ С МУТАМИ ====================

    /**
     * Замутить игрока.
     * @param uuid UUID игрока
     * @param playerName имя игрока
     * @param reason причина мута
     * @param expireTime время истечения (мс), -1 для перманентного
     */
    public void mute(UUID uuid, String playerName, String reason, long expireTime) {
        PunishmentData data = new PunishmentData();
        data.playerName = playerName;
        data.reason = reason;
        data.expireTime = expireTime;
        data.createdTime = System.currentTimeMillis();
        mutes.put(uuid.toString(), data);
        nameToUUID.put(playerName.toLowerCase(), uuid.toString());
        savePunishments();
    }

    /**
     * Снять мут с игрока по UUID.
     * @param uuid UUID игрока
     * @return true если мут был снят
     */
    public boolean unmute(UUID uuid) {
        boolean removed = mutes.remove(uuid.toString()) != null;
        if (removed) savePunishments();
        return removed;
    }

    /**
     * Снять мут с игрока по имени (для офлайн-игроков).
     * @param playerName имя игрока
     * @return true если мут был снят
     */
    public boolean unmuteByName(String playerName) {
        String uuidStr = nameToUUID.get(playerName.toLowerCase());
        if (uuidStr != null) {
            boolean removed = mutes.remove(uuidStr) != null;
            if (removed) savePunishments();
            return removed;
        }
        for (Map.Entry<String, PunishmentData> entry : mutes.entrySet()) {
            if (entry.getValue().playerName.equalsIgnoreCase(playerName)) {
                mutes.remove(entry.getKey());
                savePunishments();
                return true;
            }
        }
        return false;
    }

    /**
     * Получить данные мута игрока.
     * Автоматически снимает истёкшие муты.
     * @param uuid UUID игрока
     * @return данные мута или null если не замучен
     */
    public PunishmentData getMute(UUID uuid) {
        PunishmentData data = mutes.get(uuid.toString());
        if (data == null) return null;

        // Проверка истечения
        if (data.expireTime != -1 && System.currentTimeMillis() >= data.expireTime) {
            mutes.remove(uuid.toString());
            savePunishments();
            return null;
        }
        return data;
    }

    /**
     * Проверить, замучен ли игрок.
     * @param uuid UUID игрока
     * @return true если замучен
     */
    public boolean isMuted(UUID uuid) {
        return getMute(uuid) != null;
    }

    // ==================== УТИЛИТЫ ====================

    /**
     * Разобрать строку формата времени (1d, 12h, 30m, 15s) в миллисекунды.
     * @param timeStr строка времени, например "1d12h30m"
     * @return количество миллисекунд, или -1 при ошибке парсинга
     */
    public static long parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return -1;

        long totalMs = 0;
        StringBuilder numberBuf = new StringBuilder();

        for (char c : timeStr.toCharArray()) {
            if (Character.isDigit(c)) {
                numberBuf.append(c);
            } else {
                if (numberBuf.length() == 0) return -1;
                long value = Long.parseLong(numberBuf.toString());
                numberBuf.setLength(0);

                switch (Character.toLowerCase(c)) {
                    case 's':
                        totalMs += value * 1000L;
                        break;
                    case 'm':
                        totalMs += value * 60 * 1000L;
                        break;
                    case 'h':
                        totalMs += value * 60 * 60 * 1000L;
                        break;
                    case 'd':
                        totalMs += value * 24 * 60 * 60 * 1000L;
                        break;
                    default:
                        return -1;
                }
            }
        }

        return totalMs > 0 ? totalMs : -1;
    }

    /**
     * Отформатировать оставшееся время в читаемый вид.
     * @param remainingMs оставшееся время в миллисекундах
     * @return строка вида "1д 2ч 30м"
     */
    public static String formatTime(long remainingMs) {
        if (remainingMs <= 0) return "0с";

        long seconds = remainingMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("д ");
        if (hours > 0) sb.append(hours).append("ч ");
        if (minutes > 0) sb.append(minutes).append("м ");
        if (seconds > 0 && days == 0) sb.append(seconds).append("с");

        return sb.toString().trim();
    }

    // ==================== ЗАГРУЗКА / СОХРАНЕНИЕ ====================

    /**
     * Загрузить наказания из punishments.json.
     */
    @SuppressWarnings("deprecation")
    private void loadPunishments() {
        File file = new File(plugin.getDataFolder(), "punishments.json");
        if (!file.exists()) return;

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonElement element = new JsonParser().parse(reader);
            if (element == null || !element.isJsonObject()) return;

            JsonObject root = element.getAsJsonObject();

            // Загружаем баны
            if (root.has("bans") && root.get("bans").isJsonObject()) {
                JsonObject bansObj = root.getAsJsonObject("bans");
                for (Map.Entry<String, JsonElement> entry : bansObj.entrySet()) {
                    PunishmentData data = gson.fromJson(entry.getValue(), PunishmentData.class);
                    bans.put(entry.getKey(), data);
                    if (data.playerName != null) {
                        nameToUUID.put(data.playerName.toLowerCase(), entry.getKey());
                    }
                }
            }

            // Загружаем муты
            if (root.has("mutes") && root.get("mutes").isJsonObject()) {
                JsonObject mutesObj = root.getAsJsonObject("mutes");
                for (Map.Entry<String, JsonElement> entry : mutesObj.entrySet()) {
                    PunishmentData data = gson.fromJson(entry.getValue(), PunishmentData.class);
                    mutes.put(entry.getKey(), data);
                    if (data.playerName != null) {
                        nameToUUID.put(data.playerName.toLowerCase(), entry.getKey());
                    }
                }
            }

            plugin.getLogger().info("Загружено банов: " + bans.size() + ", мутов: " + mutes.size());

        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка загрузки punishments.json: " + e.getMessage());
        }
    }

    /**
     * Сохранить все наказания в punishments.json.
     */
    public void savePunishments() {
        File file = new File(plugin.getDataFolder(), "punishments.json");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        JsonObject root = new JsonObject();

        // Сохраняем баны
        JsonObject bansObj = new JsonObject();
        for (Map.Entry<String, PunishmentData> entry : bans.entrySet()) {
            bansObj.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
        }
        root.add("bans", bansObj);

        // Сохраняем муты
        JsonObject mutesObj = new JsonObject();
        for (Map.Entry<String, PunishmentData> entry : mutes.entrySet()) {
            mutesObj.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
        }
        root.add("mutes", mutesObj);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка сохранения punishments.json: " + e.getMessage());
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ====================

    /**
     * Данные одного наказания (бан или мут).
     */
    public static class PunishmentData {
        /** Имя наказанного игрока */
        public String playerName;
        /** Причина наказания */
        public String reason;
        /** Время истечения наказания (мс с начала эпохи), -1 = вечно */
        public long expireTime;
        /** Время создания наказания */
        public long createdTime;

        /**
         * Является ли наказание перманентным.
         * @return true если вечное
         */
        public boolean isPermanent() {
            return expireTime == -1;
        }

        /**
         * Получить оставшееся время наказания.
         * @return оставшееся время в мс, или -1 если перманентное
         */
        public long getRemainingTime() {
            if (isPermanent()) return -1;
            return Math.max(0, expireTime - System.currentTimeMillis());
        }
    }
}
