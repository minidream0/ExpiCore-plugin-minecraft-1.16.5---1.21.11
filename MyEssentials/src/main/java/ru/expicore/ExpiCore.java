package ru.expicore;

import org.bukkit.plugin.java.JavaPlugin;
import ru.expicore.commands.*;
import ru.expicore.listeners.GodListener;
import ru.expicore.listeners.KitEditorListener;
import ru.expicore.listeners.PlayerChatListener;
import ru.expicore.listeners.PlayerLoginListener;
import ru.expicore.listeners.PlayerMoveListener;
import ru.expicore.listeners.SignChangeListener;
import ru.expicore.listeners.SpawnListener;
import ru.expicore.managers.AFKManager;
import ru.expicore.managers.DataManager;
import ru.expicore.managers.IgnoreManager;
import ru.expicore.managers.KitManager;
import ru.expicore.managers.MessageManager;
import ru.expicore.managers.PunishmentManager;

/**
 * Главный класс плагина ExpiCore.
 * Лёгкая альтернатива EssentialsX с полной русской локализацией.
 * Поддерживает Paper/Spigot 1.16.5 — 1.21.1 (только Bukkit API).
 */
public class ExpiCore extends JavaPlugin {

    /** Единственный экземпляр плагина (синглтон) */
    private static ExpiCore instance;

    /** Менеджер сообщений (русская локализация) */
    private MessageManager messageManager;

    /** Менеджер данных (JSON-хранилище домов, варпов, китов, спавна) */
    private DataManager dataManager;

    /** Менеджер китов (Base64 сериализация, GUI) */
    private KitManager kitManager;

    /** Менеджер игнорирования (хранит списки /ignore) */
    private IgnoreManager ignoreManager;

    /** Менеджер наказаний (баны, муты, JSON-хранилище) */
    private PunishmentManager punishmentManager;

    /** Менеджер AFK (автоматическое определение бездействия) */
    private AFKManager afkManager;

    /** Обработчик команд личных сообщений (для очистки при выходе и socialspy) */
    private MessageCommands messageCommands;

    @Override
    public void onEnable() {
        instance = this;

        // Инициализируем менеджеры
        this.messageManager = new MessageManager();
        this.dataManager = new DataManager(this);
        this.kitManager = new KitManager(this, dataManager);
        this.ignoreManager = new IgnoreManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.afkManager = new AFKManager(this);

        // Регистрируем команды телепортации
        TeleportCommands teleportCommands = new TeleportCommands(this);
        getCommand("tpa").setExecutor(teleportCommands);
        getCommand("tpaccept").setExecutor(teleportCommands);

        // Регистрируем команды домов
        HomeCommands homeCommands = new HomeCommands(this);
        getCommand("sethome").setExecutor(homeCommands);
        getCommand("home").setExecutor(homeCommands);
        getCommand("delhome").setExecutor(homeCommands);

        // Регистрируем команды варпов
        WarpCommands warpCommands = new WarpCommands(this);
        getCommand("setwarp").setExecutor(warpCommands);
        getCommand("warp").setExecutor(warpCommands);
        getCommand("delwarp").setExecutor(warpCommands);

        // Регистрируем команды режима игры, бога и полёта
        AdminCommands adminCommands = new AdminCommands(this);
        getCommand("gamemode").setExecutor(adminCommands);
        getCommand("gmc").setExecutor(adminCommands);
        getCommand("gms").setExecutor(adminCommands);
        getCommand("gmsp").setExecutor(adminCommands);
        getCommand("god").setExecutor(adminCommands);
        getCommand("fly").setExecutor(adminCommands);

        // Регистрируем команды китов
        KitCommands kitCommands = new KitCommands(this);
        getCommand("kit").setExecutor(kitCommands);

        // Регистрируем утилитарные команды игрока (heal, feed, repair, hat, suicide и т.д.)
        PlayerUtilCommands playerUtilCommands = new PlayerUtilCommands(this);
        getCommand("feed").setExecutor(playerUtilCommands);
        getCommand("heal").setExecutor(playerUtilCommands);
        getCommand("repair").setExecutor(playerUtilCommands);
        getCommand("hat").setExecutor(playerUtilCommands);
        getCommand("suicide").setExecutor(playerUtilCommands);
        getCommand("workbench").setExecutor(playerUtilCommands);
        getCommand("enderchest").setExecutor(playerUtilCommands);

        // Регистрируем команды модерирования
        ModerationCommands moderationCommands = new ModerationCommands(this);
        getCommand("invsee").setExecutor(moderationCommands);
        getCommand("broadcast").setExecutor(moderationCommands);
        getCommand("clear").setExecutor(moderationCommands);

        // Регистрируем команды спавна
        SpawnCommands spawnCommands = new SpawnCommands(this);
        getCommand("spawn").setExecutor(spawnCommands);
        getCommand("setspawn").setExecutor(spawnCommands);

        // Регистрируем команды личных сообщений, /ignore и /socialspy
        this.messageCommands = new MessageCommands(this);
        getCommand("msg").setExecutor(messageCommands);
        getCommand("r").setExecutor(messageCommands);
        getCommand("ignore").setExecutor(messageCommands);
        getCommand("socialspy").setExecutor(messageCommands);

        // Регистрируем команды наказаний (kick, mute, unmute, ban, unban, tempban)
        PunishmentCommands punishmentCommands = new PunishmentCommands(this);
        getCommand("kick").setExecutor(punishmentCommands);
        getCommand("mute").setExecutor(punishmentCommands);
        getCommand("unmute").setExecutor(punishmentCommands);
        getCommand("ban").setExecutor(punishmentCommands);
        getCommand("unban").setExecutor(punishmentCommands);
        getCommand("tempban").setExecutor(punishmentCommands);

        // Регистрируем команду AFK
        AFKCommands afkCommands = new AFKCommands(this);
        getCommand("afk").setExecutor(afkCommands);

        // Регистрируем команды мониторинга (gc, near)
        MonitoringCommands monitoringCommands = new MonitoringCommands(this);
        getCommand("gc").setExecutor(monitoringCommands);
        getCommand("near").setExecutor(monitoringCommands);

        // Регистрируем слушателей событий
        getServer().getPluginManager().registerEvents(new GodListener(this), this);
        getServer().getPluginManager().registerEvents(new KitEditorListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new SignChangeListener(), this);

        getLogger().info("ExpiCore успешно запущен!");
    }

    @Override
    public void onDisable() {
        // Сохраняем все данные перед выключением
        if (dataManager != null) {
            dataManager.saveAll();
        }
        if (ignoreManager != null) {
            ignoreManager.saveIgnores();
        }
        if (punishmentManager != null) {
            punishmentManager.savePunishments();
        }
        if (afkManager != null) {
            afkManager.shutdown();
        }
        getLogger().info("ExpiCore выключен.");
    }

    /**
     * Получить экземпляр плагина.
     * @return экземпляр ExpiCore
     */
    public static ExpiCore getInstance() {
        return instance;
    }

    /**
     * Получить менеджер сообщений.
     * @return менеджер сообщений
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * Получить менеджер данных.
     * @return менеджер данных
     */
    public DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Получить менеджер китов.
     * @return менеджер китов
     */
    public KitManager getKitManager() {
        return kitManager;
    }

    /**
     * Получить менеджер игнорирования.
     * @return менеджер игнорирования
     */
    public IgnoreManager getIgnoreManager() {
        return ignoreManager;
    }

    /**
     * Получить обработчик личных сообщений.
     * @return обработчик личных сообщений
     */
    public MessageCommands getMessageCommands() {
        return messageCommands;
    }

    /**
     * Получить менеджер наказаний.
     * @return менеджер наказаний
     */
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    /**
     * Получить менеджер AFK.
     * @return менеджер AFK
     */
    public AFKManager getAFKManager() {
        return afkManager;
    }
}
