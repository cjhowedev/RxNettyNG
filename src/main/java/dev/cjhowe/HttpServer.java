package dev.cjhowe;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class HttpServer {
  private final ServerBootstrap bootstrap = new ServerBootstrap();
  private final EventLoopGroup bossGroup = new NioEventLoopGroup();
  private final EventLoopGroup workerGroup = new NioEventLoopGroup();

  public HttpServer(HttpServerRequestCallback callback) {
    bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
        .childHandler(new HttpServerChannelInitializer(callback));
  }

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
