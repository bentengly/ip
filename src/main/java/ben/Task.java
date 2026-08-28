package ben;

/**
 * Base class for anything Ben is tracking: a description plus a
 * done/not-done status. Subclasses add their own extra fields (a
 * deadline's "by" date, an event's "from"/"to" times) and override
 * {@link #getTypeIcon()} to identify themselves in the list.
 */
abstract class Task {
    private final String description;
    private boolean isDone;

    Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /** Marks this task as done. */
    void markAsDone() {
        isDone = true;
    }

    /** Marks this task as not done. */
    void markAsNotDone() {
        isDone = false;
    }

    /** Returns {@code "X"} if this task is done, or a single space otherwise. */
    String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /** Returns whether this task has been marked done. */
    boolean isDone() {
        return isDone;
    }

    /** Returns the task's description text (without any type or status markers). */
    String getDescription() {
        return description;
    }

    /** One-letter tag identifying the task type: "T", "D", or "E". */
    abstract String getTypeIcon();

    /**
     * Renders this task as one line for the data file, using " | " as the
     * field separator, e.g. {@code T | 1 | read book}. The second field is
     * the done flag (1 = done, 0 = not done). Subclasses append their
     * extra fields.
     */
    String serialize() {
        return getTypeIcon() + " | " + (isDone ? "1" : "0") + " | " + description;
    }

    /**
     * Rebuilds a task from one line produced by {@link #serialize()}.
     *
     * @throws BenException if the line does not match any known format
     */
    static Task deserialize(String line) throws BenException {
        String[] parts = line.split(" \\| ");
        try {
            boolean isDone = parts[1].equals("1");
            Task task;
            switch (parts[0]) {
                case "T":
                    task = new Todo(parts[2]);
                    break;
                case "D":
                    task = new Deadline(parts[2], parts[3]);
                    break;
                case "E":
                    task = new Event(parts[2], parts[3], parts[4]);
                    break;
                default:
                    throw new BenException("Skipping unrecognised saved task: " + line);
            }
            if (isDone) {
                task.markAsDone();
            }
            return task;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new BenException("Skipping corrupted saved task: " + line);
        }
    }

    @Override
    public String toString() {
        return "[" + getTypeIcon() + "][" + getStatusIcon() + "] " + description;
    }
}
