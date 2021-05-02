package me.geek.tom.nucleoidextrasvelocity.integrations.messages.base;

import com.google.gson.JsonObject;

public abstract class EmptyMessage implements Message {
    @Override
    public JsonObject encode() {
        return new JsonObject();
    }
}
