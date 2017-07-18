package nokamoto.github.com.vrchandroid.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Optional;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FirebaseMessageClient {
    private final static String TAG = FirebaseMessageClient.class.getSimpleName();
    private final static String MESSAGES = "messages";

    private FirebaseDatabase database;
    private ChildEventListener listener;

    private String room;
    private String p;

    public FirebaseMessageClient(String room) {
        this.room = room;
        this.database = FirebaseDatabase.getInstance();
        p = "#" + room + " ";
    }

    private DatabaseReference roomRef() {
        return database.getReference().child(MESSAGES).child(room);
    }

    public interface MessageEventListener {
        void onAdded(FirebaseMessage message);
    }

    public void onStart(final MessageEventListener listener) {
        this.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Optional<FirebaseMessage> data = FirebaseMessage.fromSnapshot(dataSnapshot);
                if (data.isPresent()) {
                    listener.onAdded(data.get());
                } else {
                    Log.e(TAG, p + "unexpected data snapshot: " + dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, p + "canceled.", databaseError.toException());
            }
        };

        roomRef().
                orderByChild(FirebaseMessage.CREATED_AT).
                startAt(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)).
                addChildEventListener(this.listener);
    }

    public void onStop() {
        if (listener != null) {
            roomRef().removeEventListener(listener);
        }
    }

    public void write(FirebaseMessage message) throws ExecutionException, InterruptedException {
        Log.i(TAG, p + "write: " + message);

        String pushed = roomRef().push().getKey();
        Map<String, Object> updates = new HashMap<>();
        updates.put(pushed, message.toMap());

        Tasks.await(roomRef().updateChildren(updates));
    }
}
