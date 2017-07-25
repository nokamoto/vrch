package vrch.vrchgrpc

import com.google.protobuf.util.JsonFormat
import com.typesafe.config.ConfigFactory
import vrch.ImplicitDuration._
import vrch.chgrpc.UseChConfig
import vrch.grpc.{ServerConfig, UseServerConfig}
import vrch.vrgrpc.UseVrConfig
import vrchcfg._

trait UseVrchConfig extends UseServerConfig {
  def vrchConfig: VrchCfg

  override def serverConfig: ServerConfig = {
    ServerConfig(
      port = vrchConfig.port,
      concurrency = vrchConfig.concurrency,
      shutdownTimeout = vrchConfig.getShutdownTimeout.duration
    )
  }
}

trait MixinVrchConfig extends UseVrchConfig with UseChConfig with UseVrConfig {
  override def vrchConfig: VrchCfg = {
    val cfg = Cfg[VrchCfg, Vrch.VrchCfg.Builder](
      newBuilder = Vrch.VrchCfg.newBuilder(),
      build = { builder => VrchCfg.fromJavaProto(builder.build()) }
    )

    val fallback = VrchCfg().update(
      _.concurrency := Runtime.getRuntime.availableProcessors(),
      _.shutdownTimeout.seconds := 10,
      _.vr := VrCfg().update(
        _.shutdownTimeout.seconds := 10,
        _.requestTimeout.seconds := 10,
        _.keepaliveInterval.seconds := 10,
        _.keepaliveTimeout.seconds := 30
      ),
      _.ch := ChCfg().update(_.url := "https://api.apigw.smt.docomo.ne.jp")
    )

    val json = JsonFormat.printer().print(VrchCfg.toJavaProto(fallback))

    cfg.proto(ConfigFactory.load().withFallback(ConfigFactory.parseString(json)))
  }

  override def chConfig: ChCfg = vrchConfig.getCh

  override def vrConfig: VrCfg = vrchConfig.getVr
}
