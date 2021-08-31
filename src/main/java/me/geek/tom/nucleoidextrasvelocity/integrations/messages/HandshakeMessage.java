package me.geek.tom.nucleoidextrasvelocity.integrations.messages;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public record HandshakeMessage(
        String channel,
        String gameVersion,
        ServerType serverType
) implements Message {

    @Override
    public JsonObject encode() {
        JsonObject obj = new JsonObject();
        obj.addProperty("channel", this.channel);
        obj.addProperty("game_version", this.gameVersion);
        obj.addProperty("server_type", this.serverType.name());
        return obj;
    }

    @Override
    public String type() {
        return "handshake";
    }
}
