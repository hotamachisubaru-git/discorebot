package io.github.mrbest2525.disCoreBot.api.event;

import io.github.mrbest2525.disCoreBot.DisCoreBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DisCoreBotDiscordMsgEvent  extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final DisCoreBot core;
    private final MessageReceivedEvent jdaEvent;
    
    public DisCoreBotDiscordMsgEvent(DisCoreBot core, MessageReceivedEvent jdaEvent) {
        // メインスレッドで呼び出す場合は false, 非同期なら true
        super(false);
        this.core = core;
        this.jdaEvent = jdaEvent;
    }

    /**
     * イベントを発火したDisCoreBotインスタンスを取得します。
     *
     * @return DisCoreBotインスタンス
     */
    public DisCoreBot getCore() {
        return core;
    }
    
    /**
     * JDAの生イベントを取得します。
     */
    public MessageReceivedEvent getEvent() {
        return jdaEvent;
    }
    
    // --- ショートカットメソッド ---
    
    /**
     * メッセージを取得します。
     * @return メッセージ
     */
    public String getMessage() {
        return jdaEvent.getMessage().getContentRaw();
    }
    
    /**
     * 発火されたチャンネルIDを取得します。
     * @return チャンネルID
     */
    public String getChannelID() {
        return jdaEvent.getChannel().getId();
    }
    
    /**
     * 発火したDiscordユーザー名を取得します。
     * @return ユーザー名
     */
    public String getAuthorName() {
        return jdaEvent.getAuthor().getName();
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
