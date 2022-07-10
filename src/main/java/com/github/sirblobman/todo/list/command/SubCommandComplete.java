package com.github.sirblobman.todo.list.command;

import java.math.BigInteger;
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
import com.github.sirblobman.api.language.Replacer;
import com.github.sirblobman.api.language.SimpleReplacer;
import com.github.sirblobman.todo.list.ToDoListPlugin;

import org.jetbrains.annotations.NotNull;

public final class SubCommandComplete extends Command {
    private final ToDoListPlugin plugin;

    public SubCommandComplete(ToDoListPlugin plugin) {
        super(plugin, "add");
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

        if(args.length == 2) {
            return Collections.singletonList("1");
        }

        return Collections.emptyList();
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            return false;
        }

        String indexString = args[1];
        BigInteger indexBig = parseInteger(sender, indexString);
        if(indexBig == null) {
            return true;
        }

        int index = (indexBig.intValue() - 1);
        if(index < 0) {
            Replacer replacer = new SimpleReplacer("{value}", args[1]);
            sendMessage(sender, "error.number-too-small", replacer, true);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.US);
        if(sub.equals("global")) {
            if(!checkPermission(sender, getGlobalEditPermission(), true)) {
                return true;
            }

            List<String> globalToDoList = getGlobalToDoList();
            int globalToDoListSize = globalToDoList.size();
            if(index >= globalToDoListSize) {
                Replacer replacer = new SimpleReplacer("{value}", args[1]);
                sendMessage(sender, "error.number-too-big", replacer, true);
                return true;
            }

            String completedTask = globalToDoList.remove(index);
            setGlobalToDoList(globalToDoList);

            Replacer replacer = new SimpleReplacer("{task}", completedTask);
            sendMessage(sender, "to-do-list.complete-task", replacer, true);
            return true;
        }

        if(sub.equals("self")) {
            if(!(sender instanceof Player)) {
                sendMessage(sender, "error.not-player", null, true);
                return true;
            }

            Player player = (Player) sender;
            List<String> selfToDoList = getSelfToDoList(player);
            int selfToDoListSize = selfToDoList.size();
            if(index >= selfToDoListSize) {
                Replacer replacer = new SimpleReplacer("{value}", args[1]);
                sendMessage(sender, "error.number-too-big", replacer, true);
                return true;
            }

            String completedTask = selfToDoList.remove(index);
            setSelfToDoList(player, selfToDoList);

            Replacer replacer = new SimpleReplacer("{task}", completedTask);
            sendMessage(sender, "to-do-list.complete-task", replacer, true);
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

    private String getGlobalEditPermission() {
        YamlConfiguration config = getConfiguration();
        return config.getString("global-list.edit-permission");
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

    private void setGlobalToDoList(List<String> taskList) {
        YamlConfiguration config = getGlobalConfiguration();
        config.set("to-do-list", taskList);

        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        configurationManager.save("global.yml");
    }

    private void setSelfToDoList(Player player, List<String> taskList) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration data = playerDataManager.get(player);
        data.set("to-do-list", taskList);
        playerDataManager.save(player);
    }
}
