package me.geek.tom.nucleoidextrasvelocity.integrations.messages.base;

import com.google.gson.JsonObject;

public interface Message {
    JsonObject encode();
    String type();
}
