package me.geek.tom.nucleoidextrasvelocity.integrations.messages.server;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

import java.util.UUID;

public record SendToServer(
        UUID player,
        String targetServer
) implements Message {

    @Override
    public JsonObject encode() {
        JsonObject obj = new JsonObject();
        obj.addProperty("player", this.player.toString());
        obj.addProperty("target_server", this.targetServer);
        return obj;
    }

    public static SendToServer decode(JsonObject obj) {
        return new SendToServer(
                UUID.fromString(obj.get("player").getAsString()),
                obj.get("target_server").getAsString()
        );
    }

    @Override
    public String type() {
        return "send_to_server";
    }
}
