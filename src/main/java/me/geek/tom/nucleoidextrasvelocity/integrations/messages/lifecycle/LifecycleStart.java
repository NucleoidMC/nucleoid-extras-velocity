package me.geek.tom.nucleoidextrasvelocity.integrations.messages.lifecycle;

import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.EmptyMessage;

public class LifecycleStart extends EmptyMessage {
    @Override
    public String type() {
        return "lifecycle_start";
    }
}
