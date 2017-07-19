package nokamoto.github.com.vrchandroid.firebase;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.firebase.database.DataSnapshot;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseRoom {
    private final static String TAG = FirebaseRoom.class.getSimpleName();

    private String context;

    private final static String CONTEXT = "context";

    public FirebaseRoom(String context) {
        this.context = context;
    }

    static Optional<FirebaseRoom> fromSnapshot(DataSnapshot snapshot) {
        try {
            if (snapshot.exists()) {
                Log.i(TAG, snapshot.getKey() + " " + snapshot.getValue());
                String context = (String)snapshot.child(CONTEXT).getValue();
                return Optional.of(new FirebaseRoom(context));
            } else {
                return Optional.absent();
            }
        } catch (Exception e) {
            Log.e(TAG, "from snapshot error", e);
            return Optional.absent();
        }
    }

    Map<String, Object> toMap() {
        HashMap<String, Object> m = new HashMap<>();
        m.put(CONTEXT, context);
        return m;
    }

    @Override
    public String toString() {
        return new JSONObject(toMap()).toString();
    }

    public String getContext() {
        return context;
    }
}
