package me.geek.tom.nucleoidextrasvelocity.integrations.messages.server;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public record SendServerToServer(
        String fromServer,
        String toServer
) implements Message {

    @Override
    public JsonObject encode() {
        JsonObject obj = new JsonObject();
        obj.addProperty("from_server", this.fromServer);
        obj.addProperty("to_server", this.toServer);
        return obj;
    }

    public static SendServerToServer decode(JsonObject obj) {
        return new SendServerToServer(
                obj.get("from_server").getAsString(),
                obj.get("to_server").getAsString()
        );
    }

    @Override
    public String type() {
        return "send_server_to_server";
    }
}
