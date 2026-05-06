package io.github.mrbest2525.disCoreBot.addon;

import io.github.mrbest2525.disCoreBot.DisCoreBot;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

public class ListCommand implements Listener {
    
    private final DisCoreBot core;
    private final File configFile;
    private YamlConfiguration config;
    private boolean enabled = true;
    
    public ListCommand (DisCoreBot core) {
        this.core = core;
        
        this.configFile = new File(core.getAddonDataDir(), "list_command/list_command.yml");
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
        event.registerSlashCommand(Commands.slash("list", "プレイヤーリストを表示"), commandEvent -> {
            StringBuilder builder = new StringBuilder();
            Bukkit.getServer().getOnlinePlayers().forEach(name -> builder.append("・").append(name.getDisplayName()).append("\n"));
            commandEvent.reply(Bukkit.getServer().getOnlinePlayers().isEmpty() ? "現在オンラインのプレイヤーはいません。" : builder.toString()).queue();
        });
    }
}
