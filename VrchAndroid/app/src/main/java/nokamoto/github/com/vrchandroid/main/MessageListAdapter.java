package nokamoto.github.com.vrchandroid.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nokamoto.github.com.vrchandroid.R;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final String TAG = MessageListAdapter.class.getSimpleName();

    private Context context;
    private List<ChatMessage> messages;

    public MessageListAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<>();
    }

    public void add(ChatMessage message) {
        messages.add(0, message);
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == WhoAmI.SELF.ordinal()) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == WhoAmI.KIRITAN.ordinal()) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        Log.e(TAG, "unexpected viewType: " + viewType);

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == WhoAmI.SELF.ordinal()) {
            ((SentMessageHolder)holder).bind(messages.get(position));
        } else if (holder.getItemViewType() == WhoAmI.KIRITAN.ordinal()) {
            ((ReceivedMessageHolder)holder).bind(messages.get(position));
        } else {
            Log.e(TAG, "unexpected viewType: " + holder.getItemViewType());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getFrom().ordinal();
    }

    private static String formatDate(long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(date);
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());

            timeText.setText(formatDate(message.getCreatedAt()));

            nameText.setText(message.getName());

            // Insert the profile image from the URL into the ImageView.
            Glide.with(context)
                    .load(R.drawable.kiritan)
                    .asBitmap()
                    .centerCrop()
                    .dontAnimate()
                    .into(new BitmapImageViewTarget(profileImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            //circularBitmapDrawable.setCircular(true);
                            profileImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());

            timeText.setText(formatDate(message.getCreatedAt()));
        }
    }
}
