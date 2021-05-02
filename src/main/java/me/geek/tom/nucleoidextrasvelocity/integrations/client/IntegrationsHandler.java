package me.geek.tom.nucleoidextrasvelocity.integrations.client;

import com.velocitypowered.api.proxy.ProxyServer;
import io.netty.channel.ChannelFuture;
import me.geek.tom.nucleoidextrasvelocity.NucleoidExtrasVelocity;
import me.geek.tom.nucleoidextrasvelocity.Util;
import me.geek.tom.nucleoidextrasvelocity.integrations.Integration;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.HandshakeMessage;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.ServerType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class IntegrationsHandler implements Handler {
    private final NucleoidExtrasVelocity plugin;
    private final String channelName;
    private final ProxyServer proxy;
    private final Logger logger;
    private NucleoidIntegrationsClient client;

    private final List<Integration> integrations;

    public IntegrationsHandler(NucleoidExtrasVelocity plugin, String channelName, ProxyServer proxy, Logger logger) {
        this.plugin = plugin;
        this.channelName = channelName;
        this.proxy = proxy;
        this.logger = logger;
        this.integrations = new ArrayList<>();
    }

    public void addIntegration(Function<IntegrationsHandler, Integration> supplier) {
        Integration integration = supplier.apply(this);
        this.integrations.add(integration);
        integration.onRegister(this.plugin, this.proxy);
    }

    @Override
    public void onConnect(NucleoidIntegrationsClient client) {
        this.client = client;
        client.send(new HandshakeMessage(
                this.channelName,
                Util.formatProxyVersion(this.proxy.getVersion()),
                ServerType.Velocity
        ));
        this.integrations.forEach(Integration::onConnect);
    }

    @Override
    public void onMessage(NucleoidIntegrationsClient client, Message message) {
        this.integrations.forEach(integration -> integration.onMessage(message));
    }

    @Override
    public void onDisconnect(NucleoidIntegrationsClient client) {
        this.logger.info("Disconnected from integrations server!");
    }

    @Override
    public void onException(NucleoidIntegrationsClient client, Throwable e) {
        this.logger.error("Error in netty pipeline:", e);
    }

    public ChannelFuture send(Message message) {
        return this.client.send(message);
    }

    public void onProxyShutdown() {
        this.integrations.forEach(Integration::onProxyShutdown);
    }
}
