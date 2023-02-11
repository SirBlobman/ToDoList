package com.github.sirblobman.todo.list.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.todo.list.ToDoListPlugin;

public final class SubCommandReload extends Command {
    public SubCommandReload(ToDoListPlugin plugin) {
        super(plugin, "reload");
        setPermissionName("to-do-list.command.to-do-list.reload");
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        JavaPlugin plugin = getPlugin();
        plugin.reloadConfig();

        sendMessage(sender, "to-do-list.reload-success");
        return true;
    }
}
