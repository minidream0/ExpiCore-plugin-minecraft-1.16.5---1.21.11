package ru.expicore.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import ru.expicore.ExpiCore;
import ru.expicore.managers.MessageManager;

/**
 * Обработчик утилитарных команд игрока:
 * /feed, /heal, /repair, /hat, /suicide, /workbench, /enderchest.
 * Команды для удобства VIP-игроков и выживания.
 */
public class PlayerUtilCommands implements CommandExecutor {

    /** Ссылка на главный плагин */
    private final ExpiCore plugin;

    /** Менеджер сообщений */
    private final MessageManager msg;

    /**
     * Создать обработчик утилитарных команд.
     * @param plugin экземпляр главного плагина
     */
    public PlayerUtilCommands(ExpiCore plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "feed":
                return handleFeed(sender, args);
            case "heal":
                return handleHeal(sender, args);
            case "repair":
                return handleRepair(sender, args);
            case "hat":
                return handleHat(sender);
            case "suicide":
                return handleSuicide(sender);
            case "workbench":
                return handleWorkbench(sender);
            case "enderchest":
                return handleEnderchest(sender);
            default:
                return false;
        }
    }

    // ==================== /feed ====================

    /**
     * Обработать команду /feed [игрок].
     * Восстанавливает голод (20 ед.) и насыщение (20 ед.).
     * Без аргумента — себе, с аргументом — другому (требует expicore.feed.others).
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleFeed(CommandSender sender, String[] args) {
        if (args.length > 0) {
            // Кормим другого игрока
            if (!sender.hasPermission("expicore.feed.others")) {
                sender.sendMessage(MessageManager.NO_PERMISSION);
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(MessageManager.PLAYER_NOT_FOUND);
                return true;
            }
            target.setFoodLevel(20);
            target.setSaturation(20f);
            target.sendMessage(MessageManager.FEED_BY_OTHER);
            sender.sendMessage(msg.feedOther(target.getName()));
            return true;
        }

        // Кормим себя
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.feed")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.sendMessage(MessageManager.FEED_SELF);
        return true;
    }

    // ==================== /heal ====================

    /**
     * Обработать команду /heal [игрок].
     * Восстанавливает здоровье, голод и снимает все эффекты зелий.
     * Без аргумента — себе, с аргументом — другому (требует expicore.heal.others).
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleHeal(CommandSender sender, String[] args) {
        if (args.length > 0) {
            // Лечим другого игрока
            if (!sender.hasPermission("expicore.heal.others")) {
                sender.sendMessage(MessageManager.NO_PERMISSION);
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(MessageManager.PLAYER_NOT_FOUND);
                return true;
            }
            healPlayer(target);
            target.sendMessage(MessageManager.HEAL_BY_OTHER);
            sender.sendMessage(msg.healOther(target.getName()));
            return true;
        }

        // Лечим себя
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.heal")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }
        healPlayer(player);
        player.sendMessage(MessageManager.HEAL_SELF);
        return true;
    }

    /**
     * Полностью вылечить игрока: здоровье, голод, убрать огонь и эффекты зелий.
     * @param player целевой игрок
     */
    @SuppressWarnings("deprecation")
    private void healPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
        // Снимаем все активные эффекты зелий
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    // ==================== /repair ====================

    /**
     * Обработать команду /repair [all].
     * Без аргумента — чинит предмет в руке (требует expicore.repair.hand).
     * С аргументом "all" — чинит весь инвентарь (требует expicore.repair.all).
     * @param sender отправитель команды
     * @param args аргументы
     * @return true
     */
    private boolean handleRepair(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
            // Проверка права на починку всего инвентаря
            if (!player.hasPermission("expicore.repair.all")) {
                player.sendMessage(MessageManager.NO_PERMISSION);
                return true;
            }
            // Чиним все предметы в инвентаре
            for (ItemStack item : player.getInventory().getContents()) {
                repairItem(item);
            }
            // Также чиним броню
            for (ItemStack item : player.getInventory().getArmorContents()) {
                repairItem(item);
            }
            player.sendMessage(MessageManager.REPAIR_ALL);
        } else {
            // Проверка права на починку предмета в руке
            if (!player.hasPermission("expicore.repair.hand")) {
                player.sendMessage(MessageManager.NO_PERMISSION);
                return true;
            }
            // Чиним только предмет в руке
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR || !repairItem(hand)) {
                player.sendMessage(MessageManager.REPAIR_NOTHING);
                return true;
            }
            player.sendMessage(MessageManager.REPAIR_HAND);
        }
        return true;
    }

    /**
     * Починить один предмет (сбросить прочность до максимума).
     * @param item предмет для починки
     * @return true если предмет был починен, false если не повреждаемый
     */
    private boolean repairItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            if (damageable.getDamage() > 0) {
                damageable.setDamage(0);
                item.setItemMeta(meta);
                return true;
            }
        }
        return false;
    }

    // ==================== /hat ====================

    /**
     * Обработать команду /hat.
     * Надевает предмет из основной руки на голову (слот шлема).
     * Если на голове уже есть предмет — он перемещается в руку.
     * @param sender отправитель
     * @return true
     */
    private boolean handleHat(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.hat")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            player.sendMessage(MessageManager.HAT_EMPTY);
            return true;
        }

        // Обмениваем предмет в руке и шлем
        ItemStack currentHelmet = player.getInventory().getHelmet();
        player.getInventory().setHelmet(hand);
        player.getInventory().setItemInMainHand(currentHelmet);
        player.sendMessage(MessageManager.HAT_SET);
        return true;
    }

    // ==================== /suicide ====================

    /**
     * Обработать команду /suicide.
     * Убивает игрока (устанавливает здоровье в 0).
     * @param sender отправитель
     * @return true
     */
    private boolean handleSuicide(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.suicide")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        player.sendMessage(MessageManager.SUICIDE);
        player.setHealth(0.0);
        return true;
    }

    // ==================== /workbench ====================

    /**
     * Обработать команду /workbench.
     * Открывает виртуальный верстак (окно крафта 3×3).
     * @param sender отправитель
     * @return true
     */
    private boolean handleWorkbench(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.workbench")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        player.openWorkbench(null, true);
        player.sendMessage(MessageManager.WORKBENCH_OPENED);
        return true;
    }

    // ==================== /enderchest ====================

    /**
     * Обработать команду /enderchest.
     * Открывает персональный эндер-сундук игрока.
     * @param sender отправитель
     * @return true
     */
    private boolean handleEnderchest(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.ONLY_PLAYERS);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("expicore.enderchest")) {
            player.sendMessage(MessageManager.NO_PERMISSION);
            return true;
        }

        player.openInventory(player.getEnderChest());
        player.sendMessage(MessageManager.ENDERCHEST_OPENED);
        return true;
    }
}
