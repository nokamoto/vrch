package nokamoto.github.com.vrchandroid.wav;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WavController {
    private final static String TAG = WavController.class.getSimpleName();
    private final static int MAX_STREAMS = 5;

    private Context context;
    private Map<String, Integer> streamIds;
    private SoundPool pool;

    public WavController(Context context) {
        this.context = context;
        this.streamIds = Collections.synchronizedMap(new HashMap<String, Integer>());

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        pool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(MAX_STREAMS)
                .build();

        pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.i(TAG, String.format("load completed: sampleId=%d, status=%d", sampleId, status));
                soundPool.play(sampleId, 1, 1, 1, 0, 1);
            }
        });
    }

    public void play(String filename, byte[] bytes) {
        Log.i(TAG, "play: " + filename);
        try {
            try(FileOutputStream stream = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                stream.write(bytes);
                stream.flush();
                stream.getFD().sync();
            }

            try(FileInputStream stream = context.openFileInput(filename)) {
                if (streamIds.size() >= MAX_STREAMS) {
                    for (int streamId : streamIds.values()) {
                        pool.unload(streamId);
                    }
                    streamIds.clear();
                }

                int streamId = pool.load(stream.getFD(), 0, bytes.length, 1);
                streamIds.put(filename, streamId);
            }
        } catch(Exception e) {
            Log.e(TAG, "failed to play wav.", e);
        }
    }
}
