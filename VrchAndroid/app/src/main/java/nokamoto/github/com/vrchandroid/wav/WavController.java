package nokamoto.github.com.vrchandroid.wav;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class WavController {
    private final static String TAG = WavController.class.getSimpleName();

    public final static int STREAM_TYPE = AudioManager.STREAM_MUSIC;

    private Context context;

    public WavController(Context context) {
        this.context = context;
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
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(stream.getFD());

                player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.e(TAG, String.format("error: what=%d, extra=%d", what, extra));
                        mp.stop();
                        mp.release();
                        return false;
                    }
                });

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(TAG, String.format("%s", "completed."));
                        mp.stop();
                        mp.release();
                    }
                });

                player.prepare();

                AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                int vol = audio.getStreamVolume(STREAM_TYPE);
                int max = audio.getStreamMaxVolume(STREAM_TYPE);
                float fVol = (float)vol / max;
                player.setVolume(fVol, fVol);

                player.start();
            }
        } catch(Exception e) {
            Log.e(TAG, "failed to play wav.", e);
        }
    }
}
