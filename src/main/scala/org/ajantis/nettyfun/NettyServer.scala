package org.ajantis.nettyfun

import java.net.InetSocketAddress
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioEventLoopGroup}
import io.netty.channel.{ChannelInboundMessageHandlerAdapter, ChannelHandlerContext, ChannelInitializer, ChannelOption}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.{IdleStateEvent, IdleStateHandler}
import io.netty.handler.codec.http._
import io.netty.channel.ChannelHandler.Sharable

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
      .childHandler(new AkkaChannelInitializer)

    srv.bind().sync()
    println("Listening on %s:%s", addr.getAddress.getHostAddress, addr.getPort)
  }
}

case class Session(ctx: ChannelHandlerContext){
  val created = new java.util.Date
  val ch = ctx.channel
}

@Sharable
class AkkaServerHandler extends ChannelInboundMessageHandlerAdapter[Object]{

  override def exceptionCaught(ctx: ChannelHandlerContext, t: Throwable){}

  override def messageReceived(ctx: ChannelHandlerContext, msg: Object) {
    msg match {
      case http: HttpRequest => AkkaManager.actor ! Session(ctx)
      case _ => // too bad
        println("unsupported frame")
        ctx.channel.write(HttpResponseStatus.BAD_REQUEST)
    }
  }
}