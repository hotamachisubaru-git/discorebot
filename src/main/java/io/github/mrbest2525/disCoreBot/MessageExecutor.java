package io.github.mrbest2525.disCoreBot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageExecutor {
    
    private final DisCoreBot core;
    
    private final BlockingQueue<DiscordMessage> queue = new LinkedBlockingQueue<>();
    private boolean isProcessing = false;
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private Thread workerThread;
    
    public MessageExecutor(DisCoreBot core) {
        this.core = core;
    }
    
    public void queueMessage(NamespacedKey key, String channelID, WebhookMessage content) {
        if (!queue.offer(new DiscordMessage(key, channelID, null, content))) {
            Bukkit.getLogger().warning("キューに追加できなかったためメッセージを破棄しました。:\n" +
                    "NameSpacedKey[" + key + "]\n" +
                    "channel id[" + channelID + "]\n" +
                    "content[" + content + "]");
        }
    }
    
    public void queueMessage(NamespacedKey key, String channelID, String threadID, WebhookMessage content) {
        if (!queue.offer(new DiscordMessage(key, channelID, threadID, content))) {
            Bukkit.getLogger().warning("キューに追加できなかったためメッセージを破棄しました。:\n" +
                    "NameSpacedKey[" + key + "]\n" +
                    "channel id[" + channelID + "]\n" +
                    "thread id[" + threadID + "]\n" +
                    "content[" + content + "]");
        }
    }
    
    public void start() {
        if (isProcessing) return;
        isProcessing = true;
        
        // Bukkitの非同期タスクとしてスレッドを立ち上げる
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
            this.workerThread = Thread.currentThread();
            try {
                Bukkit.getLogger().info("Discordメッセージ送信スレッドを開始しました。");
                
                while (isProcessing) {
                    try {
                        // キューからメッセージを1つ取り出す（空ならここでスレッドが待機する）
                        DiscordMessage message = queue.take();
                        
                        // 送信
                        if (core.getWebhookManager().isActiveKey(message.key())) {
                            message.sendWebHook(core);
                        }
                        
                        
                    } catch (InterruptedException e) {
                        // スレッド終了時の割り込み処理
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        Bukkit.getLogger().severe("送信キュー内で予期せぬエラー: " + e.getMessage());
                    }
                }
                Bukkit.getLogger().info("Discordメッセージ送信スレッドを停止しました。");
            } finally {
                // スレッドが何らかの理由で終了する際、必ずカウントを下げる
                stopLatch.countDown();
                Bukkit.getLogger().info("Discordメッセージ送信スレッドが完全に停止しました。");
            }
        });
    }
    
    public void shutdown() {
        isProcessing = false;
        if (workerThread != null) {
            workerThread.interrupt(); // take() の待機を強制解除
        }
        try {
            if (!stopLatch.await(5, TimeUnit.SECONDS)) {
                Bukkit.getLogger().warning("送信スレッドの停止待機がタイムアウトしました。");
            } else {
                Bukkit.getLogger().info("送信スレッドの正常終了を確認しました。");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public record DiscordMessage(
            @NotNull NamespacedKey key,
            @NotNull String channelID,
            @Nullable String threadID,
            @NotNull WebhookMessage message
    ) {
        public void sendWebHook(DisCoreBot core) {
            String url = core.getWebhookManager().getWebhookURL(channelID);
            if (url == null) return;
            
            // 2. ライブラリのClientを作成（その場で作っても非常に軽量です）
            try (WebhookClient client = new WebhookClientBuilder(url).build()) {
                
                WebhookClient targetClient = (threadID != null)
                        ? client.onThread(Long.parseLong(threadID))
                        : client;
                
                // 3. 送信（内部で非同期実行される）
                targetClient.send(message).handle((message, throwable) -> {
                    if (throwable != null) {
                        // 4. 404エラー（Webhook消失）を検知
                        if (throwable.getMessage().contains("404")) {
                            Bukkit.getLogger().warning("Webhook消失検知(404): channel[" + channelID +"]\n" +
                                    "送信失敗したメッセージ [" + message + "]");
                            core.getWebhookManager().invalidate(channelID);
                        }
                    }
                    return null;
                });
            } catch (NumberFormatException e) {
                Bukkit.getLogger().severe("無効なスレッドID形式です: " + threadID);
            }
        }
    }
}
