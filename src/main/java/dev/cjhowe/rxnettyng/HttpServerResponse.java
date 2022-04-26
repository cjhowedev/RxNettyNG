package dev.cjhowe.rxnettyng;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.rxjava3.core.Flowable;

/** Represents an in progress HTTP response to a request from an HttpServer. */
public final class HttpServerResponse {
  private final ChannelHandlerContext context;
  private boolean headWritten = false;

  HttpServerResponse(ChannelHandlerContext context) {
    this.context = context;
  }

  /**
   * Starts sending an HTTP response with the given status.
   *
   * @param status The HTTP status of the response
   * @return this for chaining
   */
  public HttpServerResponse writeHead(HttpResponseStatus status) {
    return writeHead(status, HttpVersion.HTTP_1_0);
  }

  /**
   * Starts sending an HTTP response with the given status and version.
   *
   * @param status The HTTP status of the response
   * @param version The version to report in the response
   * @return this for chaining
   */
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

  /**
   * Ends the HTTP response with a streaming raw body.
   *
   * @param body A Flowable containing raw chunks to write to the HTTP response body
   */
  public void end(Flowable<ByteBuf> body) {
    ensureHead();

    body.subscribe(
        (buffer) -> {
          context.write(new DefaultHttpContent(buffer));
        },
        (cause) -> {
          context.fireExceptionCaught(cause);
        },
        () -> {
          end();
        });
  }

  /** Ends the HTTP response with an empty body. */
  public void end() {
    ensureHead();

    context.writeAndFlush(new DefaultLastHttpContent());
    context.close();
  }
}
