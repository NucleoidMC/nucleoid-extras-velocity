package me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public record CommandMessage(
        String command,
        String sender
) implements Message {

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
}
