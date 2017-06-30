package vrch.grpc

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

trait UseExecutionContext {
  implicit def context: ExecutionContext
}

trait MixinExecutionContext extends UseServerConfig {
  implicit val context: ExecutionContext = {
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(serverConfig.concurrency))
  }
}
