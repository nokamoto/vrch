package vrch.vrgrpc

import com.google.protobuf.empty.Empty
import io.grpc.stub.StreamObserver
import vrch.VrClusterServiceGrpc.VrClusterService
import vrch.{ClusterInfo, Incoming, Outgoing}

import scala.concurrent.Future

trait VrClusterServiceImpl extends VrClusterService with UseVrCluster {
  override def join(responseObserver: StreamObserver[Outgoing]): StreamObserver[Incoming] = {
    println(s"join($responseObserver)")
    vrCluster.join(responseObserver)
  }

  override def info(request: Empty): Future[ClusterInfo] = vrCluster.info
}

trait UseVrClusterService {
  def vrClusterService: VrClusterService
}

trait MixinVrClusterService extends UseVrClusterService with UseVrCluster { self =>
  override val vrClusterService: VrClusterService = new VrClusterServiceImpl {
    override def vrCluster: VrCluster = self.vrCluster
  }
}
