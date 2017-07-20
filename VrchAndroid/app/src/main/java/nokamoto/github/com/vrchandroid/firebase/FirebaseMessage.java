package nokamoto.github.com.vrchandroid.firebase;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import nokamoto.github.com.vrchandroid.main.Platform;
import nokamoto.github.com.vrchandroid.main.WhoAmI;

public class FirebaseMessage {
    private final static String TAG = FirebaseMessage.class.getSimpleName();

    private WhoAmI who;
    private String displayName;
    private String uid;
    private String message;
    private String uuid;
    private long createdAt;
    private Platform platform;

    private final static String WHO = "who";
    private final static String DISPLAY_NAME = "display_name";
    private final static String UID = "uid";
    private final static String MESSAGE = "message";
    private final static String UUID = "uuid";
    final static String CREATED_AT = "created_at";
    private final static String PLATFORM = "platform";

    private FirebaseMessage(WhoAmI who, String displayName, String uid, String message, String uuid, long createdAt, Platform platform) {
        this.who = who;
        this.displayName = displayName;
        this.uid = uid;
        this.message = message;
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.platform = platform;
    }

    public FirebaseMessage(WhoAmI who, String displayName, String uid, String message) {
        this(who, displayName, uid, message, java.util.UUID.randomUUID().toString(), 0, Platform.ANDROID);
    }

    public static FirebaseMessage kiritan(String message) {
        return new FirebaseMessage(WhoAmI.KIRITAN, "", "", message, java.util.UUID.randomUUID().toString(), System.currentTimeMillis(), Platform.ANDROID);
    }

    static Optional<FirebaseMessage> fromSnapshot(DataSnapshot snapshot) {
        try {
            Log.i(TAG, snapshot.getKey() + " " + snapshot.getValue());
            int who = Integer.parseInt((String) snapshot.child(WHO).getValue());
            String displayName = (String)snapshot.child(DISPLAY_NAME).getValue();
            String uid = (String)snapshot.child(UID).getValue();
            String message = (String)snapshot.child(MESSAGE).getValue();
            String uuid = (String)snapshot.child(UUID).getValue();
            long createdAt = (long)snapshot.child(CREATED_AT).getValue();
            int platform = Integer.parseInt((String)snapshot.child(PLATFORM).getValue());
            return Optional.of(new FirebaseMessage(WhoAmI.values()[who], displayName, uid, message, uuid, createdAt, Platform.values()[platform]));
        } catch (Exception e) {
            Log.e(TAG, "from snapshot error", e);
            return Optional.absent();
        }
    }

    Map<String, Object> toMap() {
        HashMap<String, Object> m = new HashMap<>();
        m.put(WHO, String.valueOf(who.ordinal()));
        m.put(DISPLAY_NAME, displayName);
        m.put(UID, uid);
        m.put(MESSAGE, message);
        m.put(UUID, uuid);
        m.put(CREATED_AT, ServerValue.TIMESTAMP);
        m.put(PLATFORM, String.valueOf(platform.ordinal()));
        return m;
    }

    @Override
    public String toString() {
        return new JSONObject(toMap()).toString();
    }

    public WhoAmI getWho() {
        return who;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUid() {
        return uid;
    }

    public String getMessage() {
        return message;
    }

    public String getUuid() {
        return uuid;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
