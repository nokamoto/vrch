package vrch.vr

import vrch.VrServiceGrpc.VrService
import vrch.{Text, Voice}

import scala.concurrent.Future

trait VrServiceImpl extends VrService with UseVrCluster {
  override def talk(request: Text): Future[Voice] = vrCluster.talk(request)
}

trait UseVrService {
  def vrService: VrService
}

trait MixinVrService extends UseVrService with UseVrCluster { self =>
  override val vrService: VrService = new VrServiceImpl {
    override def vrCluster: VrCluster = self.vrCluster
  }
}
