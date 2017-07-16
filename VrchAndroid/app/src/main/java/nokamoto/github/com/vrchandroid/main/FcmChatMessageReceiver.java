package nokamoto.github.com.vrchandroid.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import nokamoto.github.com.vrchandroid.fcm.FcmChatMessage;

public class FcmChatMessageReceiver extends BroadcastReceiver {
    private final static String TAG = FcmChatMessageReceiver.class.getSimpleName();

    private MessageListAdapter adapter;

    public FcmChatMessageReceiver(MessageListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "receive: " + intent.getAction());

        if (intent.getAction().equals(FcmChatMessage.INTENT_ACTION)) {
            ChatMessage message = FcmChatMessage.fromIntent(intent).toChatMessage();
            adapter.add(message);

            if (message.getFrom() == WhoAmI.KIRITAN) {
                // load wav
                Log.d(TAG, "not implemented yet.");
            }
        }
    }
}
