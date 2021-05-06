package me.geek.tom.nucleoidextrasvelocity.integrations.messages.base;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat.CommandMessage;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat.IncomingChatMessage;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.server.SendToServer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageRegistry {
    private final Map<String, Function<JsonObject, ? extends Message>> messages = new HashMap<>();

    public MessageRegistry() {
        this.registerMessages();
    }

    private void registerMessages() {
        this.messages.put("chat", o -> new IncomingChatMessage());
        this.messages.put("command", CommandMessage::decode);
        this.messages.put("send_to_server", SendToServer::decode);
    }

    public JsonObject encodeMessage(Message message) {
        String type = message.type();
        JsonObject body = message.encode();
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        msg.add("body", body);
        return msg;
    }

    public Message decodeMessage(JsonObject msg) {
        String type = msg.get("type").getAsString();
        if (!messages.containsKey(type)) {
            throw new IllegalArgumentException("Unknown message type received: " + type);
        }
        JsonObject body = msg.getAsJsonObject("body");
        return messages.get(type).apply(body);
    }
}
