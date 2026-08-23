import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * Level-6 (Delete): on top of Level-5's error handling, tasks can now be
 * removed with "delete &lt;index&gt;", which reports the removed task and
 * the new task count, the same way adding one does.
 */
public class Ben {
    private static final String LINE = "____________________________________________________________";

    public static void main(String[] args) {
        String logo = " ____              \n"
                + "|  _ \\ ___ _ __    \n"
                + "| |_) / _ \\ '_ \\   \n"
                + "|  _ <  __/ | | |  \n"
                + "|_| \\_\\___|_| |_|  \n";
        System.out.println(logo);

        printBoxed("Hello! I'm Ben\nWhat can I do for you?");

        List<Task> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                printBoxed("Bye. Hope to see you again soon!");
                break;
            }
            try {
                printBoxed(handleCommand(input, tasks));
            } catch (BenException e) {
                printBoxed(e.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * Dispatches a single (non-"bye") line of input to the right handler
     * and returns the message to display. Throws {@link BenException} for
     * any recognized-but-invalid or unrecognized command.
     */
    private static String handleCommand(String input, List<Task> tasks) throws BenException {
        if (input.equals("list")) {
            return formatList(tasks);
        } else if (input.equals("mark") || input.startsWith("mark ")) {
            String indexText = input.length() > 4 ? input.substring(5).trim() : "";
            return setDone(tasks, indexText, true);
        } else if (input.equals("unmark") || input.startsWith("unmark ")) {
            String indexText = input.length() > 6 ? input.substring(7).trim() : "";
            return setDone(tasks, indexText, false);
        } else if (input.equals("delete") || input.startsWith("delete ")) {
            String indexText = input.length() > 6 ? input.substring(7).trim() : "";
            return deleteTask(tasks, indexText);
        } else if (input.equals("todo") || input.startsWith("todo ")) {
            String description = input.length() > 4 ? input.substring(5).trim() : "";
            if (description.isEmpty()) {
                throw new BenException("The description of a todo cannot be empty.");
            }
            return addTask(tasks, new Todo(description));
        } else if (input.equals("deadline") || input.startsWith("deadline ")) {
            return addTask(tasks, parseDeadline(input));
        } else if (input.equals("event") || input.startsWith("event ")) {
            return addTask(tasks, parseEvent(input));
        } else {
            throw new BenException("I'm sorry, but I don't know what that means :-(");
        }
    }

    private static Deadline parseDeadline(String input) throws BenException {
        String rest = input.length() > 8 ? input.substring(9).trim() : "";
        if (rest.isEmpty()) {
            throw new BenException("The description of a deadline cannot be empty.");
        }
        String[] parts = rest.split(" /by ", 2);
        String description = parts[0].trim();
        if (description.isEmpty()) {
            throw new BenException("The description of a deadline cannot be empty.");
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new BenException("A deadline needs a \"/by\" date/time, e.g. \"deadline return book /by Sunday\".");
        }
        return new Deadline(description, parts[1].trim());
    }

    private static Event parseEvent(String input) throws BenException {
        String rest = input.length() > 5 ? input.substring(6).trim() : "";
        if (rest.isEmpty()) {
            throw new BenException("The description of an event cannot be empty.");
        }
        String[] fromSplit = rest.split(" /from ", 2);
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

    /** A task that needs to be done before a specific date/time. */
    private static class Deadline extends Task {
        private final String by;

        Deadline(String description, String by) {
            super(description);
            this.by = by;
        }

        @Override
        String getTypeIcon() {
            return "D";
        }

        @Override
        public String toString() {
            return super.toString() + " (by: " + by + ")";
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
        public String toString() {
            return super.toString() + " (from: " + from + " to: " + to + ")";
        }
    }
}
