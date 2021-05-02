package me.geek.tom.nucleoidextrasvelocity.integrations;

import me.geek.tom.nucleoidextrasvelocity.integrations.client.IntegrationsHandler;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.lifecycle.LifecycleStart;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.lifecycle.LifecycleStop;

public class LifecycleIntegration extends Integration {
    // I do this because not doing it causes a NoClassDefFoundError. No clue why ¯\_(0.0)_/¯
    private final LifecycleStop stop = new LifecycleStop(false);

    public LifecycleIntegration(IntegrationsHandler handler) {
        super(handler, "lifecycle");
    }

    @Override
    public void onConnect() {
        this.send(new LifecycleStart());
    }

    @Override
    public void onProxyShutdown() {
        this.send(this.stop);
    }
}
