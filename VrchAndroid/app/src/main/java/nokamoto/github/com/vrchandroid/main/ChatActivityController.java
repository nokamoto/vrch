package nokamoto.github.com.vrchandroid.main;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;

import io.grpc.ManagedChannel;
import nokamoto.github.com.vrchandroid.AccountPreference;
import nokamoto.github.com.vrchandroid.R;
import nokamoto.github.com.vrchandroid.fcm.FcmChatMessage;
import nokamoto.github.com.vrchandroid.fcm.FcmClient;
import nokamoto.github.com.vrchandroid.grpc.GrpcUtils;
import nokamoto.github.com.vrchandroid.wav.WavController;
import vrch.DialogueOuterClass;
import vrch.Services;
import vrch.TextOuterClass;
import vrch.VrchServiceGrpc;

public class ChatActivityController {
    private static final String TAG = ChatActivityController.class.getSimpleName();
    private final static String LOBBY = "lobby";

    private String context;
    private ManagedChannel channel;
    private FcmClient fcmClient;
    private WavController wav;

    private RecyclerView messageRecycler;
    private MessageListAdapter messageAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private AppCompatActivity activity;
    private AccountPreference account;

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

            messageAdapter.add(new ChatMessage(WhoAmI.SELF, message, account.displayName()));

            DialogueOuterClass.Dialogue dialogue = DialogueOuterClass.Dialogue.newBuilder().
                    setText(TextOuterClass.Text.newBuilder().setText(message)).setContext(context).build();

            Services.Request req = Services.Request.newBuilder().setDialogue(dialogue).build();

            TalkTask task = new TalkTask();
            task.execute(req);
        }
    }

    private class TalkTask extends AsyncTask<Services.Request, Integer, Services.Response> {
        private Services.Response call(Services.Request req) {
            try {
                VrchServiceGrpc.VrchServiceBlockingStub stub = VrchServiceGrpc.newBlockingStub(channel);
                return stub.talk(req);
            } catch (Exception e) {
                Log.e(TAG, "grpc call failed.", e);
            }
            return null;
        }

        @Override
        protected Services.Response doInBackground(Services.Request... requests) {
            Services.Request req = requests[0];

            Services.Response res = call(req);

            if (res != null) {
                String request = req.getDialogue().getText().getText();
                String response = res.getDialogue().getText().getText();
                String displayName = account.displayName();
                fcmClient.send(LOBBY, new FcmChatMessage(request, response, displayName));
            }

            return res;
        }

        @Override
        protected void onPostExecute(Services.Response res) {
            try {
                Button send = (Button)activity.findViewById(R.id.button_chatbox_send);
                send.setEnabled(true);

                if (res != null) {
                    Log.d(this.toString(), res.toString());

                    DialogueOuterClass.Dialogue dialogue = res.getDialogue();

                    context = dialogue.getContext();

                    messageAdapter.add(ChatMessage.kiritan(dialogue.getText().getText()));

                    String filename = dialogue.getText().getText() + ".wav";
                    byte[] bytes = res.getVoice().getVoice().toByteArray();

                    wav.play(filename, bytes);
                } else {
                    messageAdapter.add(ChatMessage.kiritan("Oops! Something went wrong."));
                }

            } catch (Exception e) {
                Log.e(TAG, "failed to handle: " + res, e);
            }
        }
    }
}
