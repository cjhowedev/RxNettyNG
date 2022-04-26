package dev.cjhowe.rxnettyng;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/** A server that receives HTTP requests and sends HTTP responses over a socket. */
public final class HttpServer {
  private final ServerBootstrap bootstrap = new ServerBootstrap();
  private final EventLoopGroup bossGroup = new NioEventLoopGroup();
  private final EventLoopGroup workerGroup = new NioEventLoopGroup();

  /**
   * Creates an HTTP server to handle HTTP requests.
   *
   * @param callback The callback to call when a new HTTP request is received
   */
  public HttpServer(HttpServerRequestCallback callback) {
    bootstrap
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new HttpServerChannelInitializer(callback));
  }

  /**
   * Starts the HTTP server, starting the handling of requests.
   *
   * @param port The port to listen for HTTP connections
   * @throws Exception Rethrows exceptions when binding to the port to listen.
   */
  public void start(int port) throws Exception {
    try {
      final ChannelFuture bindFuture = bootstrap.bind(port).sync();

      bindFuture.channel().closeFuture().sync();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
