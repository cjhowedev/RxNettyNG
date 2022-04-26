package dev.cjhowe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

final class HttpServerHandler extends ChannelInboundHandlerAdapter {
  private final HttpServerRequestCallback callback;
  private HttpServerRequest request = null;

  HttpServerHandler(HttpServerRequestCallback callback) {
    this.callback = callback;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      request = new HttpServerRequest((HttpRequest) msg);
      callback.handleRequest(request, new HttpServerResponse(ctx));
    } else if (msg instanceof HttpContent) {
      request.onRead(((HttpContent) msg).content());
    } else if (msg instanceof LastHttpContent) {
      final LastHttpContent lastContent = (LastHttpContent) msg;
      request.onRead(lastContent.content());
      request.onComplete(lastContent.trailingHeaders());
    }
  }
}
