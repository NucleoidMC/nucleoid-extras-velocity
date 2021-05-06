package me.geek.tom.nucleoidextrasvelocity;

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
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Plugin(
        id = "nucleoid-extras-velocity",
        name = "Nucleoid Extras Velocity",
        version = "@version@",
        description = "Integrations and miscellaneous Nucleoid features for the Velocity proxy",
        url = "https://github.com/NucleoidMC",
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
        String host = integrationsNode.getNode("host").getString("localhost");
        int port = integrationsNode.getNode("port").getInt();

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

        try {
            configLoader.save(config);
        } catch (IOException e) {
            this.logger.error("Failed to re-save config!", e);
        }

        this.integrationsClient = new NucleoidIntegrationsClient(integrationsHandler, this.messageRegistry);
        this.integrationsClient.connect(new InetSocketAddress(host, port))
                .addListener(__ -> logger.info("Connected to backend!"));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        try {
            this.integrationsHandler.onProxyShutdown();
            this.integrationsClient.disconnect();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
