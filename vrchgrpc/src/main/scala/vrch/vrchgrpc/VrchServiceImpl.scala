package vrch.vrchgrpc

import vrch.VrchServiceGrpc.VrchService
import vrch._
import vrch.chgrpc.{ChConfig, ChServiceImpl, UseChConfig, UseChService}
import vrch.grpc.UseExecutionContext
import vrch.vrgrpc.{UseVrCluster, UseVrService, VrCluster, VrServiceImpl}

import scala.concurrent.{ExecutionContext, Future}

trait VrchServiceImpl extends VrchService
  with UseVrchConfig with UseExecutionContext with UseChService with UseVrService {

  override def talk(request: Request): Future[Response] = {
    for {
      dialogue <- chService.talk(request.getDialogue)
      voice <- vrService.talk(dialogue.getText)
    } yield {
      Response().update(_.dialogue := dialogue, _.voice := voice)
    }
  }
}

trait UseVrchService {
  def vrchService: VrchService
}

trait MixinVrchService extends UseVrchService
  with UseVrchConfig with UseExecutionContext with UseVrCluster with UseChConfig { self =>

  override val vrchService: VrchService = new VrchServiceImpl with UseVrService with UseChService {
    override def vrchConfig: VrchConfig = self.vrchConfig

    override implicit def context: ExecutionContext = self.context

    override def vrService: VrServiceGrpc.VrService = new VrServiceImpl {
      override def vrCluster: VrCluster = self.vrCluster
    }

    override def chService: ChServiceGrpc.ChService = new ChServiceImpl {
      override def chConfig: ChConfig = self.chConfig
    }
  }
}
