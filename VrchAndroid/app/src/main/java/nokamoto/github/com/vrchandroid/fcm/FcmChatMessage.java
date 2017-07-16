package nokamoto.github.com.vrchandroid.fcm;

import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

import nokamoto.github.com.vrchandroid.main.ChatMessage;
import nokamoto.github.com.vrchandroid.main.WhoAmI;

public class FcmChatMessage {
    private final static UUID app = UUID.randomUUID();
    public final static String INTENT_ACTION = "fch_chat_message";

    private WhoAmI who;
    private String message;
    private String appId;

    private final static String WHO = "who";
    private final static String MESSAGE = "message";
    private final static String APP_ID = "app_id";

    public FcmChatMessage(WhoAmI who, String message) {
        this.who = who;
        this.message = message;
        this.appId = app.toString();
    }

    private FcmChatMessage(WhoAmI who, String message, String appId) {
        this.who = who;
        this.message = message;
        this.appId = appId;
    }

    public static FcmChatMessage fromRemoteMessage(RemoteMessage message) {
        Map<String, String> data = message.getData();
        WhoAmI who = WhoAmI.values()[Integer.parseInt(data.get(WHO))];
        String msg = data.get(MESSAGE);
        String appId = data.get(APP_ID);
        return new FcmChatMessage(who, msg, appId);
    }

    public static FcmChatMessage fromIntent(Intent intent) {
        WhoAmI who = WhoAmI.values()[Integer.parseInt(intent.getStringExtra(WHO))];
        String msg = intent.getStringExtra(MESSAGE);
        String appId = intent.getStringExtra(APP_ID);
        return new FcmChatMessage(who, msg, appId);
    }

    JSONObject toJson() throws JSONException {
        return new JSONObject().
                put(WHO, who.ordinal()).put(MESSAGE, message).put(APP_ID, app.toString());
    }

    public ChatMessage toChatMessage() {
        return new ChatMessage(who, message);
    }

    public boolean isOwn(FcmChatMessage message) {
        return message.appId.equals(app.toString());
    }

    public Intent toIntent() {
        Intent i = new Intent();
        i.setAction(INTENT_ACTION);
        i.putExtra(WHO, who);
        i.putExtra(MESSAGE, message);
        i.putExtra(APP_ID, appId);
        return i;
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
