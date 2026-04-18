package io.github.mrbest2525.disCoreBot.jda;

import io.github.mrbest2525.disCoreBot.DisCoreBot;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotDiscordMsgEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class DiscordMessageListener extends ListenerAdapter {
    
    private final DisCoreBot core;
    
    public DiscordMessageListener(DisCoreBot core) {
        this.core = core;
    }
    
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        
        if (event.getAuthor().isBot()) return;
        
        // Bukkitのメインスレッドでイベントを発火させる
        Bukkit.getScheduler().runTask(core, () -> {
            DisCoreBotDiscordMsgEvent bukkitEvent = new DisCoreBotDiscordMsgEvent(
                    core, event
            );
            Bukkit.getPluginManager().callEvent(bukkitEvent);
        });
    }
}
