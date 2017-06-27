package vrch.vr

import vrch.{Text, Voice}
import vrch.VrServiceGrpc.VrService

import scala.concurrent.Future

class VrServiceImpl extends VrService {
  override def talk(request: Text): Future[Voice] = Future.successful(Voice())
}

trait UseVrService {
  def vrService: VrService
}

trait MixinVrService extends UseVrService {
  override val vrService: VrService = new VrServiceImpl
}
