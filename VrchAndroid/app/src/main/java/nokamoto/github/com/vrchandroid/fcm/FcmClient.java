package nokamoto.github.com.vrchandroid.fcm;

import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import nokamoto.github.com.vrchandroid.BuildConfig;

public class FcmClient {
    private final static String TAG = FcmClient.class.getSimpleName();

    private final static String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private final static String FCM_SERVERKEY = BuildConfig.FCM_SERVERKEY;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    public FcmClient() {
        client = new OkHttpClient();
    }

    public void send(String topic, FcmChatMessage message) {
        try {
            Log.i(TAG, "send: " + message);

            JSONObject json = new JSONObject().put("to", "/topics/" + topic).put("notification", message.toJson());

            Request req = new Request.Builder().
                    addHeader("Authorization", "key=" + FCM_SERVERKEY).
                    url(FCM_URL).
                    post(RequestBody.create(JSON, json.toString())).
                    build();

            Response res = client.newCall(req).execute();

            Log.i(TAG, String.format("req=%s, res=%s", req, res));
        } catch (Exception e) {
            Log.e(TAG, "failed to send.", e);
        }
    }
}
