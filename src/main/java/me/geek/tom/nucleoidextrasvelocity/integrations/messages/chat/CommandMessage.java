package me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public class CommandMessage implements Message {
    public final String command;
    public final String sender;

    public CommandMessage(String command, String sender) {
        this.command = command;
        this.sender = sender;
    }

    @Override
    public JsonObject encode() {
        // Never sent
        return null;
    }

    public static CommandMessage decode(JsonObject obj) {
        return new CommandMessage(
                obj.get("command").getAsString(),
                obj.get("sender").getAsString()
        );
    }

    @Override
    public String type() {
        return "command";
    }

    @Override
    public String toString() {
        return "CommandMessage{" +
                "command='" + command + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
