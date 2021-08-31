package me.geek.tom.nucleoidextrasvelocity.packets;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public class ExtrasPacketHandler {
    private static final String CHANGE_SERVER_ID = "nucleoid:switch_server";

    private final Logger logger;
    private final ProxyServer proxy;

    public ExtrasPacketHandler(Logger logger, ProxyServer proxy) {
        this.logger = logger;
        this.proxy = proxy;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        ChannelMessageSource source = event.getSource();
        if (source instanceof ServerConnection server) { // only allow message from backend servers
            switch (event.getIdentifier().getId()) {
                case CHANGE_SERVER_ID:
                    this.handleServerChangePacket(server, event);
                    break;
            }
        }
    }

    private void handleServerChangePacket(ServerConnection server, PluginMessageEvent event) {
        String targetServerName = event.dataAsDataStream().readUTF();
        if (server.getServer().getServerInfo().getName().equals(targetServerName)) {
            // Player is already on target server, ignore
            return;
        }
        Optional<RegisteredServer> targetServer = this.proxy.getServer(targetServerName);
        Player player = server.getPlayer();
        if (targetServer.isPresent()) {
            player.createConnectionRequest(targetServer.get()).fireAndForget();
        } else {
            this.logger.warn("Received request to move player {} to unknown server: {}", player.getUniqueId(), targetServerName);
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }
}
