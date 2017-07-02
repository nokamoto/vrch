package vrch.vrchcli

import io.grpc.netty.NettyChannelBuilder
import vrch.{Dialogue, Request, VrchServiceGrpc}

object VrchCli {
  def main(args: Array[String]): Unit = {
    val channel = NettyChannelBuilder.forAddress("35.189.148.55", 80).usePlaintext(true).build()

    val stub = VrchServiceGrpc.blockingStub(channel)

    def talk(context: String): String = {
      Console.print("> ")
      val s = Console.in.readLine()
      val req = Request().update(_.dialogue := Dialogue().update(_.text.text := s, _.context := context))
      val res = stub.talk(req)
      Console.println(res)
      talk(res.getDialogue.context)
    }

    talk("")
  }
}
