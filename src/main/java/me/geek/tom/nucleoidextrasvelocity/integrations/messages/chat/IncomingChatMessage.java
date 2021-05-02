package me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public class IncomingChatMessage implements Message {
    // TODO: Implement chat bridge?

    @Override
    public JsonObject encode() {
        // never sent
        return null;
    }

    @Override
    public String type() {
        return "chat";
    }
}
