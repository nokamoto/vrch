package nokamoto.github.com.vrchandroid;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import nokamoto.github.com.vrchandroid.fcm.FcmChatMessage;

public class VrchFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = VrchFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "received: " + remoteMessage.getFrom());

        FcmChatMessage message = FcmChatMessage.fromRemoteMessage(remoteMessage);

        Log.i(TAG, "message=" + message);

        if (!message.isOwn(message)) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(message.toIntent());
        }
    }
}
