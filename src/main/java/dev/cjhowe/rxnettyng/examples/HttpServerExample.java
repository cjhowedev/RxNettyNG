package dev.cjhowe.rxnettyng.examples;

import static io.netty.buffer.Unpooled.buffer;

import dev.cjhowe.rxnettyng.HttpServer;
import io.netty.buffer.ByteBuf;
import io.reactivex.rxjava3.core.Flowable;

/** A simple example of using HttpServer handle HTTP requests. */
public final class HttpServerExample {
  private HttpServerExample() {
    // no-op
  }

  /**
   * Runs the HttpServer example.
   *
   * @param args The command line arguments
   * @throws Exception Rethrows exceptions from HttpServer
   */
  public static void main(String[] args) throws Exception {
    final HttpServer server =
        new HttpServer(
            (req, res) -> {
              final ByteBuf bodyBuf = buffer().writeBytes("hello ".getBytes());
              final ByteBuf bodyBuf2 = buffer().writeBytes("world".getBytes());
              res.end(Flowable.just(bodyBuf, bodyBuf2));
            });
    server.start(8080);
  }
}
