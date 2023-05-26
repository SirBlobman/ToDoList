package com.github.sirblobman.todo.list.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.api.language.ComponentHelper;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.language.replacer.ComponentReplacer;
import com.github.sirblobman.api.language.replacer.Replacer;
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.todo.list.ToDoListPlugin;
import com.github.sirblobman.api.shaded.adventure.text.Component;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;

public final class SubCommandAdd extends Command {
    private final ToDoListPlugin plugin;

    public SubCommandAdd(@NotNull ToDoListPlugin plugin) {
        super(plugin, "add");
        setPermissionName("to-do-list.command.to-do-list.add");
        this.plugin = plugin;
    }

    @Override
    protected @NotNull LanguageManager getLanguageManager() {
        return this.plugin.getLanguageManager();
    }

    @Override
    protected @NotNull List<String> onTabComplete(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> valueList = Arrays.asList("global", "self");
            return getMatching(args[0], valueList);
        }

        if (args.length == 2) {
            return Collections.singletonList("task...");
        }

        return Collections.emptyList();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length < 2) {
            return false;
        }

        LanguageManager languageManager = getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        String sub = args[0].toLowerCase(Locale.US);
        String newItem = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (sub.equals("global")) {
            if (!checkPermission(sender, getGlobalEditPermission(), true)) {
                return true;
            }

            List<String> globalToDoList = getGlobalToDoList();
            globalToDoList.add(newItem);
            setGlobalToDoList(globalToDoList);

            Replacer replacer = new ComponentReplacer("{task}", fixTask(newItem, miniMessage));
            sendMessage(sender, "to-do-list.add-task", replacer);
            return true;
        }

        if (sub.equals("self")) {
            if (!(sender instanceof Player)) {
                languageManager.sendMessage(sender, "error.not-player");
                return true;
            }

            Player player = (Player) sender;
            List<String> selfToDoList = getSelfToDoList(player);
            selfToDoList.add(newItem);
            setSelfToDoList(player, selfToDoList);

            Replacer replacer = new ComponentReplacer("{task}", fixTask(newItem, miniMessage));
            sendMessage(sender, "to-do-list.add-task", replacer);
            return true;
        }

        return false;
    }

    private @NotNull YamlConfiguration getConfiguration() {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        return configurationManager.get("config.yml");
    }

    private @NotNull YamlConfiguration getGlobalConfiguration() {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        return configurationManager.get("global.yml");
    }

    private @NotNull String getGlobalEditPermission() {
        YamlConfiguration config = getConfiguration();
        return config.getString("global-list.edit-permission");
    }

    private @NotNull List<String> getGlobalToDoList() {
        YamlConfiguration config = getGlobalConfiguration();
        return config.getStringList("to-do-list");
    }

    private void setGlobalToDoList(@NotNull List<String> taskList) {
        YamlConfiguration config = getGlobalConfiguration();
        config.set("to-do-list", taskList);

        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        configurationManager.save("global.yml");
    }

    private @NotNull List<String> getSelfToDoList(@NotNull Player player) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration data = playerDataManager.get(player);
        return data.getStringList("to-do-list");
    }

    private void setSelfToDoList(@NotNull Player player, @NotNull List<String> taskList) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration data = playerDataManager.get(player);
        data.set("to-do-list", taskList);
        playerDataManager.save(player);
    }

    private @NotNull Component fixTask(@NotNull String original, @NotNull MiniMessage miniMessage) {
        if (original.contains("&") || original.contains("§")) {
            String legacy = MessageUtility.color(original);
            return ComponentHelper.toComponent(legacy);
        }

        return miniMessage.deserialize(original);
    }
}
