package com.SirBlobman.todo.list.command;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.SirBlobman.api.command.Command;
import com.SirBlobman.api.configuration.ConfigurationManager;
import com.SirBlobman.api.configuration.PlayerDataManager;
import com.SirBlobman.api.language.LanguageManager;
import com.SirBlobman.api.language.Replacer;
import com.SirBlobman.api.utility.MessageUtility;
import com.SirBlobman.todo.list.ToDoListPlugin;

public class CommandToDoList extends Command {
    private final ToDoListPlugin plugin;
    public CommandToDoList(ToDoListPlugin plugin) {
        super(plugin, "to-do-list");
        this.plugin = plugin;
    }

    @Override
    public LanguageManager getLanguageManager() {
        return this.plugin.getLanguageManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1) {
            List<String> valueList = Arrays.asList("help", "reload", "list", "add", "complete");
            return getMatching(valueList, args[0]);
        }
        
        if(args.length == 2) {
            String sub = args[0].toLowerCase();
            List<String> subList = Arrays.asList("list", "add", "complete");
            if(subList.contains(sub)) {
                List<String> valueList = Arrays.asList("global", "self");
                return getMatching(valueList, args[1]);
            }
        }
        
        if(args.length == 3) {
            String sub = args[0].toLowerCase();
            if(sub.equals("add")) return Collections.singletonList("Type your task here.");
            if(sub.equals("complete")) {
                Set<String> valueSet = IntStream.rangeClosed(1, 10).sorted().boxed().map(Object::toString).collect(Collectors.toSet());
                return getMatching(valueSet, args[2]);
            }
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            LanguageManager languageManager = getLanguageManager();
            languageManager.sendMessage(sender, "to-do-list.command-help", null,true);
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] newArgs = (args.length < 2 ? new String[0] : Arrays.copyOfRange(args, 1, args.length));
        switch(sub) {
            case "reload": return reloadCommand(sender);
            case "list": return listCommand(sender, newArgs);
            case "add": return addCommand(sender, newArgs);
            case "complete": return completeCommand(sender, newArgs);
            case "help": return execute(sender, new String[0]);
            default: break;
        }

        return false;
    }

    private boolean reloadCommand(CommandSender sender) {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        configurationManager.reload("config.yml");
        configurationManager.reload("global.yml");

        configurationManager.reload("language.yml");
        configurationManager.reload("language/en_us.lang.yml");

        LanguageManager languageManager = getLanguageManager();
        languageManager.sendMessage(sender, "to-do-list.reload-success", null,true);
        return true;
    }
    
    private boolean listCommand(CommandSender sender, String[] args) {
        if(args.length < 1) return false;
        
        String sub = args[0].toLowerCase();
        if(sub.equals("global")) {
            if(!checkPermission(sender, getGlobalViewPermission(), true)) return true;
            List<String> globalToDoList = getGlobalToDoList();
            sendToDoList(sender, globalToDoList);
            return true;
        }
        
        if(sub.equals("self")) {
            if(!(sender instanceof Player)) {
                LanguageManager languageManager = getLanguageManager();
                languageManager.sendMessage(sender, "error.not-player", null, true);
                return true;
            }
            Player player = (Player) sender;
            
            List<String> selfToDoList = getSelfToDoList(player);
            sendToDoList(player, selfToDoList);
            return true;
        }
        
        return false;
    }
    
    private boolean addCommand(CommandSender sender, String[] args) {
        if(args.length < 2) return false;
        String sub = args[0].toLowerCase();
        String newItem = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if(sub.equals("global")) {
            if(!checkPermission(sender, getGlobalEditPermission(), true)) return true;
            List<String> globalToDoList = getGlobalToDoList();
            globalToDoList.add(newItem);
            setGlobalToDoList(globalToDoList);

            Replacer replacer = message -> message.replace("{task}", newItem);
            LanguageManager languageManager = getLanguageManager();
            languageManager.sendMessage(sender, "to-do-list.add-task", replacer, true);
            return true;
        }

        if(sub.equals("self")) {
            if(!(sender instanceof Player)) {
                LanguageManager languageManager = getLanguageManager();
                languageManager.sendMessage(sender, "error.not-player", null, true);
                return true;
            }
            Player player = (Player) sender;

            List<String> selfToDoList = getSelfToDoList(player);
            selfToDoList.add(newItem);
            setSelfToDoList(player, selfToDoList);

            Replacer replacer = message -> message.replace("{task}", newItem);
            LanguageManager languageManager = getLanguageManager();
            languageManager.sendMessage(sender, "to-do-list.add-task", replacer, true);
            return true;
        }

        return false;
    }
    
    private boolean completeCommand(CommandSender sender, String[] args) {
        if(args.length < 2) return false;

        String indexString = args[1];
        BigInteger indexBig = parseInteger(sender, indexString);
        if(indexBig == null) return true;
        int index = (indexBig.intValue() - 1);
        if(index < 0) {
            Replacer replacer = message -> message.replace("{value}", indexBig.toString());
            LanguageManager languageManager = getLanguageManager();
            languageManager.sendMessage(sender, "error.number-too-small", replacer, true);
            return true;
        }

        String sub = args[0].toLowerCase();
        if(sub.equals("global")) {
            if(!checkPermission(sender, getGlobalEditPermission(), true)) return true;
            List<String> globalToDoList = getGlobalToDoList();
            int globalToDoListSize = globalToDoList.size();
            if(index >= globalToDoListSize) {
                Replacer replacer = message -> message.replace("{value}", indexBig.toString());
                LanguageManager languageManager = getLanguageManager();
                languageManager.sendMessage(sender, "error.number-too-big", replacer, true);
                return true;
            }

            String completedTask = globalToDoList.remove(index);
            setGlobalToDoList(globalToDoList);

            Replacer replacer = message -> message.replace("{task}", completedTask);
            LanguageManager languageManager = getLanguageManager();
            languageManager.sendMessage(sender, "to-do-list.complete-task", replacer, true);
            return true;
        }

        if(sub.equals("self")) {
            if(!(sender instanceof Player)) {
                LanguageManager languageManager = getLanguageManager();
                languageManager.sendMessage(sender, "error.not-player", null, true);
                return true;
            }
            Player player = (Player) sender;

            List<String> selfToDoList = getSelfToDoList(player);
            int selfToDoListSize = selfToDoList.size();
            if(index >= selfToDoListSize) {
                Replacer replacer = message -> message.replace("{value}", indexBig.toString());
                LanguageManager languageManager = getLanguageManager();
                languageManager.sendMessage(sender, "error.number-too-big", replacer, true);
                return true;
            }

            String completedTask = selfToDoList.remove(index);
            setSelfToDoList(player, selfToDoList);

            Replacer replacer = message -> message.replace("{task}", completedTask);
            LanguageManager languageManager = getLanguageManager();
            languageManager.sendMessage(sender, "to-do-list.complete-task", replacer, true);
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
    
    private String getGlobalEditPermission() {
        YamlConfiguration config = getConfiguration();
        return config.getString("global-list.edit-permission");
    }
    
    private List<String> getGlobalToDoList() {
        YamlConfiguration config = getGlobalConfiguration();
        return config.getStringList("to-do-list");
    }
    
    private void setGlobalToDoList(List<String> taskList) {
        YamlConfiguration config = getGlobalConfiguration();
        config.set("to-do-list", taskList);

        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        configurationManager.save("global.yml");
    }
    
    private List<String> getSelfToDoList(Player player) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration data = playerDataManager.get(player);
        return data.getStringList("to-do-list");
    }
    
    private void setSelfToDoList(Player player, List<String> taskList) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration data = playerDataManager.get(player);
        data.set("to-do-list", taskList);
        playerDataManager.save(player);
    }
    
    private void sendToDoList(CommandSender sender, List<String> taskList) {
        LanguageManager languageManager = getLanguageManager();
        if(taskList.isEmpty()) {
            languageManager.sendMessage(sender, "to-do-list.empty-list", null, true);
            return;
        }
        
        String titleFormat = languageManager.getMessageColored(sender, "to-do-list.title-format");
        List<String> messageList = new ArrayList<>();
        messageList.add(titleFormat);
    
        int taskListSize = taskList.size();
        String taskFormat = languageManager.getMessageColored(sender,"to-do-list.task-format");
        for(int index = 0; index < taskListSize; index++) {
            String numberString = Integer.toString(index + 1);
            String task = taskList.get(index);
            String taskColored = MessageUtility.color(task);
            
            String taskFormatted = taskFormat.replace("{number}", numberString).replace("{task}", taskColored);
            messageList.add(taskFormatted);
        }
        
        String[] messageArray = messageList.toArray(new String[0]);
        sender.sendMessage(messageArray);
    }
}