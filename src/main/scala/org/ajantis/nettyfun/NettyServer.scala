package org.ajantis.nettyfun

import java.net.InetSocketAddress
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioEventLoopGroup}
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, ChannelOption}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.{IdleStateEvent, IdleStateHandler}
import io.netty.handler.codec.http.{HttpContentCompressor, HttpRequestEncoder, HttpRequestDecoder}

/**
 * Copyright iFunSoftware 2011
 * @author Dmitry Ivanov
 */
object NettyServer {
  def main(args: Array[String]) = {
    val addr = new InetSocketAddress("0.0.0.0", 8080)
    val srv = new ServerBootstrap
    val chan = srv.group(new NioEventLoopGroup, new NioEventLoopGroup)
      .localAddress(addr)
      .channel(classOf[NioServerSocketChannel])
      .childOption[java.lang.Boolean](ChannelOption.TCP_NODELAY, true)
      .childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
      .childOption[java.lang.Boolean](ChannelOption.SO_REUSEADDR, true)
      .childOption[java.lang.Integer](ChannelOption.SO_LINGER, 0)
      .childHandler(new ChannelInitializer[SocketChannel] {
      def initChannel(p1: SocketChannel) {
        val p = p1.pipeline()
        p.addLast("timeout", new IdleStateHandler(0, 110, 0){
          override def channelIdle(ctx: ChannelHandlerContext, edx: IdleStateEvent){
            ctx.channel().close()
          }
        })
        p.addLast("decoder", HttpRequestDecoder)
        p.addLast("encoder", HttpRequestEncoder)
        p.addLast("deflater", HttpContentCompressor)
      }
    })

    srv.bind().sync()
    println("Listening on %s:%s", addr.getAddress.getHostAddress, addr.getPort)
  }
}