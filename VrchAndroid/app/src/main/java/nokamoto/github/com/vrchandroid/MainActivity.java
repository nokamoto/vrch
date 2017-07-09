package nokamoto.github.com.vrchandroid;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import vrch.DialogueOuterClass;
import vrch.Services;
import vrch.TextOuterClass;
import vrch.VrchServiceGrpc;

public class MainActivity extends AppCompatActivity {
    private final static int MAX_STREAMS = 5;

    private static ManagedChannel channel;
    private static String context;
    private final static String host = BuildConfig.GRPC_HOST;
    private final static String apikey = BuildConfig.GRPC_APIKEY;

    private AudioAttributes audioAttributes;
    private SoundPool sounds;
    private Map<String, Integer> streamIds = Collections.synchronizedMap(new HashMap<String, Integer>());

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = "";
        Log.d(this.toString(), String.format("%s %s", host, apikey));

        channel = OkHttpChannelBuilder.
                forAddress(host, 80).
                intercept(new GrpcApiKeyInterceptor(apikey)).
                usePlaintext(true).
                build();

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        mMessageAdapter = new MessageListAdapter(this);
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageRecycler.setLayoutManager(mLayoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        sounds = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(MAX_STREAMS)
                .build();

        sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d(this.toString(), String.format("onLoadComplete: sampleId=%d, status=%d", sampleId, status));
                soundPool.play(sampleId, 1, 1, 1, 0, 1);
            }
        });
    }

    public void sendMessage(View view) throws IOException {
        EditText editText = (EditText) findViewById(R.id.edittext_chatbox);
        String message = editText.getText().toString();

        Log.d(this.toString(), message);

        mMessageAdapter.add(new ChatMessage(WhoAmI.SELF, message));

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

            VrchServiceGrpc.VrchServiceBlockingStub stub = VrchServiceGrpc.newBlockingStub(channel);

            Services.Response res = stub.talk(req);
            context = res.getDialogue().getContext();

            return res;
        }

        @Override
        protected void onPostExecute(Services.Response res) {
            try {
                Log.d(this.toString(), res.toString());

                DialogueOuterClass.Dialogue dialogue = res.getDialogue();
                String filename = dialogue.getText().getText() + ".wav";
                byte[] bytes = res.getVoice().getVoice().toByteArray();

                try(FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE)) {
                    stream.write(bytes);
                    stream.flush();
                    stream.getFD().sync();
                }

                try(FileInputStream stream = openFileInput(filename)) {
                    if (streamIds.size() >= MAX_STREAMS) {
                        for (int streamId : streamIds.values()) {
                            sounds.unload(streamId);
                        }
                        streamIds.clear();
                    }

                    int streamId = sounds.load(stream.getFD(), 0, bytes.length, 1);
                    streamIds.put(filename, streamId);
                    Log.d(this.toString(), String.format("load %s %d", filename, streamId));
                }

                context = dialogue.getContext();
                mMessageAdapter.add(new ChatMessage(WhoAmI.KIRITAN, dialogue.getText().getText()));
            } catch (Exception e) {
                Log.e(this.toString(), "onPoseExecute", e);
            }
        }
    }
}
