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
    compileOnly("org.spigotmc:spigot-api:26.1.2-R0.1-SNAPSHOT")

    implementation("net.dv8tion:JDA:6.4.1") {
        // 音声系は不要なので除外して軽量化
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
    implementation("club.minnced:discord-webhooks:0.8.4")
}

bukkitPluginYaml {
    main = "io.github.mrbest2525.disCoreBot.DisCoreBot"
    apiVersion = "26.1"

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
    toolchain.languageVersion = JavaLanguageVersion.of(25)
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

    
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = "io.github.mrbest2525"
            artifactId = "discorebot"

            artifact(tasks.shadowJar.get()) {
                classifier = "" 
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
