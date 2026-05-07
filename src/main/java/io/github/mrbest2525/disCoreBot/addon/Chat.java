package io.github.mrbest2525.disCoreBot.addon;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import io.github.mrbest2525.disCoreBot.DisCoreBot;
import io.github.mrbest2525.disCoreBot.api.DisCoreBotApi;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotDiscordMsgEvent;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.IOException;

public class Chat implements Listener {
    
    private final DisCoreBot core;
    private final File configFile;
    private YamlConfiguration config;
    private boolean enabled = true;
    
    private final NamespacedKey CHAT;
    
    public Chat (DisCoreBot core) {
        this.core = core;
        CHAT = NamespacedKey.fromString("chat", core);
        
        this.configFile = new File(core.getAddonDataDir(), "chat/chat.yml");
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
        event.registerM2D(CHAT, config.getString("channel-id"));
    }
    
    @EventHandler
    public void onDiscordChat(DisCoreBotDiscordMsgEvent event) {
        if (!enabled) return;
        if (!event.getChannelID().equals(config.getString("channel-id"))) return;
        Member member = event.getEvent().getMember();
        String name = (member != null && member.getNickname() != null) ? member.getNickname() : event.getEvent().getAuthor().getName();
        Bukkit.broadcastMessage(String.format("<%s> %s", name, event.getMessage()));
    }
    
    @EventHandler
    public void onMinecraftChat(AsyncPlayerChatEvent event) {
        if (!enabled) return;
        DisCoreBotApi.getInstance().sendMessage(CHAT, config.getString("channel-id"), new WebhookMessageBuilder().setAvatarUrl(String.format("https://mc-heads.net/avatar/%s", event.getPlayer().getUniqueId())).setUsername(event.getPlayer().getDisplayName()).setContent(event.getMessage()).build());
    }
}
