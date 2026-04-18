package io.github.mrbest2525.disCoreBot.jda;

import io.github.mrbest2525.disCoreBot.DisCoreBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class JDAManager extends ListenerAdapter {
    
    private final DisCoreBot core;
    
    private JDA jda;
    
    private final Map<String, DiscordCommandHolder> registeredCommands = new HashMap<>();
    
    public JDAManager(DisCoreBot core) {
        this.core = core;
    }
    
    public void boot(String token) {
        Logger logger = Bukkit.getLogger();
        this.jda = JDABuilder.createDefault(token)
                .addEventListeners(new DiscordMessageListener(core)) // リスナーを登録
                .addEventListeners(this)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // メッセージ送信に必要
                .build();
        try {
            logger.info("Discord Botへのログインを待機中...");
            jda.awaitReady();
            logger.info("Discord Botが正常に起動しました！");
        } catch (InterruptedException e) {
            logger.warning("DiscordBot起動中に割り込みが発生しました。");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.severe("DiscordBot起動時にエラーが発生しました。 error:\n" + e.getMessage() + "\n" + e.fillInStackTrace());
        }
    }
    
    public void shutdown() {
        if (jda != null) {
            Logger logger = Bukkit.getLogger();
            logger.info("DiscordBotを終了します。");
            // 1. まずシャットダウンを命令する（これを忘れると await は永遠に終わりません）
            jda.shutdownNow();
            
            try {
                // 2. 最大5秒間だけ待機する（awaitShutdown() は無限に待つ可能性があるため非推奨）
                if (!jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                    logger.warning("DiscordBotの停止に時間がかかりすぎたため、強制終了しました。");
                }
            } catch (InterruptedException e) {
                // 3. 待機中にサーバーから中断された場合
                Thread.currentThread().interrupt();
                logger.severe("DiscordBotの停止中に割り込みが発生しました。");
            }
            logger.info("DiscordBotを終了しました。");
        }
    }
    
    public String getOrCreateWebhookSync(String channelId) {
        try {
            long id = Long.parseLong(channelId);
            GuildChannel channel = jda.getGuildChannelById(id);
            
            // スレッドやボイスチャンネル等、Webhookが使えるコンテナか判定
            if (channel instanceof IWebhookContainer container) {
                // .complete() でDiscordからの返答を同期的に待つ
                List<Webhook> webhooks = container.retrieveWebhooks().complete();
                
                return webhooks.stream()
                        .filter(w -> w.getName().equalsIgnoreCase("DisCoreBot"))
                        .findFirst()
                        .map(Webhook::getUrl)
                        .orElseGet(() -> container.createWebhook("DisCoreBot").complete().getUrl());
            } else {
                Bukkit.getLogger().severe("指定されたチャンネルではWebhookを使用できません。channel id[" + id + "]");
            }
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("不正なチャンネルID形式です。channel id[" + channelId + "]");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Webhookの同期取得に失敗:\n" + e.getMessage());
        }
        return null;
    }
    
    public void addCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> executor) {
        registeredCommands.put(data.getName(), new DiscordCommandHolder(data, executor));
    }
    
    public void registerCommands() {
        core.getJDAManager().getJDA().updateCommands()
                .addCommands(getCommandDatas())
                .queue(success -> Bukkit.getLogger().info(getCommandDatas().size() + " 個のスラッシュコマンドをDiscordに同期しました。"), throwable -> Bukkit.getLogger().severe("コマンド同期エラー: " + throwable.getMessage()));
    }
    
    // JDA起動時に一括送信するためのリスト取得用
    public Collection<SlashCommandData> getCommandDatas() {
        return registeredCommands.values().stream()
                .map(DiscordCommandHolder::data)
                .toList();
    }
    
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        DiscordCommandHolder holder = registeredCommands.get(event.getName());
        if (holder != null) {
            // メインスレッドに同期して実行
            Bukkit.getScheduler().runTask(core, () -> holder.executor().accept(event));
        }
    }
    
    public JDA getJDA() {
        return jda;
    }
    
    public record DiscordCommandHolder(
            SlashCommandData data,
            Consumer<SlashCommandInteractionEvent> executor
    ) {}
}
