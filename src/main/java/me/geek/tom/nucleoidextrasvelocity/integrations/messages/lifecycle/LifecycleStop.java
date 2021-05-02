package me.geek.tom.nucleoidextrasvelocity.integrations.messages.lifecycle;

import com.google.gson.JsonObject;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;

public class LifecycleStop implements Message {
    private final boolean crash;

    public LifecycleStop(boolean crash) {
        this.crash = crash;
    }

    @Override
    public JsonObject encode() {
        JsonObject obj = new JsonObject();
        obj.addProperty("crash", this.crash);
        return obj;
    }

    @Override
    public String type() {
        return "lifecycle_stop";
    }
}
