package vrch.grpc

import java.util.concurrent.TimeUnit

import io.grpc.ServerServiceDefinition
import io.grpc.netty.NettyServerBuilder

import scala.concurrent.ExecutionContext

trait ServerMain extends UseServerConfig with UseExecutionContext {
  protected[this] def services: Seq[ExecutionContext => ServerServiceDefinition]

  private[this] lazy val server = {
    services.foldLeft(NettyServerBuilder.forPort(serverConfig.port)){
      case (builder, svc) => builder.addService(svc(context))
    }.build()
  }

  def start(): Unit = server.start()

  def awaitTermination(): Unit = server.awaitTermination()

  def shutdown(): Unit = {
    val timeout = serverConfig.shutdownTimeout.toMillis
    val unit = TimeUnit.MILLISECONDS

    server.shutdown()
    if (!server.awaitTermination(timeout, unit)) {
      server.shutdownNow()
    }
  }

  def main(args: Array[String]): Unit = {
    start()

    sys.addShutdownHook(shutdown())

    awaitTermination()
  }
}
