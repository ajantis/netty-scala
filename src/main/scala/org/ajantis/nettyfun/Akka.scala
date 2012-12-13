package org.ajantis.nettyfun

import akka.actor.{ActorSystem, Actor, Props}
import io.netty.handler.codec.http.{HttpVersion, DefaultHttpResponse, HttpResponse, HttpResponseStatus}
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil
import akka.util.duration._

/**
 * Copyright iFunSoftware 2011
 * @author Dmitry Ivanov
 */

object AkkaManager {
  val system = ActorSystem("akkaSystem")
  val actor = system.actorOf(Props[NettyAkkaActor])
}

class NettyAkkaActor extends Actor{

  private var clients: Vector[Session] = Vector()
  private case object Send

  def receive = {
    case s: Session =>
      println("We got a session. Enqueing...")
      clients :+= s
    case Send =>
      println("We have to send some stuff...")
      clients.foreach{ s: Session =>
        s.ch.write(nettyResponse(HttpResponseStatus.OK, "my 10 seconds obligation!"))
        s.ch.close()
      }
  }

  private def nettyResponse(status: HttpResponseStatus, body: String): HttpResponse = {
    val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status)
    val content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8)
    resp.setContent(content)
    resp
  }

  context.system.scheduler.schedule(1 second, 10 seconds, self, Send)
}