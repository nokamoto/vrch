package vrch.vr

import java.util.concurrent.{Executors, TimeUnit}

import io.grpc.ServerBuilder
import vrch.VrServiceGrpc

import scala.concurrent.ExecutionContext

trait Vr extends UseVrConfig with UseVrService {
  private[this] lazy val context = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(vrConfig.concurrency))

  private[this] lazy val server = {
    ServerBuilder.forPort(vrConfig.port).addService(VrServiceGrpc.bindService(vrService, context)).build()
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

    context.shutdown()
    if (!context.awaitTermination(timeout, unit)) {
      context.shutdownNow()
    }
  }
}

object VrMain extends Vr with MixinVrConfig with MixinVrService {
  def main(args: Array[String]): Unit = {
    start()

    sys.addShutdownHook(shutdown())

    awaitTermination()
  }
}
