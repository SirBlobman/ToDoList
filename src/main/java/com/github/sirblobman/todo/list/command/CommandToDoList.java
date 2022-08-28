package com.github.sirblobman.todo.list.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.todo.list.ToDoListPlugin;

public final class CommandToDoList extends Command {
    public CommandToDoList(ToDoListPlugin plugin) {
        super(plugin, "to-do-list");
        setPermissionName("to-do-list.command.to-do-list");
        addSubCommand(new SubCommandAdd(plugin));
        addSubCommand(new SubCommandComplete(plugin));
        addSubCommand(new SubCommandHelp(plugin));
        addSubCommand(new SubCommandList(plugin));
        addSubCommand(new SubCommandReload(plugin));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sendMessage(sender, "to-do-list.command-help", null);
        return true;
    }
}
