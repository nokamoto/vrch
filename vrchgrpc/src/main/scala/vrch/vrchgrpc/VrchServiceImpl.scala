package vrch.vrchgrpc

import io.grpc.netty.NettyChannelBuilder
import vrch.VrchServiceGrpc.VrchService
import vrch.grpc.UseExecutionContext
import vrch.{ChServiceGrpc, Request, Response, VrServiceGrpc}

import scala.concurrent.{ExecutionContext, Future}

trait VrchServiceImpl extends VrchService with UseVrchConfig with UseExecutionContext {
  private[this] val vrChannel = NettyChannelBuilder.forAddress(vrchConfig.vrHost, vrchConfig.vrPort).usePlaintext(true).build()
  private[this] val chChannel = NettyChannelBuilder.forAddress(vrchConfig.chHost, vrchConfig.chPort).usePlaintext(true).build()

  override def talk(request: Request): Future[Response] = {
    for {
      dialogue <- ChServiceGrpc.stub(chChannel).talk(request.getDialogue)
      voice <- VrServiceGrpc.stub(vrChannel).talk(dialogue.getText)
    } yield {
      Response().update(_.dialogue := dialogue, _.voice := voice)
    }
  }
}

trait UseVrchService {
  def vrchService: VrchService
}

trait MixinVrchService extends UseVrchService with UseVrchConfig with UseExecutionContext { self =>
  override val vrchService: VrchService = new VrchServiceImpl {
    override def vrchConfig: VrchConfig = self.vrchConfig

    override implicit def context: ExecutionContext = self.context
  }
}
