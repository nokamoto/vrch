package nokamoto.github.com.vrchandroid.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

class GrpcApiKeyInterceptor implements ClientInterceptor {
    private String apikey = null;

    static final private Metadata.Key<String> header = Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);

    GrpcApiKeyInterceptor(String apikey) {
        this.apikey = apikey;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(header, apikey);
                super.start(responseListener, headers);
            }
        };
    }
}
