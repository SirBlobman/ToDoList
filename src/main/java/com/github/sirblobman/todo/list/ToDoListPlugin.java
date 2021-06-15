package com.github.sirblobman.todo.list;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.todo.list.command.CommandToDoList;

public final class ToDoListPlugin extends ConfigurablePlugin {
    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.saveDefault("global.yml");

        LanguageManager languageManager = getLanguageManager();
        languageManager.saveDefaultLanguages();
    }

    @Override
    public void onEnable() {
        LanguageManager languageManager = getLanguageManager();
        languageManager.reloadLanguages();

        new CommandToDoList(this).register();
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }
}
