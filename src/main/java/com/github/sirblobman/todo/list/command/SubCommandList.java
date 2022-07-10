package com.github.sirblobman.todo.list.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.configuration.PlayerDataManager;
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
                sendMessage(sender, "error.not-player", null, true);
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
        LanguageManager languageManager = getLanguageManager();
        if(taskList.isEmpty()) {
            sendMessage(sender, "to-do-list.empty-list", null, true);
            return;
        }

        String titleFormat = languageManager.getMessage(sender, "to-do-list.title-format", null,
                true);
        List<String> messageList = new ArrayList<>();
        messageList.add(titleFormat);

        int taskListSize = taskList.size();
        String taskFormat = languageManager.getMessage(sender, "to-do-list.task-format", null,
                true);
        for(int index = 0; index < taskListSize; index++) {
            String numberString = Integer.toString(index + 1);
            String task = taskList.get(index);
            String taskColored = MessageUtility.color(task);

            String taskFormatted = taskFormat.replace("{number}", numberString)
                    .replace("{task}", taskColored);
            messageList.add(taskFormatted);
        }

        String[] messageArray = messageList.toArray(new String[0]);
        sender.sendMessage(messageArray);
    }
}
