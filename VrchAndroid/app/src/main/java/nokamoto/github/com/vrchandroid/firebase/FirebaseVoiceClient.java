package nokamoto.github.com.vrchandroid.firebase;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.ExecutionException;

import nokamoto.github.com.vrchandroid.BuildConfig;
import vrch.VoiceOuterClass;

public class FirebaseVoiceClient {
    private FirebaseStorage storage;
    private final static String FIREBASE_STORAGE_REF = BuildConfig.FIREBASE_STORAGE_REF;
    private final static String AUDIOS = "audios";
    private final static String EXT = ".wav";
    private final static StorageMetadata METADATA = new StorageMetadata.Builder().
            setContentType("audio/wav").build();

    public FirebaseVoiceClient() {
        storage = FirebaseStorage.getInstance();
    }

    private StorageReference audios() {
        StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_REF);
        return storageRef.child(AUDIOS);
    }

    private StorageReference voice(FirebaseMessage message) {
        return audios().child(message.getUuid() + EXT);
    }

    public void write(FirebaseMessage message, VoiceOuterClass.Voice voice)
            throws ExecutionException, InterruptedException {
        UploadTask task = this.voice(message).putBytes(voice.getVoice().toByteArray(), METADATA);
        Tasks.await(task);
    }

    public Task<Uri> uri(FirebaseMessage message) {
        return voice(message).getDownloadUrl();
    }
}
