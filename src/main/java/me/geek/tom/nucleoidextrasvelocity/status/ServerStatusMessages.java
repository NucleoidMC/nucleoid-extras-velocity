package me.geek.tom.nucleoidextrasvelocity.status;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

public class ServerStatusMessages {
    private final Map<String, Component> forcedMotds;

    public ServerStatusMessages(Map<String, Component> forcedMotds) {
        this.forcedMotds = forcedMotds;
    }

    @Subscribe
    public void onProxyQuery(ProxyPingEvent event) {
        Optional<InetSocketAddress> vHost = event.getConnection().getVirtualHost();
        vHost.ifPresent(virtualHost -> {
            Component customMessage = forcedMotds.get(virtualHost.getHostString());
            if (customMessage != null) {
                ServerPing ping = event.getPing();
                ServerPing.Builder pingBuilder = ping
                        .asBuilder();
                Component description = ping.getDescriptionComponent();
                if (description != null) {
                    description = description.append(Component.newline()).append(customMessage);
                } else {
                    description = customMessage;
                }
                ServerPing newPing = pingBuilder.description(description).build();
                event.setPing(newPing);
            }
        });
    }
}
