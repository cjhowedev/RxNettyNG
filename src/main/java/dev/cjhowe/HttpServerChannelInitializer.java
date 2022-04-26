package dev.cjhowe;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

final class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
  private final HttpServerRequestCallback callback;

  HttpServerChannelInitializer(HttpServerRequestCallback callback) {
    this.callback = callback;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline().addLast("requestDecoder", new HttpRequestDecoder())
        .addLast("responseEncoder", new HttpResponseEncoder())
        .addLast("handler", new HttpServerHandler(this.callback));
  }
}
