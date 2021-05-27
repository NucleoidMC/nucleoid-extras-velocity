package me.geek.tom.nucleoidextrasvelocity.integrations;

import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import me.geek.tom.nucleoidextrasvelocity.NucleoidExtrasVelocity;
import me.geek.tom.nucleoidextrasvelocity.integrations.client.IntegrationsHandler;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat.CommandMessage;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat.SystemMessage;
import net.kyori.text.Component;
import net.kyori.text.serializer.plain.PlainComponentSerializer;
import org.slf4j.Logger;

public class CommandsIntegration extends Integration {
    private ProxyServer proxy;
    private NucleoidExtrasVelocity plugin;
    private IntegrationCommandSource source;

    public CommandsIntegration(IntegrationsHandler handler) {
        super(handler, "commands");
    }

    @Override
    public void onRegister(NucleoidExtrasVelocity plugin, ProxyServer proxy) {
        super.onRegister(plugin, proxy);
        this.proxy = proxy;
        this.plugin = plugin;
    }

    private IntegrationCommandSource getSource() {
        if (this.source == null) {
            this.source = new IntegrationCommandSource();
        }
        return this.source;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof CommandMessage) {
            this.proxy.getScheduler().buildTask(this.plugin, () -> {
                CommandMessage command = (CommandMessage) message;
                String cmd = command.command;

                // the velocity shutdown command has a very stupid behaviour - the command source
                // MUST be == proxy.getConsoleCommandSource() or else the command fails. Its not
                // very good behaviour if you're trying to implement another version of the console
                // like this lol.
                if (cmd.trim().startsWith("shutdown")) {
                    this.send(new SystemMessage("You have reached the hacky //shutdown discord command." +
                            " The server will now shut down..."));
                    proxy.shutdown();
                    return;
                }

                this.proxy.getCommandManager().executeAsync(this.getSource(), cmd).thenAccept((ok) -> {
                    if (!ok) {
                        this.send(new SystemMessage("Unknown command!"));
                    }
                }).exceptionally(ex -> {
                    this.send(new SystemMessage("Failed to execute command, see the proxy log for info."));
                    this.logger.error("Failed to execute command: " + cmd + " (triggered by: " + command.sender + ")", ex);
                    return null;
                });
            }).schedule();
        }
    }

    private class IntegrationCommandSource implements ConsoleCommandSource {
        // this function is @Deprecated, but we still NEED to override it.
        // Intellij doesn't notice that its deprecated (probably because its part of an interface), but this is still here to make javac/gradle shut up
        @SuppressWarnings({"deprecation", "RedundantSuppression"})
        @Override
        public void sendMessage(Component component) {
            CommandsIntegration.this.send(new SystemMessage(PlainComponentSerializer.INSTANCE.serialize(component)));
        }

        @Override
        public Tristate getPermissionValue(String permission) {
            // Give integrations full permissions for commands.
            return Tristate.TRUE;
        }
    }
}
