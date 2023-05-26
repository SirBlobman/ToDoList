package com.github.sirblobman.todo.list.command;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.CommandSender;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.todo.list.ToDoListPlugin;

public final class SubCommandHelp extends Command {
    public SubCommandHelp(@NotNull ToDoListPlugin plugin) {
        super(plugin, "help");
        setPermissionName("to-do-list.command.to-do-list.help");
    }

    @Override
    protected @NotNull List<String> onTabComplete(@NotNull CommandSender sender, String @NotNull [] args) {
        return Collections.emptyList();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, String @NotNull [] args) {
        sendMessage(sender, "to-do-list.command-help");
        return true;
    }
}
