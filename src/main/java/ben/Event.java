package ben;

/** A task that starts and ends at specific date/times. */
class Event extends Task {
    private final String from;
    private final String to;

    Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    String getTypeIcon() {
        return "E";
    }

    @Override
    String serialize() {
        return super.serialize() + " | " + from + " | " + to;
    }

    @Override
    public String toString() {
        return super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
