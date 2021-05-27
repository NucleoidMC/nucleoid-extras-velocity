package me.geek.tom.nucleoidextrasvelocity.integrations;

import com.velocitypowered.api.proxy.ProxyServer;
import io.netty.channel.ChannelFuture;
import me.geek.tom.nucleoidextrasvelocity.NucleoidExtrasVelocity;
import me.geek.tom.nucleoidextrasvelocity.integrations.client.IntegrationsHandler;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;
import org.slf4j.Logger;

public abstract class Integration {
    private final IntegrationsHandler handler;
    public final String name;
    protected Logger logger;

    protected Integration(IntegrationsHandler handler, String name) {
        this.handler = handler;
        this.name = name;
    }

    protected final ChannelFuture send(Message message) {
        return this.handler.send(message);
    }

    public void onRegister(NucleoidExtrasVelocity plugin, ProxyServer proxy) {
        this.logger = plugin.logger;
    }
    public void onConnect() { }
    public void onMessage(Message message) { }
    public void onProxyShutdown() { }
}
