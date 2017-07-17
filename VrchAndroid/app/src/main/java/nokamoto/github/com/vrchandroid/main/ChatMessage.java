package nokamoto.github.com.vrchandroid.main;

public class ChatMessage {
    private WhoAmI from;
    private String message;
    private String name;
    private long createdAt;

    private final static String KIRITAN_NAME = "kiritan";

    public ChatMessage(WhoAmI from, String message, String name) {
        this.from = from;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
        this.name = name;
    }

    static ChatMessage kiritan(String message) {
        return new ChatMessage(WhoAmI.KIRITAN, message, KIRITAN_NAME);
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
        return name;
    }
}
