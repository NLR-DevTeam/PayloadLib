package top.nlrdev.payloadlib.internal;

import org.bukkit.plugin.java.JavaPlugin;

public final class PluginInitializer extends JavaPlugin {
    private static PluginInitializer instance;

    public static PluginInitializer getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
    }
}