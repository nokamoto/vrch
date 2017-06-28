package vrch.vr

import java.util.concurrent.TimeUnit

import com.google.protobuf.ByteString
import io.grpc.netty.NettyChannelBuilder
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, StatusRuntimeException}
import org.scalatest.FlatSpec
import vrch._
import vrch.vr.VrMainSpec.{observer, withServer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class VrMainSpec extends FlatSpec {
  it should "invoke Talk to empty cluster" in {
    withServer(9000) { channel =>
      val stub = VrServiceGrpc.blockingStub(channel)
      intercept[StatusRuntimeException](stub.talk(Text()))
    }
  }

  it should "invoke Talk to cluster" in {
    withServer(9001) { channel =>
      val stub = VrClusterServiceGrpc.stub(channel)

      var res1: StreamObserver[Incoming] = null
      var res2: StreamObserver[Incoming] = null

      res1 = stub.join(observer { value =>
        res1.onNext(Incoming().update(_.voice.voice := ByteString.copyFromUtf8(value.getText.text)))
      })

      res2 = stub.join(observer { value =>
        res2.onNext(Incoming().update(_.voice.voice := ByteString.copyFromUtf8(value.getText.text)))
      })

      val talkStub = VrServiceGrpc.stub(channel)
      val talks = "talk1" :: "talk2" :: Nil
      val futures = Future.traverse(talks)(s => talkStub.talk(Text().update(_.text := s)))
      val voices = Await.result(futures, 10.seconds)

      assert(voices === talks.map(s => Voice().update(_.voice := ByteString.copyFromUtf8(s))))

      res1.onCompleted()
      res2.onCompleted()
    }
  }
}

object VrMainSpec {
  def observer(f: Outgoing => Unit): StreamObserver[Outgoing] = {
    new StreamObserver[Outgoing] {
      override def onError(t: Throwable): Unit = ()

      override def onCompleted(): Unit = ()

      override def onNext(value: Outgoing): Unit = f(value)
    }
  }

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
