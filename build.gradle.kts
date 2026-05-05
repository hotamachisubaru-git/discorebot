import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.1"
    `maven-publish`
}

group = project.property("group") as String
version = project.property("version") as String

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.11-R0.1-SNAPSHOT")

    implementation("net.dv8tion:JDA:6.4.1") {
        // 音声系は不要なので除外して軽量化
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
    implementation("club.minnced:discord-webhooks:0.8.4")
}

bukkitPluginYaml {
    main = "io.github.mrbest2525.disCoreBot.DisCoreBot"
    apiVersion = "1.21"

    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.addAll("Mr.Best")
    prefix = "DisCoreBot"

    commands {
        register("discorebot") { // コマンド名
            description = "This is the main command for DisCoreBot."
            usage = "/discorebot"
            permission = "discorebot.admin"
        }
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks.shadowJar {
    mergeServiceFiles()

    archiveClassifier.set("all")

    from("LICENSE")
    from("NOTICE")
    from("licenses") {
        into("META-INF/licenses")
    }

    // --- ここからリロケートの設定 ---
    // あなたのパッケージ配下の「libs」という場所に全て引っ越しさせます
    val prefix = "io.github.mrbest2525.disCoreBot.libs"

    // 1. 直接依存（JDAとWebhooks）
    relocate("net.dv8tion.jda", "$prefix.jda")
    relocate("club.minnced.discord.webhook", "$prefix.webhooks")

    // 2. JDAやWebhooksが「中に入れている」間接依存たち
    // これらを指定しないと、他のプラグインのライブラリと衝突してクラッシュします
    relocate("okhttp3", "$prefix.okhttp3")
    relocate("okio", "$prefix.okio")
    relocate("com.fasterxml.jackson", "$prefix.jackson")
    relocate("org.slf4j", "$prefix.slf4j")
    relocate("net.sf.trove4j", "$prefix.trove4j")
    relocate("com.neovisionaries.ws", "$prefix.websocket")
    relocate("org.apache.commons.collections4", "$prefix.commons")
    relocate("org.json", "$prefix.json")
    relocate("org.jetbrains.annotations", "$prefix.jetbrains.annotations")
    // Kotlinは他と競合しやすいので、これもリロケートするのが安全です
    relocate("kotlin", "$prefix.kotlin")
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = "io.github.mrbest2525"
            artifactId = "discorebot"

            artifact(tasks.shadowJar.get()) {
                classifier = "" 
            }

            pom.withXml {
                val node = asNode()
                val dependenciesNode = node.getAt(groovy.xml.QName.valueOf("dependencies"))
                if (dependenciesNode.isNotEmpty()) {
                    node.remove(dependenciesNode[0] as groovy.util.Node)
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mrbest2525/discorebot")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
