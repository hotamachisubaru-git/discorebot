package io.github.mrbest2525.disCoreBot.api;

import club.minnced.discord.webhook.send.WebhookMessage;
import io.github.mrbest2525.disCoreBot.DisCoreBot;
import org.bukkit.NamespacedKey;

public class DisCoreBotApi {
    
    private final DisCoreBot core;
    
    private static DisCoreBotApi instance;
    
    public DisCoreBotApi(DisCoreBot core) {
        this.core = core;
        instance = this;
    }
    
    public static DisCoreBotApi getInstance() {
        return instance;
    }
    
    public void sendMessage(NamespacedKey id, String channel, WebhookMessage message) {
        core.getMessageExecutor().queueMessage(id, channel, message);
    }
}
