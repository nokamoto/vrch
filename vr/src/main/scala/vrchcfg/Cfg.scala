package vrchcfg

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import com.trueaccord.scalapb.GeneratedMessage
import com.typesafe.config.{Config, ConfigRenderOptions}

abstract class Cfg[A <: GeneratedMessage, B <: Message.Builder] {
  type Builder = B

  def newBuilder: Builder

  def build(builder: Builder): A

  def proto(config: Config): A = {
    val json = config.root().render(ConfigRenderOptions.concise().setJson(true))
    val builder = newBuilder

    JsonFormat.parser().ignoringUnknownFields().merge(json, builder)

    build(builder)
  }
}

object Cfg {
  def apply[A <: GeneratedMessage, B <: Message.Builder](newBuilder: => B, build: B => A): Cfg[A, B] = {
    val nb = newBuilder
    val b = build
    new Cfg[A, B] {
      override def newBuilder: Builder = nb
      override def build(builder: Builder): A = b(builder)
    }
  }
}
