package vrch.vrgrpc

import java.util.concurrent.atomic.AtomicReference

import com.google.protobuf.ByteString
import com.google.protobuf.empty.Empty
import io.grpc.netty.NettyChannelBuilder
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, ServerServiceDefinition}
import org.scalatest.{Assertion, FlatSpec, Suite}
import vrch.grpc.{MixinExecutionContext, ServerConfig, ServerMain}
import vrch.util.AvailablePort
import vrch.vrgrpc.VrServiceSpec.{expect, observer, withServer, clusterInfo}
import vrch._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class VrServiceSpec extends FlatSpec {
  it should "join nodes and keep-alive" in {
    withServer { channel =>
      val clusterStub = VrClusterServiceGrpc.stub(channel)

      val expected1 = new AtomicReference[Outgoing]()
      val expected2 = new AtomicReference[Outgoing]()

      val in1 = clusterStub.join(observer(expected1.set))
      val in2 = clusterStub.join(observer(expected2.set))

      expect(10.seconds)(assert(clusterInfo(channel).node.size === 2))

      in1.onNext(Incoming().update(_.keeplive := 1))
      in2.onNext(Incoming().update(_.keeplive := 2))

      expect(10.seconds)(assert(expected1.get() === Outgoing().update(_.keepalive := 1)))
      expect(10.seconds)(assert(expected2.get() === Outgoing().update(_.keepalive := 2)))

      in1.onNext(Incoming().update(_.keeplive := 2))
      in2.onNext(Incoming().update(_.keeplive := 3))

      expect(10.seconds)(assert(expected1.get() === Outgoing().update(_.keepalive := 2)))
      expect(10.seconds)(assert(expected2.get() === Outgoing().update(_.keepalive := 3)))

      in1.onCompleted()
      in2.onCompleted()
    }
  }

  it should "cancel on error" in {
    withServer { channel =>
      val clusterStub = VrClusterServiceGrpc.stub(channel)
      val in = clusterStub.join(observer(_ => ()))

      expect(10.seconds)(assert(clusterInfo(channel).node.size === 1))

      in.onError(new RuntimeException("cancel"))

      expect(10.seconds)(assert(clusterInfo(channel).node.size === 0))
    }
  }

  it should "talk to a node" in {
    withServer { channel =>
      val clusterStub = VrClusterServiceGrpc.stub(channel)
      val stub = VrServiceGrpc.blockingStub(channel)

      val in = new AtomicReference[StreamObserver[Incoming]]()

      in.set(clusterStub.join(
        observer(out => in.get().onNext(Incoming().update(_.voice.voice := ByteString.copyFromUtf8(out.getText.text))))
      ))

      def echo(ss: String*) = {
        ss.foreach { s =>
          val text = Text().update(_.text := s)
          assert(stub.talk(text) === Voice().update(_.voice := ByteString.copyFromUtf8(text.text)))
        }
      }

      echo("echo", "this", "message")

      in.get().onCompleted()
    }
  }
}

object VrServiceSpec extends AvailablePort with Suite {
  def clusterInfo(channel: ManagedChannel): ClusterInfo = VrClusterServiceGrpc.blockingStub(channel).info(Empty())

  def expect(timeout: FiniteDuration)(f: => Assertion): Unit = {
    val ms = timeout.toMillis / 10

    val fs = (1 to 10).foldLeft(Option[Throwable](new RuntimeException)) { case (last, trial) =>
      last match {
        case Some(_) =>
          if (trial > 1) Thread.sleep(ms)

          try {
            f
            None
          } catch {
            case t: Throwable =>
              Some(t)
          }

        case None => None
      }
    }

    fs.foreach(throw _)
  }

  def observer(f: Outgoing => Unit): StreamObserver[Outgoing] = {
    new StreamObserver[Outgoing] {
      override def onError(t: Throwable): Unit = println(t)

      override def onCompleted(): Unit = ()

      override def onNext(value: Outgoing): Unit = f(value)
    }
  }

  def withServer(f: ManagedChannel => Unit): Unit = {
    val p = availablePort()

    val grpc = new ServerMain
      with MixinVrClusterService with MixinVrService with MixinExecutionContext with MixinVrCluster with MixinVrConfig {

      override protected[this] def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
        Seq(VrServiceGrpc.bindService(vrService, _), VrClusterServiceGrpc.bindService(vrClusterService, _))
      }

      override def serverConfig: ServerConfig = ServerConfig(port = p, concurrency = 3, shutdownTimeout = 10.seconds)

      override def shutdown(): Unit = {
        super.shutdown()
        vrCluster.shutdown()
      }
    }

    val channel = NettyChannelBuilder.forAddress("localhost", p).usePlaintext(true).build()

    try {
      grpc.start()
      f(channel)
    } finally {
      channel.shutdown()
      grpc.shutdown()
    }
  }
}
