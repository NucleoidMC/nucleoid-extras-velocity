package me.geek.tom.nucleoidextrasvelocity.integrations;

import com.velocitypowered.api.proxy.ProxyServer;
import io.netty.channel.ChannelFuture;
import me.geek.tom.nucleoidextrasvelocity.NucleoidExtrasVelocity;
import me.geek.tom.nucleoidextrasvelocity.integrations.client.IntegrationsHandler;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public abstract class Integration {
    private final IntegrationsHandler handler;
    public final String name;

    protected Integration(IntegrationsHandler handler, String name) {
        this.handler = handler;
        this.name = name;
    }

    protected final ChannelFuture send(Message message) {
        return this.handler.send(message);
    }

    public void onRegister(NucleoidExtrasVelocity plugin, ProxyServer proxy) { }
    public void onConnect() { }
    public void onMessage(Message message) { }
    public void onProxyShutdown() { }
}
