package me.geek.tom.nucleoidextrasvelocity.integrations.client;

import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public interface Handler {
    void onConnect(NucleoidIntegrationsClient client);
    void onMessage(NucleoidIntegrationsClient client, Message message);
    void onDisconnect(NucleoidIntegrationsClient client);
    void onException(NucleoidIntegrationsClient client, Throwable e);
}
