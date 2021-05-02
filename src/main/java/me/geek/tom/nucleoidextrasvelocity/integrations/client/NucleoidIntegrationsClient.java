package me.geek.tom.nucleoidextrasvelocity.integrations.client;

import com.google.gson.JsonObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.WriteTimeoutHandler;
import me.geek.tom.nucleoidextrasvelocity.Util;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.Message;
import me.geek.tom.nucleoidextrasvelocity.integrations.messages.base.MessageRegistry;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NucleoidIntegrationsClient extends SimpleChannelInboundHandler<JsonObject> {
    private static final NioEventLoopGroup EVENT_LOOP_GROUP = new NioEventLoopGroup(1, r -> {
        Thread t = new Thread(r);
        t.setName("nucleoid-integrations");
        t.setDaemon(true);
        return t;
    });
    // Constants pulled from here: https://github.com/NucleoidMC/nucleoid-extras/blob/master/src/main/java/xyz/nucleoid/extras/integrations/connection/IntegrationsSocketConnection.java#L34-L37
    private static final int TIMEOUT_SECONDS = 5;
    private static final int MAX_FRAME_SIZE = 4 * 1024 * 1024;
    private static final int FRAME_HEADER_SIZE = 4;

    private Channel channel;
    private final Handler handler;
    private final MessageRegistry messageRegistry;

    public NucleoidIntegrationsClient(Handler handler, MessageRegistry messageRegistry) {
        this.handler = handler;
        this.messageRegistry = messageRegistry;
    }

    public ChannelFuture connect(SocketAddress addr) {
        ChannelFuture connectFuture = new Bootstrap()
                .group(EVENT_LOOP_GROUP)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new WriteTimeoutHandler(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                                .addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_SIZE, 0, FRAME_HEADER_SIZE, 0, FRAME_HEADER_SIZE))
                                .addLast(new LengthFieldPrepender(FRAME_HEADER_SIZE))
                                .addLast(new JsonCodec())
                                // If it doesn't refer to the outer class, you get a StackOverflowError. Trust me, it will
                                .addLast(NucleoidIntegrationsClient.this);
                    }
                }).connect(addr);

        connectFuture.addListener((ChannelFutureListener) this::connected);
        return connectFuture;
    }

    private void connected(ChannelFuture future) {
        this.channel = future.channel();
        this.handler.onConnect(this);
    }

    public ChannelFuture send(Message message) {
        Util.verifyThat(this.channel != null, "Not connected yet!");
        return this.channel.writeAndFlush(this.messageRegistry.encodeMessage(message));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JsonObject msg) {
        Message message = this.messageRegistry.decodeMessage(msg);
        this.handler.onMessage(this, message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.handler.onDisconnect(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.handler.onException(this, cause);
        ctx.close();
    }

    public void disconnect() {
        this.channel.close();
    }
}
