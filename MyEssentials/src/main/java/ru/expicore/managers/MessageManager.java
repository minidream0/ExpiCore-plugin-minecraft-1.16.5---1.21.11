package ru.expicore.managers;

import org.bukkit.ChatColor;

/**
 * Менеджер сообщений плагина.
 * Все игровые сообщения хранятся здесь на русском языке.
 * Используется кодировка UTF-8 для корректного отображения кириллицы.
 */
public class MessageManager {

    /** Префикс плагина для сообщений в чате */
    public static final String PREFIX = ChatColor.GOLD + "[ExpiCore] " + ChatColor.RESET;

    // ==================== ОБЩИЕ ====================

    /** Сообщение: только для игроков */
    public static final String ONLY_PLAYERS = PREFIX + ChatColor.RED + "Эта команда доступна только для игроков!";

    /** Сообщение: нет прав */
    public static final String NO_PERMISSION = PREFIX + ChatColor.RED + "У вас нет прав для выполнения этой команды!";

    /** Сообщение: игрок не найден */
    public static final String PLAYER_NOT_FOUND = PREFIX + ChatColor.RED + "Игрок не найден!";

    /** Сообщение: неверное использование команды */
    public static final String INVALID_USAGE = PREFIX + ChatColor.RED + "Неверное использование команды!";

    // ==================== ТПА (Телепортация) ====================

    /**
     * Запрос на ТП отправлен.
     * @param target имя целевого игрока
     * @return отформатированное сообщение
     */
    public String tpaSent(String target) {
        return PREFIX + ChatColor.GREEN + "Запрос на телепортацию отправлен игроку " + ChatColor.YELLOW + target + ChatColor.GREEN + ".";
    }

    /**
     * Вы получили запрос на ТП.
     * @param sender имя отправителя
     * @return отформатированное сообщение
     */
    public String tpaReceived(String sender) {
        return PREFIX + ChatColor.GREEN + "Игрок " + ChatColor.YELLOW + sender + ChatColor.GREEN
                + " хочет телепортироваться к вам.\n"
                + PREFIX + ChatColor.GREEN + "Введите " + ChatColor.YELLOW + "/tpaccept"
                + ChatColor.GREEN + " чтобы принять. (60 секунд)";
    }

    /** Сообщение: запрос на ТП истёк */
    public static final String TPA_EXPIRED = PREFIX + ChatColor.RED + "Запрос на телепортацию истёк.";

    /** Сообщение: нет входящих запросов */
    public static final String TPA_NO_PENDING = PREFIX + ChatColor.RED + "У вас нет входящих запросов на телепортацию.";

    /** Сообщение: запрос принят (для отправителя) */
    public static final String TPA_ACCEPTED_SENDER = PREFIX + ChatColor.GREEN + "Ваш запрос на телепортацию принят!";

    /** Сообщение: вы приняли запрос */
    public static final String TPA_ACCEPTED_TARGET = PREFIX + ChatColor.GREEN + "Вы приняли запрос на телепортацию.";

    /** Сообщение: нельзя отправить запрос самому себе */
    public static final String TPA_SELF = PREFIX + ChatColor.RED + "Вы не можете телепортироваться к самому себе!";

    /** Сообщение: у вас уже есть ожидающий запрос */
    public static final String TPA_ALREADY_PENDING = PREFIX + ChatColor.RED + "У этого игрока уже есть входящий запрос. Подождите.";

    // ==================== ДОМА ====================

    /**
     * Дом установлен.
     * @param name название дома
     * @return отформатированное сообщение
     */
    public String homeSet(String name) {
        return PREFIX + ChatColor.GREEN + "Дом " + ChatColor.YELLOW + name + ChatColor.GREEN + " успешно установлен!";
    }

    /**
     * Телепортация к дому.
     * @param name название дома
     * @return отформатированное сообщение
     */
    public String homeTeleported(String name) {
        return PREFIX + ChatColor.GREEN + "Вы телепортированы к дому " + ChatColor.YELLOW + name + ChatColor.GREEN + ".";
    }

    /**
     * Дом удалён.
     * @param name название дома
     * @return отформатированное сообщение
     */
    public String homeDeleted(String name) {
        return PREFIX + ChatColor.GREEN + "Дом " + ChatColor.YELLOW + name + ChatColor.GREEN + " удалён.";
    }

    /**
     * Дом не найден.
     * @param name название дома
     * @return отформатированное сообщение
     */
    public String homeNotFound(String name) {
        return PREFIX + ChatColor.RED + "Дом " + ChatColor.YELLOW + name + ChatColor.RED + " не найден!";
    }

    /** Сообщение: у вас нет домов */
    public static final String NO_HOMES = PREFIX + ChatColor.RED + "У вас нет установленных домов.";

    // ==================== ВАРПЫ ====================

    /**
     * Варп создан.
     * @param name название варпа
     * @return отформатированное сообщение
     */
    public String warpSet(String name) {
        return PREFIX + ChatColor.GREEN + "Варп " + ChatColor.YELLOW + name + ChatColor.GREEN + " успешно создан!";
    }

    /**
     * Телепортация к варпу.
     * @param name название варпа
     * @return отформатированное сообщение
     */
    public String warpTeleported(String name) {
        return PREFIX + ChatColor.GREEN + "Вы телепортированы к варпу " + ChatColor.YELLOW + name + ChatColor.GREEN + ".";
    }

    /**
     * Варп удалён.
     * @param name название варпа
     * @return отформатированное сообщение
     */
    public String warpDeleted(String name) {
        return PREFIX + ChatColor.GREEN + "Варп " + ChatColor.YELLOW + name + ChatColor.GREEN + " удалён.";
    }

    /**
     * Варп не найден.
     * @param name название варпа
     * @return отформатированное сообщение
     */
    public String warpNotFound(String name) {
        return PREFIX + ChatColor.RED + "Варп " + ChatColor.YELLOW + name + ChatColor.RED + " не найден!";
    }

    // ==================== РЕЖИМ ИГРЫ ====================

    /**
     * Режим игры изменён.
     * @param mode название режима
     * @return отформатированное сообщение
     */
    public String gamemodeChanged(String mode) {
        return PREFIX + ChatColor.GREEN + "Игровой режим изменён на " + ChatColor.YELLOW + mode + ChatColor.GREEN + ".";
    }

    /** Сообщение: неизвестный режим игры */
    public static final String GAMEMODE_INVALID = PREFIX + ChatColor.RED + "Неизвестный режим игры! Используйте: survival, creative, adventure, spectator.";

    // ==================== БОГ И ПОЛЁТ ====================

    /** Сообщение: режим бога включён */
    public static final String GOD_ENABLED = PREFIX + ChatColor.GREEN + "Режим неуязвимости " + ChatColor.YELLOW + "включён" + ChatColor.GREEN + ".";

    /** Сообщение: режим бога выключен */
    public static final String GOD_DISABLED = PREFIX + ChatColor.GREEN + "Режим неуязвимости " + ChatColor.YELLOW + "выключен" + ChatColor.GREEN + ".";

    /** Сообщение: полёт включён */
    public static final String FLY_ENABLED = PREFIX + ChatColor.GREEN + "Режим полёта " + ChatColor.YELLOW + "включён" + ChatColor.GREEN + ".";

    /** Сообщение: полёт выключен */
    public static final String FLY_DISABLED = PREFIX + ChatColor.GREEN + "Режим полёта " + ChatColor.YELLOW + "выключен" + ChatColor.GREEN + ".";

    // ==================== КИТЫ ====================

    /**
     * Кит выдан.
     * @param name название кита
     * @return отформатированное сообщение
     */
    public String kitGiven(String name) {
        return PREFIX + ChatColor.GREEN + "Вы получили кит " + ChatColor.YELLOW + name + ChatColor.GREEN + "!";
    }

    /**
     * Кит создан.
     * @param name название кита
     * @return отформатированное сообщение
     */
    public String kitCreated(String name) {
        return PREFIX + ChatColor.GREEN + "Кит " + ChatColor.YELLOW + name + ChatColor.GREEN + " успешно создан!";
    }

    /**
     * Кит удалён.
     * @param name название кита
     * @return отформатированное сообщение
     */
    public String kitDeleted(String name) {
        return PREFIX + ChatColor.GREEN + "Кит " + ChatColor.YELLOW + name + ChatColor.GREEN + " удалён.";
    }

    /**
     * Кит не найден.
     * @param name название кита
     * @return отформатированное сообщение
     */
    public String kitNotFound(String name) {
        return PREFIX + ChatColor.RED + "Кит " + ChatColor.YELLOW + name + ChatColor.RED + " не найден!";
    }

    /** Сообщение: инвентарь пуст — нечего сохранять */
    public static final String KIT_EMPTY_INVENTORY = PREFIX + ChatColor.RED + "Ваш инвентарь пуст! Нечего сохранять в кит.";

    /** Сообщение: нет доступных китов */
    public static final String KIT_NO_KITS = PREFIX + ChatColor.RED + "Нет доступных китов.";

    /** Заголовок GUI редактора китов */
    public static final String KIT_EDITOR_TITLE = ChatColor.DARK_PURPLE + "Редактор китов";

    // ==================== УТИЛИТЫ ИГРОКА ====================

    /** Сообщение: голод восстановлен (себе) */
    public static final String FEED_SELF = PREFIX + ChatColor.GREEN + "Ваш голод восстановлен!";

    /**
     * Голод восстановлен другому игроку.
     * @param target имя целевого игрока
     * @return отформатированное сообщение
     */
    public String feedOther(String target) {
        return PREFIX + ChatColor.GREEN + "Голод игрока " + ChatColor.YELLOW + target + ChatColor.GREEN + " восстановлен.";
    }

    /** Сообщение: вас накормил другой игрок */
    public static final String FEED_BY_OTHER = PREFIX + ChatColor.GREEN + "Ваш голод был восстановлен администратором.";

    /** Сообщение: здоровье восстановлено (себе) */
    public static final String HEAL_SELF = PREFIX + ChatColor.GREEN + "Ваше здоровье полностью восстановлено!";

    /**
     * Здоровье восстановлено другому игроку.
     * @param target имя целевого игрока
     * @return отформатированное сообщение
     */
    public String healOther(String target) {
        return PREFIX + ChatColor.GREEN + "Здоровье игрока " + ChatColor.YELLOW + target + ChatColor.GREEN + " восстановлено.";
    }

    /** Сообщение: вас вылечил другой игрок */
    public static final String HEAL_BY_OTHER = PREFIX + ChatColor.GREEN + "Ваше здоровье было восстановлено администратором.";

    /** Сообщение: предмет починен */
    public static final String REPAIR_HAND = PREFIX + ChatColor.GREEN + "Предмет в руке починен!";

    /** Сообщение: весь инвентарь починен */
    public static final String REPAIR_ALL = PREFIX + ChatColor.GREEN + "Весь инвентарь починен!";

    /** Сообщение: нечего чинить (предмет не повреждается) */
    public static final String REPAIR_NOTHING = PREFIX + ChatColor.RED + "Этот предмет нельзя починить!";

    /** Сообщение: шляпа надета */
    public static final String HAT_SET = PREFIX + ChatColor.GREEN + "Предмет надет на голову!";

    /** Сообщение: рука пуста (для /hat) */
    public static final String HAT_EMPTY = PREFIX + ChatColor.RED + "У вас нет предмета в руке!";

    /** Сообщение: верстак открыт */
    public static final String WORKBENCH_OPENED = PREFIX + ChatColor.GREEN + "Верстак открыт.";

    /** Сообщение: эндер-сундук открыт */
    public static final String ENDERCHEST_OPENED = PREFIX + ChatColor.GREEN + "Эндер-сундук открыт.";

    // ==================== МОДЕРИРОВАНИЕ ====================

    /**
     * Просмотр инвентаря игрока.
     * @param target имя целевого игрока
     * @return отформатированное сообщение
     */
    public String invseeOpened(String target) {
        return PREFIX + ChatColor.GREEN + "Вы просматриваете инвентарь игрока " + ChatColor.YELLOW + target + ChatColor.GREEN + ".";
    }

    /** Префикс объявления */
    public static final String BROADCAST_PREFIX = ChatColor.RED + "" + ChatColor.BOLD + "[Объявление] " + ChatColor.RESET;

    /** Сообщение: инвентарь очищен (себе) */
    public static final String CLEAR_SELF = PREFIX + ChatColor.GREEN + "Ваш инвентарь очищен.";

    /**
     * Инвентарь другого игрока очищен.
     * @param target имя целевого игрока
     * @return отформатированное сообщение
     */
    public String clearOther(String target) {
        return PREFIX + ChatColor.GREEN + "Инвентарь игрока " + ChatColor.YELLOW + target + ChatColor.GREEN + " очищен.";
    }

    /** Сообщение: ваш инвентарь очищен администратором */
    public static final String CLEAR_BY_OTHER = PREFIX + ChatColor.RED + "Ваш инвентарь был очищен администратором.";

    // ==================== СПАВН ====================

    /** Сообщение: телепортация на спавн */
    public static final String SPAWN_TELEPORTED = PREFIX + ChatColor.GREEN + "Вы телепортированы на спавн.";

    /** Сообщение: спавн не установлен */
    public static final String SPAWN_NOT_SET = PREFIX + ChatColor.RED + "Точка спавна не установлена!";

    /** Сообщение: спавн установлен */
    public static final String SPAWN_SET = PREFIX + ChatColor.GREEN + "Точка спавна успешно установлена!";

    /** Сообщение: первый заход — телепорт на спавн */
    public static final String SPAWN_FIRST_JOIN = PREFIX + ChatColor.GREEN + "Добро пожаловать на сервер! Вы телепортированы на спавн.";

    // ==================== ЛИЧНЫЕ СООБЩЕНИЯ ====================

    /**
     * Формат исходящего личного сообщения (отправителю).
     * @param target имя получателя
     * @param message текст сообщения
     * @return отформатированное сообщение
     */
    public String msgTo(String target, String message) {
        return ChatColor.GRAY + "[" + ChatColor.GREEN + "Я" + ChatColor.GRAY + " -> "
                + ChatColor.GREEN + target + ChatColor.GRAY + "] " + ChatColor.WHITE + message;
    }

    /**
     * Формат входящего личного сообщения (получателю).
     * @param sender имя отправителя
     * @param message текст сообщения
     * @return отформатированное сообщение
     */
    public String msgFrom(String sender, String message) {
        return ChatColor.GRAY + "[" + ChatColor.GREEN + sender + ChatColor.GRAY + " -> "
                + ChatColor.GREEN + "Я" + ChatColor.GRAY + "] " + ChatColor.WHITE + message;
    }

    /** Сообщение: некому ответить */
    public static final String MSG_NO_REPLY = PREFIX + ChatColor.RED + "Вам некому отвечать!";

    /** Сообщение: отправить личное сообщение самому себе нельзя */
    public static final String MSG_SELF = PREFIX + ChatColor.RED + "Вы не можете отправить сообщение самому себе!";

    // ==================== САМОУБИЙСТВО ====================

    /** Сообщение: вы покончили с собой */
    public static final String SUICIDE = PREFIX + ChatColor.RED + "Вы покончили с собой.";

    // ==================== ИГНОРИРОВАНИЕ ====================

    /**
     * Игрок добавлен в список игнорируемых.
     * @param target имя игрока
     * @return отформатированное сообщение
     */
    public String ignoreAdded(String target) {
        return PREFIX + ChatColor.GREEN + "Вы теперь игнорируете игрока " + ChatColor.YELLOW + target + ChatColor.GREEN + ".";
    }

    /**
     * Игрок удалён из списка игнорируемых.
     * @param target имя игрока
     * @return отформатированное сообщение
     */
    public String ignoreRemoved(String target) {
        return PREFIX + ChatColor.GREEN + "Вы больше не игнорируете игрока " + ChatColor.YELLOW + target + ChatColor.GREEN + ".";
    }

    /** Сообщение: этого игрока нельзя заигнорить (expicore.ignore.exempt) */
    public static final String IGNORE_EXEMPT = PREFIX + ChatColor.RED + "Этого игрока нельзя заигнорить!";

    /** Сообщение: нельзя заигнорить самого себя */
    public static final String IGNORE_SELF = PREFIX + ChatColor.RED + "Вы не можете заигнорить самого себя!";

    /** Сообщение: сообщение заблокировано — вас игнорируют */
    public static final String IGNORE_BLOCKED = PREFIX + ChatColor.RED + "Этот игрок вас игнорирует.";

    // ==================== SOCIALSPY ====================

    /** Сообщение: socialspy включён */
    public static final String SOCIALSPY_ENABLED = PREFIX + ChatColor.GREEN + "SocialSpy " + ChatColor.YELLOW + "включён" + ChatColor.GREEN + ". Вы видите чужие личные сообщения.";

    /** Сообщение: socialspy выключен */
    public static final String SOCIALSPY_DISABLED = PREFIX + ChatColor.GREEN + "SocialSpy " + ChatColor.YELLOW + "выключен" + ChatColor.GREEN + ".";

    /**
     * Формат перехваченного сообщения для socialspy.
     * @param sender имя отправителя
     * @param target имя получателя
     * @param message текст сообщения
     * @return отформатированное сообщение
     */
    public String socialSpyFormat(String sender, String target, String message) {
        return ChatColor.GRAY + "[SocialSpy] " + ChatColor.DARK_GRAY + sender + " -> " + target + ": " + ChatColor.GRAY + message;
    }

    // ==================== НАКАЗАНИЯ ====================

    /** Сообщение: игрок кикнут */
    public static final String KICK_SUCCESS = PREFIX + ChatColor.GREEN + "Игрок кикнут с сервера.";

    /** Сообщение: игрок замучен */
    public static final String MUTE_SUCCESS = PREFIX + ChatColor.GREEN + "Игрок замучен.";

    /** Сообщение: мут снят */
    public static final String UNMUTE_SUCCESS = PREFIX + ChatColor.GREEN + "Мут снят с игрока.";

    /** Сообщение: игрок забанен */
    public static final String BAN_SUCCESS = PREFIX + ChatColor.GREEN + "Игрок забанен.";

    /** Сообщение: бан снят */
    public static final String UNBAN_SUCCESS = PREFIX + ChatColor.GREEN + "Бан снят с игрока.";

    /** Сообщение: игрок временно забанен */
    public static final String TEMPBAN_SUCCESS = PREFIX + ChatColor.GREEN + "Игрок временно забанен.";

    /** Сообщение: игрок не найден (наказания) */
    public static final String PUNISHMENT_PLAYER_NOT_FOUND = PREFIX + ChatColor.RED + "Игрок не найден или никогда не заходил на сервер.";

    /** Сообщение: игрок не замучен */
    public static final String NOT_MUTED = PREFIX + ChatColor.RED + "Этот игрок не замучен.";

    /** Сообщение: игрок не забанен */
    public static final String NOT_BANNED = PREFIX + ChatColor.RED + "Этот игрок не забанен.";

    /** Сообщение: неверный формат времени */
    public static final String INVALID_TIME_FORMAT = PREFIX + ChatColor.RED + "Неверный формат времени! Используйте: 1d12h30m";

    // ==================== AFK ====================

    /** Сообщение: игрок теперь AFK */
    public static final String AFK_ON = PREFIX + ChatColor.GRAY + "Теперь вы в режиме AFK.";

    /** Сообщение: игрок вышел из AFK */
    public static final String AFK_OFF = PREFIX + ChatColor.GREEN + "Вы вернулись из AFK.";

    // ==================== МОНИТОРИНГ ====================

    /** Сообщение: использование /near */
    public static final String NEAR_USAGE = PREFIX + ChatColor.YELLOW + "Использование: /near [радиус]";
}
