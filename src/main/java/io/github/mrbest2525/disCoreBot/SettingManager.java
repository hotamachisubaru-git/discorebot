package io.github.mrbest2525.disCoreBot;

public class SettingManager {
    public final String BOT_TOKEN;
    public SettingManager(DisCoreBot plugin) {
        plugin.saveDefaultConfig();
        
        this.BOT_TOKEN = plugin.getConfig().getString("bot-token", "");
    }
}
