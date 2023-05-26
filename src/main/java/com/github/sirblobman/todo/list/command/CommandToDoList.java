package com.github.sirblobman.todo.list.command;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.CommandSender;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.todo.list.ToDoListPlugin;

public final class CommandToDoList extends Command {
    public CommandToDoList(@NotNull ToDoListPlugin plugin) {
        super(plugin, "to-do-list");
        setPermissionName("to-do-list.command.to-do-list");
        addSubCommand(new SubCommandAdd(plugin));
        addSubCommand(new SubCommandComplete(plugin));
        addSubCommand(new SubCommandHelp(plugin));
        addSubCommand(new SubCommandList(plugin));
        addSubCommand(new SubCommandReload(plugin));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, String @NotNull [] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String @NotNull [] args) {
        sendMessage(sender, "to-do-list.command-help");
        return true;
    }
}
