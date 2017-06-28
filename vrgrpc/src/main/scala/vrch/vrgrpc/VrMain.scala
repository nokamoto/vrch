package vrch.vrgrpc

import java.util.concurrent.{Executors, TimeUnit}

import io.grpc.netty.NettyServerBuilder
import vrch.{VrClusterServiceGrpc, VrServiceGrpc}

import scala.concurrent.ExecutionContext

trait Vr extends UseVrConfig with UseVrService with UseVrClusterService with UseVrCluster  {
  private[this] lazy val context = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(vrConfig.concurrency))

  private[this] lazy val server = {
    Seq(
      VrServiceGrpc.bindService(vrService, context),
      VrClusterServiceGrpc.bindService(vrClusterService, context)
    ).foldLeft(NettyServerBuilder.forPort(vrConfig.port))(_.addService(_)).build()
  }

  def start(): Unit = server.start()

  def awaitTermination(): Unit = server.awaitTermination()

  def shutdown(): Unit = {
    val timeout = vrConfig.shutdownTimeout.toMillis
    val unit = TimeUnit.MILLISECONDS

    server.shutdown()
    if (!server.awaitTermination(timeout, unit)) {
      server.shutdownNow()
    }

    vrCluster.shutdown()

    context.shutdown()
    if (!context.awaitTermination(timeout, unit)) {
      context.shutdownNow()
    }
  }
}

object VrMain extends Vr with MixinVrConfig with MixinVrService with MixinVrClusterService with MixinVrCluster {
  def main(args: Array[String]): Unit = {
    start()

    sys.addShutdownHook(shutdown())

    awaitTermination()
  }
}
