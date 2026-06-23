package io.github.mrbest2525.disCoreBot;

import io.github.mrbest2525.disCoreBot.addon.Chat;
import io.github.mrbest2525.disCoreBot.addon.JoinAndQuit;
import io.github.mrbest2525.disCoreBot.addon.ListCommand;
import io.github.mrbest2525.disCoreBot.addon.StartupAndShutdown;
import io.github.mrbest2525.disCoreBot.api.DisCoreBotApi;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotReadyEvent;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotStopEvent;
import io.github.mrbest2525.disCoreBot.jda.JDAManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DisCoreBot extends JavaPlugin implements Listener {
    private JDAManager jda;
    private WebhookManager webhookManager;
    private MessageExecutor messageExecutor;
    
    // API
    private DisCoreBotApi api;
    
    // Build-in アドオン
    private io.github.mrbest2525.disCoreBot.addon.Chat addonChat;
    private io.github.mrbest2525.disCoreBot.addon.JoinAndQuit addonJoinAndQuit;
    private io.github.mrbest2525.disCoreBot.addon.ListCommand addonListCommand;
    private io.github.mrbest2525.disCoreBot.addon.StartupAndShutdown addonStartupAndShutdown;
    
    @Override
    public void onEnable() {
        SettingManager setting = new SettingManager(this);
        jda = new JDAManager(this);
        try {
            jda.boot(setting.BOT_TOKEN);
        } catch (Exception e) {
            getLogger().severe("DiscordBotを起動できないためプラグインを無効化します。設定を見直してください。\nそれでも解決しない場合はGitHubリポジトリのissueにて問題を報告してください。\nGitHub: https://github.com/MrBest2525/discorebot/issues");
            jda.shutdown();
            jda = null;
            
            DisCoreBotStopEvent stopEvent = new DisCoreBotStopEvent(this);
            Bukkit.getPluginManager().callEvent(stopEvent);
            
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        webhookManager = new WebhookManager(this);
        
        messageExecutor = new MessageExecutor(this);
        
        // APIの起動
        api = new DisCoreBotApi(this);
        
        // イベントリスナーの登録
        getServer().getPluginManager().registerEvents(this, this);
        
        PluginCommand disCoreBotCommand = this.getCommand("discorebot");
        if (disCoreBotCommand != null) {
            disCoreBotCommand.setExecutor(new Command(this));
            disCoreBotCommand.setTabCompleter(new Command(this));
        }
        
        // Build-in アドオンの読み込み
        addonChat = new Chat(this);
        addonJoinAndQuit = new JoinAndQuit(this);
        addonListCommand = new ListCommand(this);
        addonStartupAndShutdown = new StartupAndShutdown(this);
        addonChat.registerEvents();
        addonJoinAndQuit.registerEvents();
        addonListCommand.registerEvents();
        addonStartupAndShutdown.registerEvents();
        addonStartupAndShutdown.onEnable();
    }
    
    @Override
    public void onDisable() {
        if (addonStartupAndShutdown != null) addonStartupAndShutdown.onDisable();
        
        if (messageExecutor != null) messageExecutor.shutdown();
        
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
        
        if (webhookManager != null) {
            webhookManager.load();
            webhookManager.save();
        }
    }
    
    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        DisCoreBotRegisterEvent registerEvent = new DisCoreBotRegisterEvent(this);
        Bukkit.getPluginManager().callEvent(registerEvent);
        
        jda.registerCommands();
        
        webhookManager.load();
        webhookManager.save();
        
        messageExecutor.start();
        getLogger().info("アドオンの登録が完了しました。");
        
        DisCoreBotReadyEvent readyEvent = new DisCoreBotReadyEvent(this);
        Bukkit.getPluginManager().callEvent(readyEvent);
    }
    
    // =============================
    // getter
    // =============================
    
    public WebhookManager getWebhookManager() {
        return webhookManager;
    }
    
    public JDAManager getJDAManager() {
        return jda;
    }
    
    public MessageExecutor getMessageExecutor() {
        return messageExecutor;
    }

    public DisCoreBotApi getApi() {
        return api;
    }
    
    public File getWebhookSavePath() {
        return new File(getDataFolder(), "data/webhook.yml");
    }
    
    public File getKeyActiveConfigPath() {
        return new File(getDataFolder(), "key_active_config.yml");
    }
    
    // =============================
    // getter (Build-in Addon 向け)
    // =============================
    public File getAddonDataDir() {
        return new File(getDataFolder(), "addons");
    }
}
