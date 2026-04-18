package io.github.mrbest2525.disCoreBot;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebhookManager {
    
    private final DisCoreBot core;
    
    private YamlConfiguration webhookConfig;
    private YamlConfiguration keyActiveConfig;
    
    private final Map<NamespacedKey, WebhookMapTempContent> tempWebhookMap = new HashMap<>();
    
    private final Map<String, String> webhookMap = new HashMap<>();
    
    private final Map<NamespacedKey, KeyActiveContent> keyActiveMap = new HashMap<>();
    
    public WebhookManager(DisCoreBot core) {
        this.core = core;
        
        loadWebhook();
    }
    
    public void register(NamespacedKey key, String channelId, StackTraceElement stackTrace) {
        if (!tempWebhookMap.containsKey(key)) {
            tempWebhookMap.put(key, new WebhookMapTempContent(channelId, stackTrace));
            keyActiveMap.put(key, new KeyActiveContent(true, false));
        } else {
            Bukkit.getLogger().warning("以下のキーが重複しているため新規登録を無視しました。\nKey[" +
                    key.toString() +
                    "] \n既存登録呼び出し元[" +
                    tempWebhookMap.get(key).stackTrace +
                    "] \n新規登録呼び出し元[" +
                    stackTrace +
                    "]"
            );
        }
    }
    
    /**
     * 対気が長いためメインスレッドで呼ばないこと
     * @param channelID DiscordChannelID
     * @return DiscordWebhookURL
     */
    public String getWebhookURL(String channelID) {
        
        if (!webhookMap.containsKey(channelID)) {
            webhookMap.put(channelID, core.getJDAManager().getOrCreateWebhookSync(channelID));
        }
        
        return webhookMap.get(channelID);
    }
    
    /**
     * そのKeyが今アクティブか判定
     * @param key 判定したいKey
     */
    public boolean isActiveKey(NamespacedKey key) {
        if (!keyActiveMap.containsKey(key)) return false;
        if (keyActiveMap.get(key).hasOverride()) return keyActiveMap.get(key).getOverrideActive();
        return keyActiveMap.get(key).getActive();
    }
    
    public boolean hasOverride(NamespacedKey key) {
        return keyActiveMap.containsKey(key) && keyActiveMap.get(key).hasOverride();
    }
    
    public void restoreOverride(NamespacedKey key) {
        if (keyActiveMap.containsKey(key)) keyActiveMap.get(key).restoreOverride();
    }
    
    public boolean hasKey(NamespacedKey key) {
        return keyActiveMap.containsKey(key);
    }
    
    public boolean getkeyActive(NamespacedKey key) {
        return keyActiveMap.containsKey(key) && keyActiveMap.get(key).getActive();
    }
    
    public boolean getOverrideKeyActive(NamespacedKey key) {
        return keyActiveMap.containsKey(key) && keyActiveMap.get(key).getOverrideActive();
    }
    
    public void setOverrideKeyActive(NamespacedKey key, boolean set) {
        if (keyActiveMap.containsKey(key)) keyActiveMap.get(key).setOverrideActive(set);
    }
    
    public List<NamespacedKey> getKeys() {
        return List.copyOf(keyActiveMap.keySet());
    }
    
    /**
     * URLが無効（404等）だった場合に呼び出してキャッシュをクリアする
     */
    public void invalidate(String channelID) {
        webhookMap.remove(channelID);
    }
    
    public void load() {
        loadWebhook();
        loadKeyActiveConfig();
    }
    
    public void save() {
        saveWebhook();
        saveKeyActiveConfig();
    }
    
    public void loadWebhook() {
        File file = core.getWebhookSavePath();
        if (file.exists()) {
            webhookConfig = YamlConfiguration.loadConfiguration(file);
        }
        if (webhookConfig == null) return;
        
        webhookConfig.getKeys(false).forEach(key -> webhookMap.put(key, webhookConfig.getString(key)));
    }
    
    public void saveWebhook() {
        File file = core.getWebhookSavePath();
        
        file.getParentFile().mkdirs();
        webhookConfig = new YamlConfiguration();
        
        webhookMap.forEach((channelId, url) -> {
            webhookConfig.set(channelId, url);
        });
        
        try {
            webhookConfig.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe(e.toString());
        }
    }
    
    public void loadKeyActiveConfig() {
        File file = core.getKeyActiveConfigPath();
        if (file.exists()) {
            keyActiveConfig = YamlConfiguration.loadConfiguration(file);
        }
        if (keyActiveConfig == null) return;
        
        keyActiveConfig.getKeys(false).forEach(key ->
            keyActiveMap.put(NamespacedKey.fromString(key), new KeyActiveContent(keyActiveConfig.getBoolean(key), false))
        );
        
    }
    
    public void saveKeyActiveConfig() {
        File file = core.getKeyActiveConfigPath();
        
        file.getParentFile().mkdirs();
        keyActiveConfig = new YamlConfiguration();
        
        keyActiveMap.forEach((key, keyActiveContent) -> keyActiveConfig.set(key.toString(), keyActiveContent.getActive()));
        
        try {
            keyActiveConfig.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe(e.toString());
        }
    }
    
    public record WebhookMapTempContent(
            String channelId,
            StackTraceElement stackTrace
    ) {}
    
    public static class KeyActiveContent {
        private final boolean active;
        private boolean override = false;
        private boolean overrideActive;
        
        public KeyActiveContent(boolean active, boolean overrideActive) {
            this.active = active;
            this.overrideActive = overrideActive;
        }
        
        public boolean getActive() {
            return active;
        }
        
        public boolean hasOverride() {
            return override;
        }
        
        public void restoreOverride() {
            override = false;
        }
        
        public boolean getOverrideActive() {
            return overrideActive;
        }
        
        public void setOverrideActive(boolean value) {
            override = true;
            overrideActive = value;
        }
    }
}
