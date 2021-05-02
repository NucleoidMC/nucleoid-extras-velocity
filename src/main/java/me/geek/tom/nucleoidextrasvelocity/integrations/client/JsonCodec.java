package me.geek.tom.nucleoidextrasvelocity.integrations.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonCodec extends MessageToMessageCodec<ByteBuf, JsonObject> {
    private static final Gson GSON = new Gson();

    @Override
    protected void encode(ChannelHandlerContext ctx, JsonObject msg, List<Object> out) {
//        System.out.println("send: " + msg);
        ByteBuf buf = Unpooled.wrappedBuffer(GSON.toJson(msg).getBytes(StandardCharsets.UTF_8));
        out.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        JsonObject obj = GSON.fromJson(msg.toString(StandardCharsets.UTF_8), JsonObject.class);
//        System.out.println("recv: " + obj);
        out.add(obj);
    }
}
