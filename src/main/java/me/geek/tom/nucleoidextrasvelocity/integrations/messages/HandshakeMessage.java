package me.geek.tom.nucleoidextrasvelocity.integrations.messages;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public class HandshakeMessage implements Message {
    public final String channel;
    public final String gameVersion;
    public final ServerType serverType;

    public HandshakeMessage(String channel, String gameVersion, ServerType serverType) {
        this.channel = channel;
        this.gameVersion = gameVersion;
        this.serverType = serverType;
    }

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
