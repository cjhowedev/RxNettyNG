package dev.cjhowe.rxnettyng;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseCombiner;
import io.reactivex.rxjava3.core.Flowable;
import java.util.ArrayList;
import java.util.List;

/** Represents an in progress HTTP response to a request from an HttpServer. */
public final class HttpServerResponse {
  private final ChannelHandlerContext context;
  private boolean headWritten = false;
  private final List<ChannelFuture> writeFutures = new ArrayList<>();

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
    writeFutures.add(context.write(new DefaultHttpResponse(version, status)));
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
          writeFutures.add(context.write(new DefaultHttpContent(buffer)));
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

    writeFutures.add(context.writeAndFlush(new DefaultLastHttpContent()));

    Promise<Void> finishedWriting = context.channel().newPromise();
    PromiseCombiner writeFutureCombiner = new PromiseCombiner(context.channel().eventLoop());
    for (ChannelFuture writeFuture : writeFutures) {
      writeFutureCombiner.add(writeFuture);
    }
    writeFutureCombiner.finish(finishedWriting);

    finishedWriting.addListener(
        new GenericFutureListener<Future<Void>>() {
          @Override
          public void operationComplete(Future<Void> f) {
            context.close();
          }
        });
  }
}
