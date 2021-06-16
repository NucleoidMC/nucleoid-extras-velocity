package me.geek.tom.nucleoidextrasvelocity.status;

import com.github.tom_the_geek.nac.NucleoidApiClient;
import com.github.tom_the_geek.nac.response.NucleoidGame;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class ServerStatusMessages {
    private final Logger logger;
    private final Map<String, Component> forcedMotds;
    private final Map<String, String> forcedServerChannels;
    private final NucleoidApiClient nucleoidApiClient;

    private final Map<String, List<NucleoidGame>> cache = new HashMap<>();
    private final Map<String, Long> lastRefreshTimes = new HashMap<>();

    public ServerStatusMessages(Logger logger, Map<String, Component> forcedMotds, Map<String, String> forcedServerChannels, NucleoidApiClient nucleoidApiClient) {
        this.logger = logger;
        this.forcedMotds = forcedMotds;
        this.forcedServerChannels = forcedServerChannels;
        this.nucleoidApiClient = nucleoidApiClient;
    }

    @Subscribe
    public void onProxyQuery(ProxyPingEvent event) {
        Optional<InetSocketAddress> vHost = event.getConnection().getVirtualHost();
        vHost.ifPresent(virtualHost -> {
            ServerPing ping = event.getPing();
            ServerPing.Builder pingBuilder = ping
                    .asBuilder();

            Component customMessage = this.forcedMotds.get(virtualHost.getHostString());
            if (customMessage != null) {
                Component description = ping.getDescriptionComponent();
                if (description != null) {
                    description = description.append(Component.newline()).append(customMessage);
                } else {
                    description = customMessage;
                }
                pingBuilder.description(description);
            }

            String forcedChannel = this.forcedServerChannels.get(virtualHost.getHostString());
            if (forcedChannel != null) {
                List<NucleoidGame> games = this.getOpenGames(forcedChannel);
                pingBuilder.getSamplePlayers().addAll(this.formatGames(games));
            }

            ServerPing newPing = pingBuilder.build();
            event.setPing(newPing);
        });
    }

    private List<ServerPing.SamplePlayer> formatGames(List<NucleoidGame> games) {
        if (games.isEmpty()) {
            return Collections.singletonList(
                    new ServerPing.SamplePlayer(
                            "No games are open right now!",
                            UUID.randomUUID()
                    )
            );
        }

        return games.stream()
                .map(game -> game.name + ": " + game.playerCount)
                .map(s -> new ServerPing.SamplePlayer(s, UUID.randomUUID()))
                .collect(Collectors.toList());
    }

    private List<NucleoidGame> getOpenGames(String channel) {
        long now = System.currentTimeMillis();
        List<NucleoidGame> games = this.cache.get(channel);

        if (games == null || this.lastRefreshTimes.getOrDefault(channel, -1L) + 1000L < now) {
            this.nucleoidApiClient.getServerStatus(channel).handle((status, t) -> {
                if (t != null) {
                    if (!(t instanceof FileNotFoundException)) {
                        this.logger.warn("Failed to fetch server status for " + channel, t);
                        this.cache.put(channel, Collections.emptyList());
                    }
                }

                if (status != null) {
                    this.cache.put(channel, status.games);
                    this.lastRefreshTimes.put(channel, now);
                }

                return null;
            });

            return this.cache.getOrDefault(channel, Collections.emptyList());
        }

        return games;
    }
}
