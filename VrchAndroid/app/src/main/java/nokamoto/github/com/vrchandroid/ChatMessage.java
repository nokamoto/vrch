package nokamoto.github.com.vrchandroid;

public class ChatMessage {
    private WhoAmI from;
    private String message;
    private long createdAt;

    ChatMessage(WhoAmI from, String message) {
        this.from = from;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
    }

    public WhoAmI getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return new String("kiritan");
    }
}
