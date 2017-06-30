package vrch.chgrpc

import io.grpc.ManagedChannel
import io.grpc.netty.NettyChannelBuilder
import org.scalatest.FlatSpec
import vrch.chgrpc.ChGrpcMainSpec.withServer
import vrch.grpc.MixinExecutionContext
import vrch.{ChServiceGrpc, Dialogue}

import scala.concurrent.duration._

class ChGrpcMainSpec extends FlatSpec {
  it should "start dialogue" in {
    withServer { channel =>
      pending

      val stub = ChServiceGrpc.blockingStub(channel)

      val res1 = stub.talk(Dialogue().update(_.text.text := "hello"))
      println(res1)

      Thread.sleep(3000)

      val res2 = stub.talk(Dialogue().update(_.text.text := "what's up?", _.context := res1.context))
      println(res2)

      Thread.sleep(3000)

      val res3 = stub.talk(Dialogue().update(_.text.text := "bye", _.context := res2.context))
      println(res3)

      Thread.sleep(3000)

      val res4 = stub.talk(Dialogue().update(_.text.text := "see you later", _.context := res2.context))
      println(res4)
    }
  }
}

object ChGrpcMainSpec {
  def withServer(f: ManagedChannel => Unit): Unit = {
    val availablePort = 9001

    val server = new ChGrpc with MixinChService with MixinExecutionContext {
      override def chConfig: ChConfig = {
        ChConfig(
          apiKey = "",
          port = availablePort,
          shutdownTimeout = 10.seconds,
          concurrency = 1
        )
      }
    }

    val channel = NettyChannelBuilder.forAddress("localhost", availablePort).usePlaintext(true).build()

    try {
      server.start()

      f(channel)
    } finally {
      channel.shutdown()
      server.shutdown()
    }

  }
}
