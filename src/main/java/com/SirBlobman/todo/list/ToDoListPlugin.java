package com.SirBlobman.todo.list;

import org.bukkit.plugin.java.JavaPlugin;

import com.SirBlobman.api.configuration.ConfigurationManager;
import com.SirBlobman.api.configuration.PlayerDataManager;
import com.SirBlobman.api.language.LanguageManager;
import com.SirBlobman.todo.list.command.CommandToDoList;

public final class ToDoListPlugin extends JavaPlugin {
    private final ConfigurationManager configurationManager;
    private final LanguageManager languageManager;
    private final PlayerDataManager playerDataManager;
    public ToDoListPlugin() {
        this.configurationManager = new ConfigurationManager(this);
        this.languageManager = new LanguageManager(this, this.configurationManager);
        this.playerDataManager = new PlayerDataManager(this);
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.saveDefault("global.yml");

        configurationManager.saveDefault("language.yml");
        configurationManager.saveDefault("language/en_us.lang.yml");
    }

    @Override
    public void onEnable() {
        new CommandToDoList(this).register();
    }

    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }
}