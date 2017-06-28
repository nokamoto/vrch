package vrch.vrgrpc

import io.grpc.stub.StreamObserver
import vrch.VrClusterServiceGrpc.VrClusterService
import vrch.{Incoming, Outgoing}

trait VrClusterServiceImpl extends VrClusterService with UseVrCluster {
  override def join(responseObserver: StreamObserver[Outgoing]): StreamObserver[Incoming] = {
    vrCluster.join(responseObserver)
  }
}

trait UseVrClusterService {
  def vrClusterService: VrClusterService
}

trait MixinVrClusterService extends UseVrClusterService with UseVrCluster { self =>
  override val vrClusterService: VrClusterService = new VrClusterServiceImpl {
    override def vrCluster: VrCluster = self.vrCluster
  }
}
