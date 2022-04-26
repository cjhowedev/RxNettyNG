package dev.cjhowe;

import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;

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

  public String uri() {
    return this.request.uri();
  }

  public HttpMethod method() {
    return this.request.method();
  }

  public HttpHeaders headers() {
    return this.request.headers();
  }

  public HttpHeaders trailers() {
    return trailers;
  }

  public HttpVersion version() {
    return this.request.protocolVersion();
  }

  public Flowable<ByteBuf> body() {
    return Flowable.create(new FlowableOnSubscribe<ByteBuf>() {
      @Override
      public void subscribe(FlowableEmitter<ByteBuf> emitter) throws Exception {
        buffers.forEach(buffer -> emitter.onNext(buffer));
        emitters.add(emitter);
      }
    }, BackpressureStrategy.BUFFER);
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
