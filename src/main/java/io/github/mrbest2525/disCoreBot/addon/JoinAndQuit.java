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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;

public class JoinAndQuit implements Listener {
    private final DisCoreBot core;
    private final File configFile;
    private YamlConfiguration config;
    
    private final NamespacedKey JOIN_AND_QUIT;
    
    public JoinAndQuit(DisCoreBot core) {
        this.core = core;
        JOIN_AND_QUIT = NamespacedKey.fromString("join_and_quit", core);
        
        this.configFile = new File(core.getAddonDataDir(), "join_and_quit/join_and_quit.yml");
        loadConfig();
        
        // アドオンが有効設定の場合のみ機能させる
        if (!config.getBoolean("enabled")) return;
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
        event.registerM2D(JOIN_AND_QUIT, config.getString("channel-id"));
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DisCoreBotApi.getInstance().sendMessage(JOIN_AND_QUIT, config.getString("channel-id"), new WebhookMessageBuilder().setAvatarUrl(String.format("https://mc-heads.net/avatar/%s", event.getPlayer().getUniqueId())).setUsername(event.getPlayer().getDisplayName()).setContent("<" + event.getPlayer().getDisplayName() + "> がゲームに参加しました").build());
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        DisCoreBotApi.getInstance().sendMessage(JOIN_AND_QUIT, config.getString("channel-id"), new WebhookMessageBuilder().setAvatarUrl(String.format("https://mc-heads.net/avatar/%s", event.getPlayer().getUniqueId())).setUsername(event.getPlayer().getDisplayName()).setContent("<" + event.getPlayer().getDisplayName() + "> がゲームから退出しました").build());
    }
}
