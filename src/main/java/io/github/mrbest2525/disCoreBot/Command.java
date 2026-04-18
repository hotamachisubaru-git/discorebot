package io.github.mrbest2525.disCoreBot;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Command implements CommandExecutor, TabCompleter {
    
    private final DisCoreBot core;
    
    public Command(DisCoreBot core) {
        this.core = core;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        
        if (args.length < 1) {
            sendHelp(sender, command);
            return true;
        }
        
        switch (args[0]) {
            case "reload":
                core.getWebhookManager().load();
                core.getWebhookManager().save();
                
                sender.sendMessage("§a[DisCoreBot] 設定を再読み込みしました。");
                break;
                
            case "toggle":
                if (args.length < 2) {
                    sender.sendMessage("IDを指定してください");
                    break;
                }
                if (!core.getWebhookManager().hasKey(NamespacedKey.fromString(args[1]))) {
                    sender.sendMessage("そのIDは存在しません");
                    break;
                }
                
                
                NamespacedKey key = NamespacedKey.fromString(args[1]);
                
                if (args.length < 3) {
                    toggleKeyActive(sender, key, null);
                } else if (args[2].equalsIgnoreCase("true")) {
                    toggleKeyActive(sender, key, true);
                } else if (args[2].equalsIgnoreCase("false")) {
                    toggleKeyActive(sender, key, false);
                } else if (args[2].equalsIgnoreCase("switching")) {
                    toggleKeyActive(sender, key, null);
                } else if (args[2].equalsIgnoreCase("restore")) {
                    core.getWebhookManager().restoreOverride(key);
                    sender.sendMessage("オーバーライド設定を無効化しました");
                } else {
                    String setting = !core.getWebhookManager().hasKey(key) ? "無効" : core.getWebhookManager().getOverrideKeyActive(key) ? "true" : "false";
                    sender.sendMessage("現在のオーバーライド設定は [" + setting + "] です");
                }
                break;
                
            default:
                sendHelp(sender, command);
                break;
        }
        return true;
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        // 最初の引数（/discorebot [ここ]）を入力しているとき
        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1:
                completions.add("reload");
                completions.add("toggle");
                completions.add("help");
                
                completions = completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
                break;
            case 2:
                if (args[0].equals("toggle")) {
                    completions.addAll(core.getWebhookManager().getKeys().stream().map(NamespacedKey::toString).toList());
                }
                
                completions = completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                break;
            case 3:
                if (args[0].equals("toggle")) {
                    completions.add("true");
                    completions.add("false");
                    completions.add("switching");
                    completions.add("restore");
                }
                
                completions = completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                break;
        }
        
        // それ以降の引数や、該当なしの場合は空のリストを返す
        return completions;
    }
    
    public void toggleKeyActive(CommandSender sender, NamespacedKey key, Boolean toggle) {
        if (toggle == null) {
            toggle = !core.getWebhookManager().getOverrideKeyActive(key);
        }
        core.getWebhookManager().setOverrideKeyActive(key, toggle);
        sender.sendMessage("[" + key.toString() + "] のオーバーライドを [" + toggle + "] に設定しました");
    }
    
    public void sendHelp(CommandSender sender, org.bukkit.command.Command command) {
        sender.sendMessage(
                command.getName() + "コマンド一覧\n" +
                        // reload
                        "/" + command.getName() + " " + "reload" + "\n" +
                        "    " + "設定を再読み込みします。" + "\n" +
                        "    " + "※注意 このコマンドは限定的な読み込みしかサポートされておらずサーバーを再起動することを推奨します。" + "\n" +
                        // toggle
                        "/" + command.getName() + " " + "toggle" + "<ID>" + "\n" +
                        "    " + "指定したIDの上書き設定を反転します" + "\n" +
                        "/" + command.getName() + " " + "toggle" + "<ID>" + "[true | false]" + "\n" +
                        "    " + "指定したIDの上書き設定を設定します" + "\n"
        );
    }
}
