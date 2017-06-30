package vrch.chcli

import io.grpc.netty.NettyChannelBuilder
import vrch.{ChServiceGrpc, Dialogue}

object ChCli {
  def main(args: Array[String]): Unit = {
    val channel = NettyChannelBuilder.forAddress("localhost", 9000).usePlaintext(true).build()

    val stub = ChServiceGrpc.blockingStub(channel)

    def talk(context: String): String = {
      Console.print("> ")
      val s = Console.in.readLine()
      val res = stub.talk(Dialogue().update(_.text.text := s, _.context := context))
      Console.println(res.getText.text)
      talk(res.context)
    }

    talk("")
  }
}
