package nokamoto.github.com.vrchandroid.grpc;

import android.util.Log;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import nokamoto.github.com.vrchandroid.BuildConfig;

public class GrpcUtils {
    private final static String TAG = GrpcUtils.class.getSimpleName();

    private final static String HOST = BuildConfig.GRPC_HOST;
    private final static int PORT = Integer.parseInt(BuildConfig.GRPC_PORT);
    private final static String APIKEY = BuildConfig.GRPC_APIKEY;

    public static ManagedChannel newChannel() {
        Log.i(TAG, String.format("grpc channel: host=%s, port=%d", HOST, PORT));

        return OkHttpChannelBuilder.
                forAddress(HOST, PORT).
                intercept(new GrpcApiKeyInterceptor(APIKEY)).
                usePlaintext(true).
                build();
    }
}
