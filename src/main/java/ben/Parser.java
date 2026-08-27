package ben;

/**
 * Turns a raw line of user input into something the main loop can act on:
 * the {@link CommandWord}, its argument string, and (for the commands
 * that need it) a fully-built {@link Task} or a validated task number.
 * <p>
 * A-MoreOOP: extracted from the main class so that "making sense of the
 * user command" lives in one place. All methods are static because a
 * parser has no state of its own.
 */
class Parser {
    private Parser() {
        // Utility class: not meant to be instantiated.
    }

    /** Returns the command keyword of the given line (UNKNOWN if unrecognised). */
    static CommandWord commandWord(String input) {
        return CommandWord.fromString(input.split(" ", 2)[0]);
    }

    /** Returns everything after the first word, trimmed (empty if there is nothing). */
    static String args(String input) {
        String[] split = input.split(" ", 2);
        return split.length > 1 ? split[1].trim() : "";
    }

    /**
     * Builds a {@link Deadline} from the argument string of a "deadline"
     * command, e.g. {@code return book /by 2019-12-02}.
     */
    static Deadline parseDeadline(String args) throws BenException {
        if (args.isEmpty()) {
            throw new BenException("The description of a deadline cannot be empty.");
        }
        String[] parts = args.split(" /by ", 2);
        String description = parts[0].trim();
        if (description.isEmpty()) {
            throw new BenException("The description of a deadline cannot be empty.");
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new BenException("A deadline needs a \"/by\" date/time, e.g. \"deadline return book /by 2019-12-02\".");
        }
        return new Deadline(description, parts[1].trim());
    }

    /**
     * Builds an {@link Event} from the argument string of an "event"
     * command, e.g. {@code meeting /from Mon 2pm /to 4pm}.
     */
    static Event parseEvent(String args) throws BenException {
        if (args.isEmpty()) {
            throw new BenException("The description of an event cannot be empty.");
        }
        String[] fromSplit = args.split(" /from ", 2);
        String description = fromSplit[0].trim();
        if (description.isEmpty()) {
            throw new BenException("The description of an event cannot be empty.");
        }
        if (fromSplit.length < 2 || fromSplit[1].trim().isEmpty()) {
            throw new BenException(
                    "An event needs a \"/from\" and \"/to\" time, e.g. \"event meeting /from Mon 2pm /to 4pm\".");
        }
        String[] toSplit = fromSplit[1].split(" /to ", 2);
        String from = toSplit[0].trim();
        if (from.isEmpty() || toSplit.length < 2 || toSplit[1].trim().isEmpty()) {
            throw new BenException(
                    "An event needs a \"/from\" and \"/to\" time, e.g. \"event meeting /from Mon 2pm /to 4pm\".");
        }
        return new Event(description, from, toSplit[1].trim());
    }

    /**
     * Parses a 1-based task number given as text, throwing a
     * {@link BenException} that names the offending command if it is
     * missing or non-numeric. Range-checking is left to {@link TaskList}.
     */
    static int parseIndex(String indexText, String commandName) throws BenException {
        try {
            return Integer.parseInt(indexText.trim());
        } catch (NumberFormatException e) {
            throw new BenException("\"" + commandName + "\" needs a task number, e.g. \"" + commandName + " 2\".");
        }
    }
}
