[![](https://jitpack.io/v/MrBest2525/discorebot.svg)](https://jitpack.io/#MrBest2525/discorebot)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/AP46ywqz?logo=Modrinth&logoSize=auto&label=DisCoreBot&link=https%3A%2F%2Fmodrinth.com%2Fplugin%2Fdiscorebot)](https://modrinth.com/plugin/discorebot)


# DisCoreBot
DisCoreBotは、Minecraftサーバー（Spigotおよびその派生）とDiscordを連携させるためのコアプラグインです。

## 利用方法
1. **インストール**
  [Modrinth](https://modrinth.com/plugin/discorebot) または [GitHub Releases](https://github.com/MrBest2525/discorebot/releases) からJARファイルをダウンロードし、サーバーの `plugins` フォルダに配置してください。
2. **トークン設定**
  サーバーを起動すると `plugins/DisCoreBot/` 内に `config.yml` が生成されます。`bot-token` 項目にDiscord Botのトークンを入力してください。
3. **アドオンの構成**
  サーバーを再起動すると、内蔵アドオン（Built-in Addons）の設定ファイルが `plugins/DisCoreBot/addons/` 内に生成されます。各設定ファイルの `enabled` を `false` に書き換えることで、特定のアドオンを無効化できます。
4. **外部アドオンの導入**
  他のプラグインと同様に `plugins` フォルダへ配置してください。
  ※外部アドオン起因のバグやエラーについては、各アドオンの開発者へ報告してください。
5. **設定の反映**
   設定変更後はサーバーの再起動を推奨します。`/discorebot reload` コマンドはコア機能の再読み込みに限定されており、すべての変更を反映するわけではありません。

## API利用方法
アドオン開発や他プラグインからの連携には、以下のイベントおよびAPIを利用できます。

### Gradleでの依存関係追加
`build.gradle.kts`に以下の行を追加してください。
```
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.mrbest2525:discorebot:v1.0.4)
}
```
> ※最新のリリースと同じバージョンに変更してください。
[![](https://jitpack.io/v/MrBest2525/discorebot.svg)](https://jitpack.io/#MrBest2525/discorebot)

### 登録イベント: DisCoreBotRegisterEvent
DisCoreBotへ必要な情報を登録するために使用します。
* **チャンネルと識別IDの登録**
  `registerM2D(NamespacedKey key, String channelId)`
* **Discordスラッシュコマンドの登録**
  `registerSlashCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> executor)`

### メッセージイベント: DisCoreBotDiscordMsgEvent
Discordからメッセージが送信された際に発火します。
* **JDAイベントの取得**
  `getEvent()` メソッドからJDAの生イベントにアクセスできます。
* **ヘルパーメソッド**
  * `getMessage()`: 送信されたメッセージを取得します。
  * `getChannelID()`: メッセージが送信されたチャンネルIDを取得します。
  * `getAuthorName()`: 送信したユーザーの名前を取得します。

### その他API
`DisCoreBotApi` を使用することで、以下の機能を利用できます。

#### メッセージ送信: `DisCoreBotApi#sendMessage(NamespacedKey id, String channel, WebhookMessage message)`
事前に登録した識別IDとチャンネルIDを使用してメッセージを送信します。
* **メッセージの作成**: `WebhookMessageBuilder` を使用してメッセージをビルドしてください。埋め込みメッセージ（Embed）も同様の手順で作成・送信可能です。
* **送信処理**: このメソッドで指定されたメッセージは一度送信キューに取り込まれた後、非同期で順次送信されます。

---

# DisCoreBot
DisCoreBot is a core plugin designed to integrate Minecraft servers (Spigot and its forks) with Discord.

## Usage
1. **Installation**
   Download the JAR file from [Modrinth](https://modrinth.com/plugin/discorebot) or [GitHub Releases](https://github.com/MrBest2525/discorebot/releases) and place it in your server's `plugins` folder.
2. **Token Configuration**
   Upon starting the server, a `config.yml` will be generated in `plugins/DisCoreBot/`. Enter your Discord Bot token in the `bot-token` field.
3. **Add-on Configuration**
   After restarting the server, configuration files for Built-in Addons will be generated in `plugins/DisCoreBot/addons/`. You can disable specific addons by setting `enabled` to `false` in their respective config files.
4. **Installing External Add-ons**
   Install them into the `plugins` folder just like any other plugin.
   *Note: Please report bugs or errors caused by external add-ons to their respective developers.*
5. **Applying Changes**
   A server restart is recommended after changing settings. The `/discorebot reload` command is limited to reloading core functions and may not reflect all changes.

## API Usage
For add-on development or integration with other plugins, the following events and APIs are available.

### Adding Dependencies in Gradle
Add the following lines to `build.gradle.kts`:
```
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.mrbest2525:discorebot:v1.0.4)
}
```
> *Please update to the latest release version.
[![](https://jitpack.io/v/MrBest2525/discorebot.svg)](https://jitpack.io/#MrBest2525/discorebot)

### Registration Event: DisCoreBotRegisterEvent
Used to register necessary information to DisCoreBot.
* **Register Channel and Identification ID**
  `registerM2D(NamespacedKey key, String channelId)`
* **Register Discord Slash Commands**
  `registerSlashCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> executor)`

### Message Event: DisCoreBotDiscordMsgEvent
Fired when a message is sent from Discord.
* **Get JDA Event**
  Access the raw JDA event via the `getEvent()` method.
* **Helper Methods**
  * `getMessage()`: Retrieves the sent message.
  * `getChannelID()`: Retrieves the channel ID where the message was sent.
  * `getAuthorName()`: Retrieves the name of the user who sent the message.

### Other APIs
Additional features are available via `DisCoreBotApi`.

#### Sending Messages: `DisCoreBotApi#sendMessage(NamespacedKey id, String channel, WebhookMessage message)`
Sends a message using a previously registered identification ID and channel ID.
* **Creating Messages**: Build your message using `WebhookMessageBuilder`. Embedded messages (Embeds) can also be created and sent using this feature.
* **Transmission Process**: Messages specified in this method are first added to a transmission queue and then sent sequentially and asynchronously.
