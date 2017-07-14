package nokamoto.github.com.vrchandroid.main;

public class ChatMessage {
    private WhoAmI from;
    private String message;
    private long createdAt;

    private final static String NAME = "kiritan";

    public ChatMessage(WhoAmI from, String message) {
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
        return NAME;
    }
}
