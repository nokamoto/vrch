package nokamoto.github.com.vrchandroid;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class VrchFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = VrchFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "received: " + remoteMessage.getFrom());

        RemoteMessage.Notification n = remoteMessage.getNotification();
        if (n != null) {
            Log.i(TAG, "notification received: not implemented yet.");
        }
    }

}
