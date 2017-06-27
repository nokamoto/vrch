package vrch.vr

import io.grpc.netty.NettyChannelBuilder
import org.scalatest.FlatSpec
import vrch.{Text, Voice, VrServiceGrpc}

import scala.concurrent.duration._

class VrMainSpec extends FlatSpec {
  it should "invoke Talk" in {
    val port = 9000

    val vr = new Vr with MixinVrService {
      override def vrConfig: VrConfig = VrConfig(port = port, concurrency = 1, shutdownTimeout = 10.seconds)
    }

    try {
      vr.start()

      val channel = NettyChannelBuilder.forAddress("localhost", port).usePlaintext(true).build()
      val stub = VrServiceGrpc.blockingStub(channel)

      assert(stub.talk(Text()) === Voice())
    } finally {
      vr.shutdown()
    }
  }
}
