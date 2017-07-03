package vrch.grpc

import io.grpc._

class GcpApiKeyInterceptor(apikey: String) extends ClientInterceptor {
  private[this] val header = Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER)

  override def interceptCall[ReqT, RespT](method: MethodDescriptor[ReqT, RespT],
                                          callOptions: CallOptions,
                                          next: Channel): ClientCall[ReqT, RespT] = {
    val call: ClientCall[ReqT, RespT] = next.newCall(method, callOptions)

    new ForwardingClientCall.SimpleForwardingClientCall[ReqT, RespT](call) {
      override def start(responseListener: ClientCall.Listener[RespT], headers: Metadata): Unit = {
        headers.put(header, apikey)
        super.start(responseListener, headers)
      }
    }
  }
}
