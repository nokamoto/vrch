package nokamoto.github.com.vrchandroid.fcm;

import org.json.JSONException;
import org.json.JSONObject;

public class FcmChatMessage {
    private String request;
    private String response;
    private String displayName;

    public FcmChatMessage(String request, String response, String displayName) {
        this.request = request;
        this.response = response;
        this.displayName = displayName;
    }

    JSONObject toJson() throws JSONException {
        return new JSONObject().
                put("title", response).
                put("body", String.format("@%s %s", displayName, request));
    }

    @Override
    public String toString() {
        try {
            return toJson().toString();
        } catch (Exception e) {
            return super.toString();
        }
    }
}
