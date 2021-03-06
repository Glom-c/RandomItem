package com.skillw.randomitem.util;

import com.skillw.randomitem.Main;
import com.skillw.randomitem.api.randomitem.RandomItem;
import com.skillw.randomitem.api.section.BaseSection;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.skillw.randomitem.Main.sendWrong;
import static com.skillw.randomitem.util.DebugUtils.sendDebug;
import static com.skillw.randomitem.util.FileUtils.getSubFilesFromFile;
import static com.skillw.randomitem.util.SectionUtils.addRandomSectionsFromConfigSection;
import static com.skillw.randomitem.util.SectionUtils.cloneBaseSectionMap;
import static com.skillw.randomitem.util.StringUtils.getMessage;
import static com.skillw.randomitem.util.StringUtils.messageToText;
import static io.izzel.taboolib.module.locale.TLocaleLoader.getLocalPriorityFirst;

/**
 * @author Glom_
 * @date 2020/10/25 20:16
 */
public final class ConfigUtils {
    private final static ConcurrentHashMap<String, BaseSection> GLOBAL_SECTION_MAP = new ConcurrentHashMap<>();

    private ConfigUtils() {
    }

    public static ConcurrentHashMap<String, BaseSection> getGlobalSectionMapClone() {
        return cloneBaseSectionMap(GLOBAL_SECTION_MAP);
    }


    public static String getLanguage() {
        if (Main.getInstance().getConfig() != null) {
            String lang = getLocalPriorityFirst(Main.getInstance().getPlugin());
            return "languages/" + lang + "/";
        }
        return null;
    }

    public static boolean isCheckVersion() {
        return Main.getInstance().getConfig().getBoolean("options.check-version");
    }

    private static YamlConfiguration loadConfigFile(File file) {
        if (file == null) {
            return null;
        }
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Exception e) {
            sendWrong("Wrong config!");
            sendWrong("Cause: " + messageToText(e.getCause().getMessage()));
        }
        if (config.getKeys(false).isEmpty()) {
            return null;
        }
        return config;
    }

    public static void loadGlobalSection() {
        GLOBAL_SECTION_MAP.clear();
        ConcurrentHashMap<String, BaseSection> sectionMap = new ConcurrentHashMap<>();
        sendDebug("&aLoading Global Sections:", true);
        List<File> fileList = getSubFilesFromFile(Main.getInstance().getGlobalSectionsFile());
        for (File file : fileList) {
            YamlConfiguration config = loadConfigFile(file);
            if (config != null) {
                addRandomSectionsFromConfigSection(sectionMap, config, true);
            }
        }
        GLOBAL_SECTION_MAP.putAll(sectionMap);
    }

    public static void loadRandomItems() {
        Main.getItemManager().getRandomItemHashMap().clear();
        sendDebug("&aLoading items:", true);
        List<File> fileList = getSubFilesFromFile(Main.getInstance().getItemsFile());
        for (File file : fileList) {
            if (file == null) {
                continue;
            }
            YamlConfiguration config = loadConfigFile(file);
            if (config != null) {
                for (String key : config.getKeys(false)) {
                    ConfigurationSection objectSection = config.getConfigurationSection(key);
                    RandomItem randomItem = Main.getRandomItemAPI().createRandomItem(objectSection, true);
                    randomItem.register();
                }
            }
        }
    }

    public static boolean isDebug() {
        try {
            return Main.getInstance().getConfig().getBoolean("options.debug");
        } catch (Exception e) {
            return false;
        }
    }

    public static String getPrefix() {
        return TLocale.asString("prefix");
    }

    public static String getCommandName() {
        return Main.getInstance().getConfig().getString("command.name");
    }

    public static String getCommandPermission() {
        return Main.getInstance().getConfig().getString("command.permission");
    }

    public static List<String> getCommandAliases() {
        return Main.getInstance().getConfig().getStringList("command.aliases");
    }


    public static String getListFormat(int number, RandomItem randomItem) {
        return TLocale.asString("list.format", getPrefix(), String.valueOf(number), randomItem.getId(), getMessage(randomItem.getDisplay()));
    }

    public static String getListPage(int now, int total) {
        return TLocale.asString("list.page", getPrefix(), String.valueOf(now), String.valueOf(total));
    }

    public static String getNoPermissionMessage() {
        return TLocale.asString("no-permission", getPrefix());
    }

    public static void sendSaveItemMessage(CommandSender sender, String item) {
        send(sender, "item.save", item);
    }

    public static HashMap<String, Integer> getAmountMap(ItemStack[] itemStacks) {
        HashMap<String, Integer> amountMap = new HashMap<>();
        for (ItemStack item : itemStacks) {
            String name = item.getItemMeta().getDisplayName();
            if (!amountMap.containsKey(name)) {
                amountMap.put(name, 1);
            } else {
                amountMap.put(name, amountMap.get(name) + 1);
            }
        }
        return amountMap;
    }

    public static void sendGetItemMessage(CommandSender sender, ItemStack[] itemStacks) {
        String path = "item.get";
        if (TLocale.asString(path).isEmpty()) {
            return;
        }
        String str = TLocale.asString(path, getPrefix(), handleFormat(itemStacks));
        sender.sendMessage(str);
    }

    public static String getFormat(String item, int amount) {
        return TLocale.asString("item.format", getPrefix(), item, amount);
    }

    public static String handleFormat(ItemStack[] itemStacks) {
        StringBuilder sb = new StringBuilder();
        HashMap<String, Integer> amountMap = getAmountMap(itemStacks);
        List<String> keys = new ArrayList<>(amountMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            sb.append(getFormat(key, amountMap.get(key)));
            if (i != keys.size() - 1) {
                sb.append(getMessage(" &e,"));
            }
        }
        return sb.toString();
    }

    public static void sendGiveItemMessage(CommandSender sender, String player, ItemStack[] itemStacks) {
        String path = "item.give";
        if (TLocale.asString(path).isEmpty()) {
            return;
        }
        String str = TLocale.asString(path, getPrefix(), player, handleFormat(itemStacks));
        sender.sendMessage(str);
    }

    public static void sendDropItemMessage(CommandSender sender, ItemStack[] itemStacks, String world, String x, String y, String z) {
        String path = "item.drop";
        if (TLocale.asString(path).isEmpty()) {
            return;
        }
        String str = TLocale.asString(path, getPrefix(), handleFormat(itemStacks), world, x, y, z);
        sender.sendMessage(str);
    }

    public static void sendOnlyPlayerMessage(CommandSender sender) {
        send(sender, "only-player");
    }

    public static void sendValidXyzMessage(CommandSender sender) {
        send(sender, "valid.x-y-z");
    }

    public static void sendValidWorldMessage(CommandSender sender, String world) {
        send(sender, "valid.world", world);
    }

    public static void sendValidItemMessage(CommandSender sender, String item) {
        send(sender, "valid.item", item);
    }

    public static void sendValidPlayerMessage(CommandSender sender, String name) {
        send(sender, "valid.player", name);
    }

    public static void sendReloadMessage(CommandSender sender) {
        send(sender, "reload.default");
    }

    public static void sendConfigReloadMessage(CommandSender sender) {
        send(sender, "reload.config");
    }

    public static void sendValidSaveMessage(CommandSender sender, String item) {
        send(sender, "valid.save", item);
    }

    public static void sendValidIdMessage(CommandSender sender, String id) {
        send(sender, "valid.id", id);
    }

    public static void sendValidCommandMessage(CommandSender sender) {
        send(sender, "valid.command");
    }

    public static void sendValidNumberMessage(CommandSender sender) {
        send(sender, "valid.number");
    }

    public static String getListUpMessage() {
        return TLocale.asString("list.up", getPrefix());
    }

    public static String getListLeftMessage() {
        return TLocale.asString("list.left", getPrefix());
    }

    public static String getListRightMessage() {
        return TLocale.asString("list.right", getPrefix());
    }

    public static String getListDownMessage() {
        return TLocale.asString("list.down", getPrefix());
    }

    public static String getVersionLegacyMessage(String latestVersion) {
        return TLocale.asString("check-version.legacy", getPrefix(), latestVersion);
    }

    public static String getVersionLatestMessage() {
        return TLocale.asString("check-version.latest", getPrefix());
    }

    public static String getValidNetWorkMessage() {
        return TLocale.asString("valid.network", getPrefix());
    }

    private static Object[] addPrefixAtFirst(String[] args) {
        String[] newStrings = Arrays.copyOf(args, args.length + 1);
        newStrings[0] = getPrefix();
        System.arraycopy(args, 0, newStrings, 1, args.length);
        return newStrings;
    }

    private static void send(CommandSender sender, String path, String... args) {
        if (TLocale.asString(path).isEmpty()) {
            return;
        }
        if (!(sender instanceof ConsoleCommandSender)) {
            TLocale.sendTo(sender, path, addPrefixAtFirst(args));
        } else {
            TLocale.sendToConsole(path, addPrefixAtFirst(args));
        }
    }

    public static int getVersion() {
        String version = Main.getInstance().getPlugin().getDescription().getVersion().replace(".", "");
        if (version.length() == 3) {
            version += "0";
        }
        return Integer.parseInt(version);
    }

    public static List<String> getCommandMessages() {
        List<String> texts = new ArrayList<>();
        for (String text : TLocale.asStringList("commands")) {
            texts.add(getMessage(text));
        }
        return texts;
    }

    public static int getListNumber() {
        try {
            return Integer.parseInt(TLocale.asString("list.value-of-one-page"));
        } catch (Exception e) {
            sendWrong("Wrong format: &6list.value-of-one-page &cin the lang file &b" + Main.getInstance().getLangFile().getPath() + "/" + getLocalPriorityFirst(Main.getInstance().getPlugin()) + ".yml");
            return 10;
        }
    }

    public static HashMap<String, Object> getMapFromConfigurationSection(ConfigurationSection section, String superKey) {
        HashMap<String, Object> dataMap = new HashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Object object = section.get(key);
                key = (superKey != null && !superKey.isEmpty()) ? superKey + "." + key : key;
                if (object instanceof ConfigurationSection) {
                    dataMap.putAll(getMapFromConfigurationSection((ConfigurationSection) object, key));
                } else {
                    dataMap.put(key, object);
                }
            }
        }
        return dataMap;
    }

}
