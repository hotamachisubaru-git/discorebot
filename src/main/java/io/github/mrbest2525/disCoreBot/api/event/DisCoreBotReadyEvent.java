package io.github.mrbest2525.disCoreBot.api.event;

import io.github.mrbest2525.disCoreBot.DisCoreBot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DisCoreBotReadyEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final DisCoreBot core;
    
    public DisCoreBotReadyEvent(DisCoreBot core) {
        this.core = core;
    }

    public DisCoreBot getCore() {
        return core;
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
