package dev.cjhowe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.rxjava3.core.Flowable;

public final class HttpServerResponse {
  private final ChannelHandlerContext context;
  private boolean headWritten = false;

  HttpServerResponse(ChannelHandlerContext context) {
    this.context = context;
  }

  public HttpServerResponse writeHead(HttpResponseStatus status) {
    return writeHead(status, HttpVersion.HTTP_1_0);
  }

  public HttpServerResponse writeHead(HttpResponseStatus status, HttpVersion version) {
    context.write(new DefaultHttpResponse(version, status));
    headWritten = true;
    return this;
  }

  private void ensureHead() {
    if (!headWritten) {
      writeHead(HttpResponseStatus.OK);
      headWritten = true;
    }
  }

  public void end(Flowable<ByteBuf> body) {
    ensureHead();

    body.subscribe((buffer) -> {
      context.write(new DefaultHttpContent(buffer));
    }, (cause) -> {
      context.fireExceptionCaught(cause);
    }, () -> {
      end();
    });
  }

  public void end() {
    ensureHead();

    context.writeAndFlush(new DefaultLastHttpContent());
    context.close();
  }
}
