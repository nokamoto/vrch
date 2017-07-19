package nokamoto.github.com.vrchandroid.main;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Optional;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import io.grpc.ManagedChannel;
import nokamoto.github.com.vrchandroid.AccountPreference;
import nokamoto.github.com.vrchandroid.BuildConfig;
import nokamoto.github.com.vrchandroid.R;
import nokamoto.github.com.vrchandroid.firebase.FcmChatMessage;
import nokamoto.github.com.vrchandroid.firebase.FcmClient;
import nokamoto.github.com.vrchandroid.firebase.FirebaseMessage;
import nokamoto.github.com.vrchandroid.firebase.FirebaseMessageClient;
import nokamoto.github.com.vrchandroid.firebase.FirebaseRoom;
import nokamoto.github.com.vrchandroid.firebase.FirebaseVoiceClient;
import nokamoto.github.com.vrchandroid.grpc.GrpcUtils;
import nokamoto.github.com.vrchandroid.wav.WavController;
import vrch.DialogueOuterClass;
import vrch.Services;
import vrch.TextOuterClass;
import vrch.VrchServiceGrpc;

public class ChatActivityController {
    private static final String TAG = ChatActivityController.class.getSimpleName();
    public final static String LOBBY = "lobby";

    private FirebaseRoom room;
    private ManagedChannel channel;
    private FcmClient fcmClient;
    private WavController wav;

    private RecyclerView messageRecycler;
    private MessageListAdapter messageAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private AppCompatActivity activity;
    private AccountPreference account;

    private FirebaseVoiceClient voiceClient;

    private FirebaseMessageClient messageClient;

    public ChatActivityController(AppCompatActivity activity, AccountPreference account) {
        this.activity = activity;
        this.account = account;
    }

    public void onCreate(Bundle savedInstanceState) {
        activity.setVolumeControlStream(WavController.STREAM_TYPE);

        channel = GrpcUtils.newChannel();

        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true);

        messageAdapter = new MessageListAdapter(activity);

        messageRecycler = (RecyclerView) activity.findViewById(R.id.reyclerview_message_list);
        messageRecycler.setLayoutManager(layoutManager);
        messageRecycler.setAdapter(messageAdapter);

        wav = new WavController(activity);

        fcmClient = new FcmClient();

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity);

        voiceClient = new FirebaseVoiceClient();

        messageClient = new FirebaseMessageClient(LOBBY);
    }

    public void onStart() {
        final long startAt = System.currentTimeMillis();

        sendEnabled(false);

        messageClient.onStart(new FirebaseMessageClient.MessageEventListener() {
            @Override
            public void onAdded(final FirebaseMessage message) {
                Log.i(TAG, "added: " + message);

                if (message.getWho() == WhoAmI.KIRITAN && message.getCreatedAt() >= startAt) {
                    voiceClient.uri(message).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            try {
                                wav.play(uri);
                            } catch (Exception e) {
                                Log.e(TAG, "failed to play voice: " + message, e);
                            } finally {
                                messageAdapter.add(message);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            messageAdapter.add(message);
                        }
                    });
                } else {
                    messageAdapter.add(message);
                }
            }
        }, new FirebaseMessageClient.RoomValueListener() {
            @Override
            public void onValue(FirebaseRoom r) {
                room = r;
                sendEnabled(true);
            }

            @Override
            public void onCancelled() {
                // nop: may be crashed.
            }
        });
    }

    public void onStop() {
        messageClient.onStop();
    }

    public void onDestroy() {
    }

    public void sendEnabled(Boolean value) {
        Button send = (Button) activity.findViewById(R.id.button_chatbox_send);
        send.setEnabled(value);
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) activity.findViewById(R.id.edittext_chatbox);
        String message = editText.getText().toString();
        editText.getText().clear();

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (!message.isEmpty()) {
            sendEnabled(false);

            String context = "";
            if (room != null) {
                context = room.getContext();
            }

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

        private void callFcm(Services.Request req, Services.Response res) {
            String request = req.getDialogue().getText().getText();
            String response = res.getDialogue().getText().getText();
            String displayName = account.displayName();
            FcmChatMessage fcm = new FcmChatMessage(request, response, displayName);
            fcmClient.send(LOBBY, fcm);
        }

        @Override
        protected FirebaseMessage doInBackground(Services.Request... requests) {
            Services.Request req = requests[0];

            Services.Response res = call(req);

            if (res != null) {
                try {
                    if (room == null) {
                        FirebaseRoom r = new FirebaseRoom(res.getDialogue().getContext());
                        messageClient.write(r);
                        room = r;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "failed to write room:" + req, e);
                    return null;
                }

                try {
                    messageClient.write(toMessage(req));
                } catch(Exception e) {
                    Log.e(TAG, "failed to write request message:" + req, e);
                    return null;
                }

                FirebaseMessage message = toMessage(res);

                try {
                    voiceClient.write(message, res.getVoice());
                    messageClient.write(message);
                } catch (Exception e) {
                    Log.e(TAG, "failed to write response message: " + message, e);
                    return null;
                }

                callFcm(req, res);

                return message;
            }

            return null;
        }

        @Override
        protected void onPostExecute(FirebaseMessage res) {
            sendEnabled(true);
            if (res == null) {
                Log.i(TAG, "Oops! Something went wrong.");
                messageAdapter.add(FirebaseMessage.kiritan("Oops! Something went wrong."));
            }
        }
    }
}
