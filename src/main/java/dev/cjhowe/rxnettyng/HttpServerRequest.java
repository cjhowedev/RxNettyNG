package dev.cjhowe.rxnettyng;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import java.util.ArrayList;
import java.util.List;

/** Represents an HTTP request sent to an HttpServer. */
public final class HttpServerRequest {
  private final HttpRequest request;
  private HttpHeaders trailers = null;
  private final List<ByteBuf> buffers;
  private final List<FlowableEmitter<ByteBuf>> emitters;

  HttpServerRequest(HttpRequest request) {
    this.request = request;
    this.buffers = new ArrayList<>();
    this.emitters = new ArrayList<>();
  }

  /**
   * Gets the URI of a HTTP request.
   *
   * @return The URI of the request
   */
  public String uri() {
    return this.request.uri();
  }

  /**
   * Gets the HTTP method of a request.
   *
   * @return The HTTP method of the request.
   */
  public HttpMethod method() {
    return this.request.method();
  }

  /**
   * Gets the HTTP headers of a request.
   *
   * @return the HTTP headers of the request
   */
  public HttpHeaders headers() {
    return this.request.headers();
  }

  /**
   * Gets the HTTP trailers of a request if they have been received.
   *
   * @return The HTTP trailers of the request
   */
  public HttpHeaders trailers() {
    return trailers;
  }

  /**
   * Gets the HTTP version of a request.
   *
   * @return the HTTP version of the request
   */
  public HttpVersion version() {
    return this.request.protocolVersion();
  }

  /**
   * Gets the streaming raw body of a request.
   *
   * @return A Flowable containing chunks of HTTP content
   */
  public Flowable<ByteBuf> body() {
    return Flowable.create(
        new FlowableOnSubscribe<ByteBuf>() {
          @Override
          public void subscribe(FlowableEmitter<ByteBuf> emitter) throws Exception {
            buffers.forEach(buffer -> emitter.onNext(buffer));
            emitters.add(emitter);
          }
        },
        BackpressureStrategy.BUFFER);
  }

  void onRead(ByteBuf buffer) {
    emitters.forEach(emitter -> emitter.onNext(buffer));
    buffers.add(buffer);
  }

  void onComplete(HttpHeaders trailers) {
    this.trailers = trailers;
    emitters.forEach(emitter -> emitter.onComplete());
  }
}
