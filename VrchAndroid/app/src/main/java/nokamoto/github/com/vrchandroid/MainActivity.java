package nokamoto.github.com.vrchandroid;

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
import android.widget.EditText;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import io.grpc.ManagedChannel;
import nokamoto.github.com.vrchandroid.fcm.FcmChatMessage;
import nokamoto.github.com.vrchandroid.fcm.FcmClient;
import nokamoto.github.com.vrchandroid.grpc.GrpcUtils;
import nokamoto.github.com.vrchandroid.main.ChatMessage;
import nokamoto.github.com.vrchandroid.main.FcmChatMessageReceiver;
import nokamoto.github.com.vrchandroid.main.MessageListAdapter;
import nokamoto.github.com.vrchandroid.main.WhoAmI;
import nokamoto.github.com.vrchandroid.wav.WavController;
import vrch.DialogueOuterClass;
import vrch.Services;
import vrch.TextOuterClass;
import vrch.VrchServiceGrpc;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static String LOBBY = "lobby";

    private String context;
    private ManagedChannel channel;
    private FcmClient fcmClient;
    private WavController wav;

    private RecyclerView messageRecycler;
    private MessageListAdapter messageAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = "";

        channel = GrpcUtils.newChannel();

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);

        messageAdapter = new MessageListAdapter(this);

        messageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        messageRecycler.setLayoutManager(layoutManager);
        messageRecycler.setAdapter(messageAdapter);

        wav = new WavController(this);

        FirebaseMessaging.getInstance().subscribeToTopic(LOBBY);

        fcmClient = new FcmClient();

        receiver = new FcmChatMessageReceiver(messageAdapter);

        LocalBroadcastManager.getInstance(getApplicationContext()).
                registerReceiver(receiver, new IntentFilter(FcmChatMessage.INTENT_ACTION));

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    public void sendMessage(View view) throws IOException {
        EditText editText = (EditText) findViewById(R.id.edittext_chatbox);
        String message = editText.getText().toString();

        messageAdapter.add(new ChatMessage(WhoAmI.SELF, message));

        DialogueOuterClass.Dialogue dialogue = DialogueOuterClass.Dialogue.newBuilder().
                setText(TextOuterClass.Text.newBuilder().setText(message)).setContext(context).build();

        Services.Request req = Services.Request.newBuilder().setDialogue(dialogue).build();

        TalkTask task = new TalkTask();
        task.execute(req);
    }

    private class TalkTask extends AsyncTask<Services.Request, Integer, Services.Response> {
        @Override
        protected Services.Response doInBackground(Services.Request... requests) {
            Services.Request req = requests[0];

            fcmClient.send(LOBBY, new FcmChatMessage(WhoAmI.SELF, req.getDialogue().getText().getText()));

            VrchServiceGrpc.VrchServiceBlockingStub stub = VrchServiceGrpc.newBlockingStub(channel);

            Services.Response res = stub.talk(req);
            context = res.getDialogue().getContext();

            fcmClient.send(LOBBY, new FcmChatMessage(WhoAmI.KIRITAN, res.getDialogue().getText().getText()));

            return res;
        }

        @Override
        protected void onPostExecute(Services.Response res) {
            try {
                Log.d(this.toString(), res.toString());

                DialogueOuterClass.Dialogue dialogue = res.getDialogue();

                context = dialogue.getContext();
                messageAdapter.add(new ChatMessage(WhoAmI.KIRITAN, dialogue.getText().getText()));

                String filename = dialogue.getText().getText() + ".wav";
                byte[] bytes = res.getVoice().getVoice().toByteArray();

                wav.play(filename, bytes);
            } catch (Exception e) {
                Log.e(TAG, "failed to handle: " + res, e);
            }
        }
    }
}
