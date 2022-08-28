package com.github.sirblobman.todo.list.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.adventure.adventure.text.Component;
import com.github.sirblobman.api.adventure.adventure.text.minimessage.MiniMessage;
import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.api.language.ComponentHelper;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.todo.list.ToDoListPlugin;

import org.jetbrains.annotations.NotNull;

public final class SubCommandList extends Command {
    private final ToDoListPlugin plugin;

    public SubCommandList(ToDoListPlugin plugin) {
        super(plugin, "list");
        this.plugin = plugin;
    }

    @NotNull
    @Override
    protected LanguageManager getLanguageManager() {
        return this.plugin.getLanguageManager();
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1) {
            List<String> valueList = Arrays.asList("global", "self");
            return getMatching(args[0], valueList);
        }

        return Collections.emptyList();
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            return false;
        }

        String sub = args[0].toLowerCase(Locale.US);
        if(sub.equals("global")) {
            if(!checkPermission(sender, getGlobalViewPermission(), true)) {
                return true;
            }

            List<String> globalToDoList = getGlobalToDoList();
            sendToDoList(sender, globalToDoList);
            return true;
        }

        if(sub.equals("self")) {
            if(!(sender instanceof Player)) {
                sendMessage(sender, "error.not-player", null);
                return true;
            }

            Player player = (Player) sender;
            List<String> selfToDoList = getSelfToDoList(player);
            sendToDoList(player, selfToDoList);
            return true;
        }

        return false;
    }

    private YamlConfiguration getConfiguration() {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        return configurationManager.get("config.yml");
    }

    private YamlConfiguration getGlobalConfiguration() {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        return configurationManager.get("global.yml");
    }

    private String getGlobalViewPermission() {
        YamlConfiguration config = getConfiguration();
        return config.getString("global-list.view-permission");
    }

    private List<String> getGlobalToDoList() {
        YamlConfiguration config = getGlobalConfiguration();
        return config.getStringList("to-do-list");
    }

    private List<String> getSelfToDoList(Player player) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration data = playerDataManager.get(player);
        return data.getStringList("to-do-list");
    }

    private void sendToDoList(CommandSender sender, List<String> taskList) {
        if(taskList.isEmpty()) {
            sendMessage(sender, "to-do-list.empty-list", null);
            return;
        }

        LanguageManager languageManager = getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();

        String titleFormat = languageManager.getMessageString(sender, "to-do-list.title-format", null);
        List<String> messageList = new ArrayList<>();
        messageList.add(titleFormat);

        int taskListSize = taskList.size();
        String taskFormat = languageManager.getMessageString(sender, "to-do-list.task-format", null);
        for(int index = 0; index < taskListSize; index++) {
            String numberString = Integer.toString(index + 1);
            String task = taskList.get(index);
            String fixTask = fixTask(task, miniMessage);

            String taskFormatted = taskFormat.replace("{number}", numberString)
                    .replace("{task}", fixTask);
            messageList.add(taskFormatted);
        }

        for (String messageString : messageList) {
            Component message = miniMessage.deserialize(messageString);
            languageManager.sendMessage(sender, message);
        }
    }

    private String fixTask(String original, MiniMessage miniMessage) {
        if(!original.contains("&") && !original.contains("\u00A7")) {
            return original;
        }

        String legacy = MessageUtility.color(original);
        Component component = ComponentHelper.toComponent(legacy);
        return miniMessage.serialize(component);
    }
}
