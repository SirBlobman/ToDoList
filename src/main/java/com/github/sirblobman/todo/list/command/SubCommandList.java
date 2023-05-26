package com.github.sirblobman.todo.list.command;

import java.util.ArrayList;
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
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.todo.list.ToDoListPlugin;
import com.github.sirblobman.api.shaded.adventure.text.Component;
import com.github.sirblobman.api.shaded.adventure.text.TextReplacementConfig;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;

public final class SubCommandList extends Command {
    private final ToDoListPlugin plugin;

    public SubCommandList(@NotNull ToDoListPlugin plugin) {
        super(plugin, "list");
        setPermissionName("to-do-list.command.to-do-list.list");
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

        return Collections.emptyList();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length < 1) {
            return false;
        }

        String sub = args[0].toLowerCase(Locale.US);
        if (sub.equals("global")) {
            if (!checkPermission(sender, getGlobalViewPermission(), true)) {
                return true;
            }

            List<String> globalToDoList = getGlobalToDoList();
            sendToDoList(sender, globalToDoList);
            return true;
        }

        if (sub.equals("self")) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "error.not-player");
                return true;
            }

            Player player = (Player) sender;
            List<String> selfToDoList = getSelfToDoList(player);
            sendToDoList(player, selfToDoList);
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

    private @NotNull String getGlobalViewPermission() {
        YamlConfiguration config = getConfiguration();
        return config.getString("global-list.view-permission");
    }

    private @NotNull List<String> getGlobalToDoList() {
        YamlConfiguration config = getGlobalConfiguration();
        return config.getStringList("to-do-list");
    }

    private @NotNull List<String> getSelfToDoList(@NotNull Player player) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration data = playerDataManager.get(player);
        return data.getStringList("to-do-list");
    }

    private void sendToDoList(@NotNull CommandSender sender, @NotNull List<String> taskList) {
        if (taskList.isEmpty()) {
            sendMessage(sender, "to-do-list.empty-list");
            return;
        }

        LanguageManager languageManager = getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();

        Component titleFormat = languageManager.getMessage(sender, "to-do-list.title-format");
        List<Component> messageList = new ArrayList<>();
        messageList.add(titleFormat);

        int taskListSize = taskList.size();
        Component taskFormat = languageManager.getMessage(sender, "to-do-list.task-format");
        for (int index = 0; index < taskListSize; index++) {
            String task = taskList.get(index);
            Component fixTask = fixTask(task, miniMessage);

            TextReplacementConfig numberConfig = TextReplacementConfig.builder().matchLiteral("{number}")
                    .replacement(Component.text(index + 1)).build();
            TextReplacementConfig taskConfig =TextReplacementConfig.builder().matchLiteral("{task}")
                    .replacement(fixTask).build();

            Component finalTask = taskFormat.replaceText(numberConfig).replaceText(taskConfig);
            messageList.add(finalTask);
        }

        for (Component message : messageList) {
            languageManager.sendMessage(sender, message);
        }
    }

    private @NotNull Component fixTask(@NotNull String original, @NotNull MiniMessage miniMessage) {
        if (original.contains("&") || original.contains("§")) {
            String legacy = MessageUtility.color(original);
            return ComponentHelper.toComponent(legacy);
        }

        return miniMessage.deserialize(original);
    }
}
