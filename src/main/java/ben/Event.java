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
    String extraFields() {
        return " | " + from + " | " + to;
    }

    @Override
    String extraInfo() {
        return " (from: " + from + " to: " + to + ")";
    }
}
