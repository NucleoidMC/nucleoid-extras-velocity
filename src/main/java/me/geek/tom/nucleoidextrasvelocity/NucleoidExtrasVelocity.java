package me.geek.tom.nucleoidextrasvelocity;

import com.github.tom_the_geek.nac.NucleoidApiClient;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.geek.tom.nucleoidextrasvelocity.integrations.CommandsIntegration;
import me.geek.tom.nucleoidextrasvelocity.integrations.LifecycleIntegration;
import me.geek.tom.nucleoidextrasvelocity.integrations.ServerIntegration;
import me.geek.tom.nucleoidextrasvelocity.integrations.client.IntegrationsHandler;
import me.geek.tom.nucleoidextrasvelocity.integrations.client.NucleoidIntegrationsClient;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.MessageRegistry;
import me.geek.tom.nucleoidextrasvelocity.packets.ExtrasPacketHandler;
import me.geek.tom.nucleoidextrasvelocity.status.ServerStatusMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(
        id = "nucleoid-extras-velocity",
        name = "Nucleoid Extras Velocity",
        version = "@version@",
        description = "Integrations and miscellaneous Nucleoid features for the Velocity proxy",
        url = "https://github.com/NucleoidMC/nucleoid-extras-velocity",
        authors = { "Tom_The_Geek" }
)
public class NucleoidExtrasVelocity {
    @Inject
    public Logger logger;

    @Inject
    private ProxyServer proxy;

    @Inject
    @DataDirectory
    private Path pluginDir;

    private final MessageRegistry messageRegistry = new MessageRegistry();
    private NucleoidIntegrationsClient integrationsClient;
    private IntegrationsHandler integrationsHandler;
    private NucleoidApiClient nucleoidApiClient;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!Files.exists(this.pluginDir)) {
            try {
                Files.createDirectories(this.pluginDir);
            } catch (IOException e) {
                this.logger.error("Failed to create plugin directory!", e);
            }
        }

        GsonConfigurationLoader configLoader = GsonConfigurationLoader.builder()
                .setPath(pluginDir.resolve("config.json"))
                .build();

        ConfigurationNode config;
        try {
            config = configLoader.load();
        } catch (IOException e) {
            this.logger.error("Failed to load config, will not connect to integrations!", e);
            return;
        }

        ConfigurationNode integrationsNode = config.getNode("integrations");

        String channelName = integrationsNode.getNode("channel_name").getString("proxy");
        String integrationsHost = integrationsNode.getNode("host").getString("localhost");
        int integrationsPort = integrationsNode.getNode("port").getInt();

        this.logger.info("Starting integrations client...");
        this.integrationsHandler = new IntegrationsHandler(
                this,
                channelName,
                this.proxy,
                this.logger);

        try {
            //noinspection UnstableApiUsage
            List<String> enabled = integrationsNode.getNode("enabled").getList(TypeToken.of(String.class), new ArrayList<>());

            // TODO: More integrations?
            if (enabled.contains("lifecycle")) {
                this.integrationsHandler.addIntegration(LifecycleIntegration::new);
            }
            if (enabled.contains("commands")) {
                this.integrationsHandler.addIntegration(CommandsIntegration::new);
            }
            if (enabled.contains("server")) {
                this.integrationsHandler.addIntegration(ServerIntegration::new);
            }
        } catch (ObjectMappingException e) {
            this.logger.info("Invalid config", e);
            return;
        }

        Map<String, Component> forcedMotds = new HashMap<>();

        for (Map.Entry<Object, ? extends ConfigurationNode> entry : config.getNode("forced_motds").getChildrenMap().entrySet()) {
            if (entry.getKey() instanceof String host) {
                String motd = entry.getValue().getString();
                if (motd == null) {
                    logger.warn("Invalid forced-MOTD for {}: {}", host, entry.getValue());
                    continue;
                }
                forcedMotds.put(host, parseMotd(motd));
            }
        }

        Map<String, String> forcedServerChannels = new HashMap<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : config.getNode("forced_channels").getChildrenMap().entrySet()) {
            if (entry.getKey() instanceof String host) {
                String channel = entry.getValue().getString();
                if (channel == null) {
                    logger.warn("Invalid forced server channel for {}: {}", host, entry.getValue());
                    continue;
                }
                forcedServerChannels.put(host, channel);
            }
        }

        String openGameFormat = config.getNode("open_game_format").getString("&9&l$GAME_NAME$&r: &6$PLAYER_COUNT$");
        String noGamesMessage = config.getNode("no_games_message").getString("&cNo games are open right now!");

        String nucleoidApi = config.getNode("nucleoid_api_base").getString("https://api.nucleoid.xyz/");

        boolean enableSwitchPackets = config.getNode("enable_switch_packets").getBoolean(false);

        try {
            configLoader.save(config);
        } catch (IOException e) {
            this.logger.error("Failed to re-save config!", e);
        }

        this.integrationsClient = new NucleoidIntegrationsClient(integrationsHandler, this.messageRegistry);
        this.integrationsClient.connect(new InetSocketAddress(integrationsHost, integrationsPort))
                .addListener(__ -> logger.info("Connected to backend!"));

        this.nucleoidApiClient = NucleoidApiClient.builder()
                .apiBase(nucleoidApi)
                .build();

        this.proxy.getEventManager().register(this, new ServerStatusMessages(
                this.logger,
                forcedMotds,
                forcedServerChannels,
                openGameFormat,
                noGamesMessage,
                this.nucleoidApiClient
        ));

        if (enableSwitchPackets) {
            this.proxy.getEventManager().register(this, new ExtrasPacketHandler(this.logger, this.proxy));
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        try {
            this.integrationsHandler.onProxyShutdown();
            this.integrationsClient.disconnect();
            this.nucleoidApiClient.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // Mimic the behavior of velocity's motd config option - support both legacy and JSON
    private static Component parseMotd(String motd) {
        if (motd.startsWith("{")) {
            return GsonComponentSerializer.gson().deserialize(motd);
        } else {
            return LegacyComponentSerializer.legacy('&').deserialize(motd);
        }
    }
}
