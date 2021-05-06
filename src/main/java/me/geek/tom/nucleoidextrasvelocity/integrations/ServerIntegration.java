package me.geek.tom.nucleoidextrasvelocity.integrations;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.geek.tom.nucleoidextrasvelocity.NucleoidExtrasVelocity;
import me.geek.tom.nucleoidextrasvelocity.integrations.client.IntegrationsHandler;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.server.SendToServer;

import java.util.Optional;

public class ServerIntegration extends Integration {
    private ProxyServer proxy;

    public ServerIntegration(IntegrationsHandler handler) {
        super(handler, "server");
    }

    @Override
    public void onRegister(NucleoidExtrasVelocity plugin, ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof SendToServer) {
            Optional<Player> player = this.proxy.getPlayer(((SendToServer) message).player);
            Optional<RegisteredServer> server = this.proxy.getServer(((SendToServer) message).targetServer);
            if (player.isPresent() && server.isPresent()) {
                player.get().createConnectionRequest(server.get()).fireAndForget();
            }
        }
    }
}
