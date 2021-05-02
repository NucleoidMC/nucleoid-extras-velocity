package me.geek.tom.nucleoidextrasvelocity.integrations.messages.chat;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public class SystemMessage implements Message {
    private final String content;

    public SystemMessage(String content) {
        this.content = content;
    }

    @Override
    public JsonObject encode() {
        JsonObject obj = new JsonObject();
        obj.addProperty("content", this.content);
        return obj;
    }

    @Override
    public String type() {
        return "system";
    }
}
