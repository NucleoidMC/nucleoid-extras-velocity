package me.geek.tom.nucleoidextrasvelocity.integrations;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.geek.tom.nucleoidextrasvelocity.NucleoidExtrasVelocity;
import me.geek.tom.nucleoidextrasvelocity.integrations.client.IntegrationsHandler;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.server.SendServerToServer;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.server.SendToServer;

import java.util.Optional;

public class ServerIntegration extends Integration {
    private ProxyServer proxy;

    public ServerIntegration(IntegrationsHandler handler) {
        super(handler, "server");
    }

    @Override
    public void onRegister(NucleoidExtrasVelocity plugin, ProxyServer proxy) {
        super.onRegister(plugin, proxy);
        this.proxy = proxy;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof SendToServer) {
            Optional<Player> player = this.proxy.getPlayer(((SendToServer) message).player());
            Optional<RegisteredServer> server = this.proxy.getServer(((SendToServer) message).targetServer());
            if (player.isPresent() && server.isPresent()) {
                player.get().createConnectionRequest(server.get()).fireAndForget();
            }
        } else if (message instanceof SendServerToServer) {
            String fromServerName = ((SendServerToServer) message).fromServer();
            String toServerName = ((SendServerToServer) message).toServer();
            Optional<RegisteredServer> fromServer = this.proxy.getServer(fromServerName);
            Optional<RegisteredServer> toServer = this.proxy.getServer(toServerName);
            if (fromServer.isEmpty() || toServer.isEmpty()) {
                this.logger.warn("Received request to move players from '{}' to '{}', but a server doesn't exist!", fromServerName, toServerName);
                return;
            }
            for (Player player : proxy.getAllPlayers()) {
                player.getCurrentServer().map(ServerConnection::getServer).ifPresent(playerServer -> {
                    if (playerServer.equals(fromServer.get())) {
                        player.createConnectionRequest(toServer.get()).fireAndForget();
                    }
                });
            }
        }
    }
}
