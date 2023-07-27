package fr.bebedlastreat.discord.bungeecord;

import fr.bebedlastreat.discord.bungeecord.commands.BungeeLinkCommand;
import fr.bebedlastreat.discord.bungeecord.commands.BungeeStopbotCommand;
import fr.bebedlastreat.discord.bungeecord.commands.BungeeUnlinkCommand;
import fr.bebedlastreat.discord.bungeecord.implementations.BungeeAsyncRunner;
import fr.bebedlastreat.discord.bungeecord.implementations.BungeeOnlineCheck;
import fr.bebedlastreat.discord.bungeecord.listeners.BungeeJoinListener;
import fr.bebedlastreat.discord.common.enums.DatabaseType;
import fr.bebedlastreat.discord.common.DiscordCommon;
import fr.bebedlastreat.discord.common.DiscordLogger;
import fr.bebedlastreat.discord.common.objects.DiscordRank;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Getter
@Setter
public class DiscordSyncBungee extends Plugin {

    @Getter
    private static DiscordSyncBungee instance;

    private DiscordCommon common;
    private Configuration config;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String token = getConfig().getString("bot-token");
        String guildId = getConfig().getString("guild-id");
        boolean rename = getConfig().getBoolean("rename");

        List<DiscordRank> ranks = new ArrayList<>();

        for (String key : getConfig().getSection("ranks").getKeys()) {
            ranks.add(new DiscordRank(getConfig().getString("ranks." + key + ".id"), getConfig().getString("ranks." + key + ".permission")));
        }

        String db = getConfig().getString("database");
        DatabaseType databaseType = DatabaseType.getByName(db);
        Map<String, Object> credentials = new HashMap<>();
        switch (databaseType) {
            case SQL: {
                Configuration section = getConfig().getSection("sql");
                credentials.put("ip", section.getString("ip"));
                credentials.put("port", section.getInt("port"));
                credentials.put("user", section.getString("user"));
                credentials.put("password", section.getString("password"));
                credentials.put("database", section.getString("database"));
                credentials.put("table", section.getString("table"));
                credentials.put("properties", section.getString("properties"));
                break;
            }
        }

        Map<String, String> messages = new HashMap<>();
        Configuration messagesSection = getConfig().getSection("messages");
        for (String key : messagesSection.getKeys()) {
            messages.put(key, messagesSection.getString(key));
        }

        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            DiscordLogger.log(Level.INFO, "Configurating the bot...");
            try {
                common = new DiscordCommon(token, guildId, rename, databaseType, ranks, credentials, messages, new BungeeOnlineCheck(), new BungeeAsyncRunner());

                PluginManager pm = ProxyServer.getInstance().getPluginManager();
                pm.registerCommand(this, new BungeeLinkCommand(common));
                pm.registerCommand(this, new BungeeUnlinkCommand(common));
                pm.registerCommand(this, new BungeeStopbotCommand(common));
                pm.registerListener(this, new BungeeJoinListener(common));
            } catch (InterruptedException e) {
                DiscordLogger.log(Level.SEVERE, "Failed to enable discord bot");
                e.printStackTrace();
            } finally {
                DiscordLogger.log(Level.INFO, "Discord bot successfully enabled");
            }
        });
    }

    private void saveDefaultConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();
        File file = new File(getDataFolder(), "config.yml");
        try {
            if (!file.exists())
                Files.copy(getResourceAsStream("config.yml"), file.toPath(), new java.nio.file.CopyOption[0]);
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration getConfig() {
        return this.config;
    }
}