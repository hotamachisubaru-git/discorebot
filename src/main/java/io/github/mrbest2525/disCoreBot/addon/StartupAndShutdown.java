package io.github.mrbest2525.disCoreBot.addon;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import io.github.mrbest2525.disCoreBot.DisCoreBot;
import io.github.mrbest2525.disCoreBot.api.DisCoreBotApi;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

public class StartupAndShutdown implements Listener {
    private final DisCoreBot core;
    private final File configFile;
    private YamlConfiguration config;
    private boolean enabled = true;
    
    private final NamespacedKey STARTUP_AND_SHUTDOWN;
    
    public StartupAndShutdown(DisCoreBot core) {
        this.core = core;
        STARTUP_AND_SHUTDOWN = NamespacedKey.fromString("startup_and_shutdown", core);
        
        this.configFile = new File(core.getAddonDataDir(), "startup_and_shutdown/startup_and_shutdown.yml");
        loadConfig();
        
        // アドオンが有効設定の場合のみ機能させる
        enabled = config.getBoolean("enabled");
        if (!enabled) return;
        core.getServer().getPluginManager().registerEvents(this, core);
        
    }
    
    private void loadConfig() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            // デフォルト設定の生成が必要ならここで行う
            config = new YamlConfiguration();
            config.set("enabled", true);
            config.set("channel-id", "0123456789");
            try {
                config.save(configFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe(e.toString());
            }
        } else {config = YamlConfiguration.loadConfiguration(configFile);
        
        }
    }
    
    @EventHandler
    public void onRegister(DisCoreBotRegisterEvent event) {
        if (!enabled) return;
        event.registerM2D(STARTUP_AND_SHUTDOWN, config.getString("channel-id"));
    }
    
    public void onEnable() {
        if (!enabled) return;
        DisCoreBotApi.getInstance().sendMessage(STARTUP_AND_SHUTDOWN, config.getString("channel-id"), new WebhookMessageBuilder().setContent("サーバーが起動しました。").build());
    }
    
    public void onDisable() {
        if (!enabled) return;
        DisCoreBotApi.getInstance().sendMessage(STARTUP_AND_SHUTDOWN, config.getString("channel-id"), new WebhookMessageBuilder().setContent("サーバーを終了しました。").build());
    }
}
