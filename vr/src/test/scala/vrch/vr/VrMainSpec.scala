package vrch.vr

import java.util.concurrent.TimeUnit

import com.google.protobuf.ByteString
import io.grpc.{ManagedChannel, StatusRuntimeException}
import io.grpc.netty.NettyChannelBuilder
import io.grpc.stub.StreamObserver
import org.scalatest.FlatSpec
import vrch.vr.VrMainSpec.withServer
import vrch._

import scala.concurrent.duration._

class VrMainSpec extends FlatSpec {
  it should "invoke Talk to empty cluster" in {
    withServer(9000) { channel =>
      val stub = VrServiceGrpc.blockingStub(channel)
      intercept[StatusRuntimeException](stub.talk(Text()))
    }
  }

  it should "invoke Talk to single node cluster" in {
    withServer(9001) { channel =>
      val stub = VrClusterServiceGrpc.stub(channel)

      var res: StreamObserver[Incoming] = null

      res = stub.join(new StreamObserver[Outgoing] {
        override def onError(t: Throwable): Unit = {
          println(s"client error: $t")
        }

        override def onCompleted(): Unit = {
          println("client completed.")
        }

        override def onNext(value: Outgoing): Unit = {
          println(s"client got: ${value.getText}")
          res.onNext(Incoming().update(_.voice.voice := ByteString.copyFromUtf8(value.getText.text)))
        }
      })

      val talkStub = VrServiceGrpc.blockingStub(channel)
      assert(talkStub.talk(Text().update(_.text := "hello")) === Voice().update(_.voice := ByteString.copyFromUtf8("hello")))

      res.onCompleted()
    }
  }
}

object VrMainSpec {
  def withServer(port: Int)(f: ManagedChannel => Unit): Unit = {
    val vr = new Vr with MixinVrService with MixinVrClusterService with MixinVrCluster {
      override def vrConfig: VrConfig = {
        VrConfig(port = port, concurrency = 1, shutdownTimeout = 3.seconds, requestTimeout = 3.seconds)
      }
    }

    val channel = NettyChannelBuilder.forAddress("localhost", port).usePlaintext(true).build()

    try {
      vr.start()

      f(channel)
    } finally {
      channel.shutdown()
      channel.awaitTermination(3, TimeUnit.SECONDS)
      vr.shutdown()
    }
  }
}
