package vrch.grpc

import java.util.concurrent.{Executors, TimeUnit}

import io.grpc.ServerServiceDefinition
import io.grpc.netty.NettyServerBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait ServerMain {
  protected[this] def port: Int

  protected[this] def shutdownTimeout: FiniteDuration

  protected[this] def concurrency: Int

  private[this] lazy val context: ExecutionContext = {
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(concurrency))
  }

  protected[this] def services: Seq[ExecutionContext => ServerServiceDefinition]

  private[this] lazy val server = {
    services.foldLeft(NettyServerBuilder.forPort(port)){ case (builder, svc) => builder.addService(svc(context)) }.build()
  }

  def start(): Unit = server.start()

  def awaitTermination(): Unit = server.awaitTermination()

  def shutdown(): Unit = {
    val timeout = shutdownTimeout.toMillis
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
