package dev.cjhowe;

import static io.netty.buffer.Unpooled.buffer;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Flowable;

public final class App {
    public static void main(String[] args) throws Exception {
        final HttpServer server = new HttpServer((req, res) -> {
            final ByteBuf bodyBuf = buffer().writeBytes("hello ".getBytes());
            final ByteBuf bodyBuf2 = buffer().writeBytes("world".getBytes());
            res.writeHead(HttpResponseStatus.BAD_REQUEST).end(Flowable.just(bodyBuf, bodyBuf2));
        });
        server.start(8080);
    }
}
