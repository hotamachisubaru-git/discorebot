package io.github.mrbest2525.disCoreBot.api.event;

import io.github.mrbest2525.disCoreBot.DisCoreBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DisCoreBotRegisterEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final DisCoreBot core;
    
    public DisCoreBotRegisterEvent(DisCoreBot core) {
        this.core = core;
    }
    
    /**
     * 使用するチャンネルとIDを登録します。登録されていないIDは使用できません。
     * @param key 識別キー
     * @param channelId 送信するチャンネルID
     */
    public void registerM2D(NamespacedKey key, String channelId) {
        core.getWebhookManager().register(key, channelId, Thread.currentThread().getStackTrace()[2]);
    }
    
    
    /**
     * アドオンからスラッシュコマンドを登録します。
     * 登録したコマンドは非同期スレッドで呼び出されます。
     */
    public void registerSlashCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> executor) {
        core.getJDAManager().addCommand(data, executor);
    }
    
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
