package nokamoto.github.com.vrchandroid.main;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Optional;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.grpc.ManagedChannel;
import nokamoto.github.com.vrchandroid.AccountPreference;
import nokamoto.github.com.vrchandroid.BuildConfig;
import nokamoto.github.com.vrchandroid.R;
import nokamoto.github.com.vrchandroid.firebase.FcmChatMessage;
import nokamoto.github.com.vrchandroid.firebase.FcmClient;
import nokamoto.github.com.vrchandroid.firebase.FirebaseMessage;
import nokamoto.github.com.vrchandroid.grpc.GrpcUtils;
import nokamoto.github.com.vrchandroid.wav.WavController;
import vrch.DialogueOuterClass;
import vrch.Services;
import vrch.TextOuterClass;
import vrch.VrchServiceGrpc;

public class ChatActivityController {
    private static final String TAG = ChatActivityController.class.getSimpleName();
    private final static String LOBBY = "lobby";
    private final static String MESSAGES = "messages";
    private final static String FIREBASE_STORAGE_REF = BuildConfig.FIREBASE_STORAGE_REF;
    private final static String AUDIOS = "audios";

    private String context;
    private ManagedChannel channel;
    private FcmClient fcmClient;
    private WavController wav;

    private RecyclerView messageRecycler;
    private MessageListAdapter messageAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private AppCompatActivity activity;
    private AccountPreference account;

    private FirebaseDatabase database;
    private ChildEventListener databaseListener;

    private FirebaseStorage storage;

    public ChatActivityController(AppCompatActivity activity, AccountPreference account) {
        this.activity = activity;
        this.account = account;
    }

    public void onCreate(Bundle savedInstanceState) {
        activity.setVolumeControlStream(WavController.STREAM_TYPE);

        context = "";

        channel = GrpcUtils.newChannel();

        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true);

        messageAdapter = new MessageListAdapter(activity);

        messageRecycler = (RecyclerView) activity.findViewById(R.id.reyclerview_message_list);
        messageRecycler.setLayoutManager(layoutManager);
        messageRecycler.setAdapter(messageAdapter);

        wav = new WavController(activity);

        FirebaseMessaging.getInstance().subscribeToTopic(LOBBY);

        fcmClient = new FcmClient();

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity);

        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance();
    }

    public void onStart() {
        final long startAt = System.currentTimeMillis();

        Query q = database.getReference().child(MESSAGES).child(LOBBY).limitToLast(100);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    Optional<FirebaseMessage> data = FirebaseMessage.fromSnapshot(children);
                    Log.i(TAG, "query: " + data);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "canceled.", databaseError.toException());
            }
        });

        databaseListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Optional<FirebaseMessage> data = FirebaseMessage.fromSnapshot(dataSnapshot);
                if (data.isPresent()) {
                    Log.i(TAG, "added: " + data);
                    FirebaseMessage message = data.get();
                    messageAdapter.add(message);

                    if (message.getWho() == WhoAmI.KIRITAN && message.getCreatedAt() >= startAt) {
                        StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_REF);
                        StorageReference audios = storageRef.child(AUDIOS);
                        StorageReference audio = audios.child(message.getUuid() + ".wav");
                        audio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                wav.play(uri);
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "canceled.", databaseError.toException());
            }
        };

        database.getReference().child(MESSAGES).child(LOBBY).addChildEventListener(databaseListener);
    }

    public void onStop() {
        if (databaseListener != null) {
            database.getReference().child(MESSAGES).child(LOBBY).removeEventListener(databaseListener);
        }
    }

    public void onDestroy() {
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) activity.findViewById(R.id.edittext_chatbox);
        String message = editText.getText().toString();
        editText.getText().clear();

        if (!message.isEmpty()) {
            Button send = (Button) activity.findViewById(R.id.button_chatbox_send);
            send.setEnabled(false);

            DialogueOuterClass.Dialogue dialogue = DialogueOuterClass.Dialogue.newBuilder().
                    setText(TextOuterClass.Text.newBuilder().setText(message)).setContext(context).build();

            Services.Request req = Services.Request.newBuilder().setDialogue(dialogue).build();

            TalkTask task = new TalkTask();
            task.execute(req);
        }
    }

    private class TalkTask extends AsyncTask<Services.Request, Integer, FirebaseMessage> {
        private Services.Response call(Services.Request req) {
            try {
                VrchServiceGrpc.VrchServiceBlockingStub stub = VrchServiceGrpc.newBlockingStub(channel);
                return stub.talk(req);
            } catch (Exception e) {
                Log.e(TAG, "grpc call failed.", e);
            }
            return null;
        }

        private FirebaseMessage toMessage(Services.Request req) {
            WhoAmI who = WhoAmI.SELF;
            String displayName = account.displayName();
            String uid = account.uid();
            String msg = req.getDialogue().getText().getText();
            return new FirebaseMessage(who, displayName, uid, msg);
        }

        private FirebaseMessage toMessage(Services.Response res) {
            String msg = res.getDialogue().getText().getText();
            return FirebaseMessage.kiritan(msg);
        }

        private void writeDatabase(FirebaseMessage message) {
            try {
                String pushed = database.getReference().child(MESSAGES).child(LOBBY).push().getKey();
                Map<String, Object> childUpdates = new HashMap<>();
                String key = String.format("/%s/%s/%s", MESSAGES, LOBBY, pushed);
                childUpdates.put(key, message.toMap());

                Tasks.await(database.getReference().updateChildren(childUpdates));

                Log.i(TAG, "write realtime database: " + key + " " + message);
            } catch (Exception e) {
                Log.e(TAG, "realtime database failed: " + message, e);
            }
        }

        @Override
        protected FirebaseMessage doInBackground(Services.Request... requests) {
            Services.Request req = requests[0];

            writeDatabase(toMessage(req));

            Services.Response res = call(req);

            if (res != null) {
                FirebaseMessage msg = toMessage(res);
                StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_REF);
                StorageReference audios = storageRef.child(AUDIOS);

                StorageMetadata metadata = new StorageMetadata.Builder().
                        setContentType("audio/wav").build();

                StorageReference audio = audios.child(msg.getUuid() + ".wav");

                try {
                    Tasks.await(audio.putBytes(res.getVoice().getVoice().toByteArray(), metadata));

                    writeDatabase(msg);

                    String request = req.getDialogue().getText().getText();
                    String response = res.getDialogue().getText().getText();
                    String displayName = account.displayName();
                    FcmChatMessage fcm = new FcmChatMessage(request, response, displayName);
                    fcmClient.send(LOBBY, fcm);
                } catch (Exception e) {
                    Log.e(TAG, "storage failed.", e);
                    return null;
                }

                return msg;
            }

            return null;
        }

        @Override
        protected void onPostExecute(FirebaseMessage res) {
            try {
                Button send = (Button)activity.findViewById(R.id.button_chatbox_send);
                send.setEnabled(true);

                if (res != null) {
                    Log.d(this.toString(), res.toString());
                } else {
                    messageAdapter.add(FirebaseMessage.kiritan("Oops! Something went wrong."));
                }

            } catch (Exception e) {
                Log.e(TAG, "failed to handle: " + res, e);
            }
        }
    }
}
