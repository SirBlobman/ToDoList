package com.github.sirblobman.todo.list.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.todo.list.ToDoListPlugin;

public final class SubCommandHelp extends Command {
    public SubCommandHelp(ToDoListPlugin plugin) {
        super(plugin, "help");
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        sendMessage(sender, "to-do-list.command-help", null);
        return true;
    }
}
