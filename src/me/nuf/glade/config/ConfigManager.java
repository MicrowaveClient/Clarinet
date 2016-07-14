package me.nuf.glade.config;

import me.nuf.api.management.ListManager;

import java.util.ArrayList;

/**
 * Created by nuf on 3/21/2016.
 */
public final class ConfigManager extends ListManager<Config> {
    public ConfigManager() {
        elements = new ArrayList<>();
    }
}
