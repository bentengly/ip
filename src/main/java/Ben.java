import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * A-Enums: command words are now represented by the {@link CommandWord}
 * enum instead of a chain of string comparisons, so dispatching on the
 * command is a type-checked switch rather than repeated
 * {@code .equals}/{@code .startsWith} calls. (Task types stay as the
 * {@link Task} class hierarchy from Level-4 — that's the better fit for
 * type-specific behaviour than an enum would be.)
 * <p>
 * Level-7: the task list is now persisted to disk (see {@link Storage}).
 * It is loaded once at start-up and saved again after every command that
 * can change the list, so the data survives across runs.
 */
public class Ben {
    private static final String LINE = "____________________________________________________________";

    /**
     * Relative path (from the project root) of the data file. Kept
     * relative and built with {@link Path} so it behaves the same on any
     * OS and on any machine the chatbot is copied to.
     */
    private static final String DATA_FILE = "data/ben.txt";

    public static void main(String[] args) {
        String logo = " ____              \n"
                + "|  _ \\ ___ _ __    \n"
                + "| |_) / _ \\ '_ \\   \n"
                + "|  _ <  __/ | | |  \n"
                + "|_| \\_\\___|_| |_|  \n";
        System.out.println(logo);

        printBoxed("Hello! I'm Ben\nWhat can I do for you?");

        Storage storage = new Storage(DATA_FILE);
        List<Task> tasks;
        try {
            tasks = storage.load();
        } catch (BenException e) {
            // Corrupted or unreadable data file: start empty rather than crash.
            printBoxed(e.getMessage());
            tasks = new ArrayList<>();
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                printBoxed("Bye. Hope to see you again soon!");
                break;
            }
            try {
                String reply = handleCommand(input, tasks);
                storage.save(tasks);
                printBoxed(reply);
            } catch (BenException e) {
                printBoxed(e.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * The recognized command keywords, used to dispatch each line of
     * input via a {@code switch} instead of a chain of string checks.
     */
    private enum CommandWord {
        LIST, MARK, UNMARK, DELETE, TODO, DEADLINE, EVENT, BYE, UNKNOWN;

        /** Maps the first word of a line of input to a {@link CommandWord}. */
        static CommandWord fromString(String word) {
            try {
                return CommandWord.valueOf(word.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    /**
     * Dispatches a single (non-"bye") line of input to the right handler
     * and returns the message to display. Throws {@link BenException} for
     * any recognized-but-invalid or unrecognized command.
     */
    private static String handleCommand(String input, List<Task> tasks) throws BenException {
        String[] split = input.split(" ", 2);
        CommandWord command = CommandWord.fromString(split[0]);
        String args = split.length > 1 ? split[1].trim() : "";

        switch (command) {
        case LIST:
            return formatList(tasks);
        case MARK:
            return setDone(tasks, args, true);
        case UNMARK:
            return setDone(tasks, args, false);
        case DELETE:
            return deleteTask(tasks, args);
        case TODO:
            if (args.isEmpty()) {
                throw new BenException("The description of a todo cannot be empty.");
            }
            return addTask(tasks, new Todo(args));
        case DEADLINE:
            return addTask(tasks, parseDeadline(args));
        case EVENT:
            return addTask(tasks, parseEvent(args));
        case BYE:
        case UNKNOWN:
        default:
            throw new BenException("I'm sorry, but I don't know what that means :-(");
        }
    }

    private static Deadline parseDeadline(String args) throws BenException {
        if (args.isEmpty()) {
            throw new BenException("The description of a deadline cannot be empty.");
        }
        String[] parts = args.split(" /by ", 2);
        String description = parts[0].trim();
        if (description.isEmpty()) {
            throw new BenException("The description of a deadline cannot be empty.");
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new BenException("A deadline needs a \"/by\" date/time, e.g. \"deadline return book /by Sunday\".");
        }
        return new Deadline(description, parts[1].trim());
    }

    private static Event parseEvent(String args) throws BenException {
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
     * Adds the given task to the list and returns the confirmation
     * message: "Got it. I've added this task: ... Now you have N tasks
     * in the list."
     */
    private static String addTask(List<Task> tasks, Task task) {
        tasks.add(task);
        return "Got it. I've added this task:\n  " + task
                + "\nNow you have " + tasks.size() + " task" + (tasks.size() == 1 ? "" : "s") + " in the list.";
    }

    /**
     * Builds the numbered listing of all stored tasks, one per line, e.g.
     * "Here are the tasks in your list:\n1.[T][X] read book".
     */
    private static String formatList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "Here are the tasks in your list:\n(no tasks yet)";
        }
        StringBuilder sb = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append("\n").append(i + 1).append(".").append(tasks.get(i));
        }
        return sb.toString();
    }

    /**
     * Marks (or unmarks) the task at the given 1-based index (as text),
     * and returns the confirmation message to show the user.
     */
    private static String setDone(List<Task> tasks, String indexText, boolean done) throws BenException {
        String commandName = done ? "mark" : "unmark";
        Task task = tasks.get(resolveIndex(tasks, indexText, commandName) - 1);
        if (done) {
            task.markAsDone();
            return "Nice! I've marked this task as done:\n  " + task;
        } else {
            task.markAsNotDone();
            return "OK, I've marked this task as not done yet:\n  " + task;
        }
    }

    /**
     * Removes the task at the given 1-based index (as text) and returns
     * the confirmation message: "Noted. I've removed this task: ... Now
     * you have N tasks in the list."
     */
    private static String deleteTask(List<Task> tasks, String indexText) throws BenException {
        int index = resolveIndex(tasks, indexText, "delete");
        Task removed = tasks.remove(index - 1);
        return "Noted. I've removed this task:\n  " + removed
                + "\nNow you have " + tasks.size() + " task" + (tasks.size() == 1 ? "" : "s") + " in the list.";
    }

    /**
     * Parses and validates a 1-based task index given as text, throwing a
     * {@link BenException} with a message naming the offending command if
     * it's missing, non-numeric, or out of range.
     */
    private static int resolveIndex(List<Task> tasks, String indexText, String commandName) throws BenException {
        int index;
        try {
            index = Integer.parseInt(indexText);
        } catch (NumberFormatException e) {
            throw new BenException("\"" + commandName + "\" needs a task number, e.g. \"" + commandName + " 2\".");
        }
        if (index < 1 || index > tasks.size()) {
            throw new BenException("There is no task number " + indexText + ".");
        }
        return index;
    }

    /**
     * Prints the given message surrounded by horizontal divider lines,
     * matching the sample UI shown in the course spec.
     */
    private static void printBoxed(String message) {
        System.out.println(LINE);
        for (String line : message.split("\n")) {
            System.out.println(" " + line);
        }
        System.out.println(LINE);
    }

    /**
     * Exception type for anything Ben-specific that goes wrong while
     * handling a command (bad input, missing arguments, and so on).
     * The message is prefixed with "OOPS!!!" so it's ready to print
     * as-is, matching the course spec's sample error messages.
     */
    private static class BenException extends Exception {
        BenException(String message) {
            super("OOPS!!! " + message);
        }
    }

    /**
     * Base class for anything Ben is tracking: a description plus a
     * done/not-done status. Subclasses add their own extra fields (a
     * deadline's "by" date, an event's "from"/"to" times) and override
     * {@link #getTypeIcon()} to identify themselves in the list.
     */
    private abstract static class Task {
        private final String description;
        private boolean isDone;

        Task(String description) {
            this.description = description;
            this.isDone = false;
        }

        void markAsDone() {
            isDone = true;
        }

        void markAsNotDone() {
            isDone = false;
        }

        String getStatusIcon() {
            return isDone ? "X" : " ";
        }

        /** One-letter tag identifying the task type: "T", "D", or "E". */
        abstract String getTypeIcon();

        /**
         * Renders this task as one line for the data file, using " | " as
         * the field separator, e.g. {@code T | 1 | read book}. The second
         * field is the done flag (1 = done, 0 = not done). Subclasses
         * append their extra fields.
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
                boolean done = parts[1].equals("1");
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
                if (done) {
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

    /** A task without any date/time attached, e.g. "visit new theme park". */
    private static class Todo extends Task {
        Todo(String description) {
            super(description);
        }

        @Override
        String getTypeIcon() {
            return "T";
        }
    }

    /**
     * A task that needs to be done before a specific date (and,
     * optionally, a time).
     * <p>
     * Level-8: the "/by" value is parsed into a {@link LocalDate} (plus an
     * optional {@link LocalTime}) instead of being kept as free text, so
     * the date can be re-formatted for display and, later, compared or
     * sorted. Accepted input: an ISO date {@code 2019-12-02}, or
     * day/month/year {@code 2/12/2019}, each optionally followed by a
     * 24-hour {@code HHmm} time, e.g. {@code 2019-12-02 1800}.
     */
    private static class Deadline extends Task {
        /** ISO formats accepted from the user / data file, tried in order. */
        private static final DateTimeFormatter[] DATE_INPUTS = {
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
        };
        private static final DateTimeFormatter TIME_INPUT = DateTimeFormatter.ofPattern("HHmm");
        private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("MMM d yyyy");
        private static final DateTimeFormatter TIME_DISPLAY = DateTimeFormatter.ofPattern("h:mma");

        private final LocalDate date;
        /** Time of day, or {@code null} when only a date was given. */
        private final LocalTime time;

        Deadline(String description, String by) throws BenException {
            super(description);
            String[] parts = by.trim().split("\\s+", 2);
            this.date = parseDate(parts[0], by);
            this.time = parts.length > 1 ? parseTime(parts[1], by) : null;
        }

        private static LocalDate parseDate(String text, String original) throws BenException {
            for (DateTimeFormatter format : DATE_INPUTS) {
                try {
                    return LocalDate.parse(text, format);
                } catch (DateTimeParseException ignored) {
                    // try the next accepted format
                }
            }
            throw new BenException("I couldn't read the deadline date \"" + original
                    + "\". Try e.g. \"deadline return book /by 2019-12-02 1800\".");
        }

        private static LocalTime parseTime(String text, String original) throws BenException {
            try {
                return LocalTime.parse(text, TIME_INPUT);
            } catch (DateTimeParseException e) {
                throw new BenException("I couldn't read the deadline time in \"" + original
                        + "\". Use a 24-hour HHmm time, e.g. 1800.");
            }
        }

        @Override
        String getTypeIcon() {
            return "D";
        }

        @Override
        String serialize() {
            // Store in a form the constructor above can read straight back.
            String saved = date.toString() + (time != null ? " " + time.format(TIME_INPUT) : "");
            return super.serialize() + " | " + saved;
        }

        @Override
        public String toString() {
            String shown = date.format(DATE_DISPLAY)
                    + (time != null ? ", " + time.format(TIME_DISPLAY) : "");
            return super.toString() + " (by: " + shown + ")";
        }
    }

    /** A task that starts and ends at specific date/times. */
    private static class Event extends Task {
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

    /**
     * Reads and writes the task list to a plain-text file on disk.
     * <p>
     * The file is line-based: one {@link Task#serialize()} line per task.
     * Missing file or missing parent folder are treated as "no tasks yet"
     * (on load) or created on demand (on save), so the chatbot works on a
     * fresh machine where nothing has been saved before.
     */
    private static class Storage {
        private final Path file;

        Storage(String relativePath) {
            // Path.of splits on "/" and re-joins with the OS separator,
            // so the same string works on Windows, macOS and Linux.
            this.file = Path.of(relativePath);
        }

        /**
         * Loads the saved tasks, or an empty list if the file does not
         * exist yet. Individual corrupted lines are skipped with a warning
         * printed to the console rather than aborting the whole load.
         *
         * @throws BenException if the file exists but cannot be read
         */
        List<Task> load() throws BenException {
            List<Task> tasks = new ArrayList<>();
            if (!Files.exists(file)) {
                return tasks;
            }
            List<String> lines;
            try {
                lines = Files.readAllLines(file);
            } catch (IOException e) {
                throw new BenException("Could not read the save file (" + file + "). Starting with an empty list.");
            }
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    tasks.add(Task.deserialize(line.trim()));
                } catch (BenException e) {
                    System.out.println(e.getMessage());
                }
            }
            return tasks;
        }

        /**
         * Overwrites the data file with the current task list, creating
         * the parent folder first if it is not there yet. A failure to
         * save is reported to the console but does not stop the chatbot.
         */
        void save(List<Task> tasks) {
            try {
                Path parent = file.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                List<String> lines = new ArrayList<>();
                for (Task task : tasks) {
                    lines.add(task.serialize());
                }
                Files.write(file, lines);
            } catch (IOException e) {
                System.out.println("Warning: could not save tasks to " + file + " (" + e.getMessage() + ")");
            }
        }
    }
}
