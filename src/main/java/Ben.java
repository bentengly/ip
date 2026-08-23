import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * Level-4 (ToDos, Events, Deadlines): tasks now come in three flavours —
 * {@link Todo}, {@link Deadline}, and {@link Event} — created via the
 * "todo", "deadline .../by ...", and "event .../from .../to ..." commands.
 * Dates and times are kept as plain strings for now (no parsing yet).
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
            } else if (input.equals("list")) {
                printBoxed(formatList(tasks));
            } else if (input.startsWith("mark ")) {
                printBoxed(setDone(tasks, input.substring("mark ".length()).trim(), true));
            } else if (input.startsWith("unmark ")) {
                printBoxed(setDone(tasks, input.substring("unmark ".length()).trim(), false));
            } else if (input.equals("todo") || input.startsWith("todo ")) {
                String description = input.length() > 4 ? input.substring(5).trim() : "";
                printBoxed(addTask(tasks, new Todo(description)));
            } else if (input.equals("deadline") || input.startsWith("deadline ")) {
                printBoxed(addTask(tasks, parseDeadline(input)));
            } else if (input.equals("event") || input.startsWith("event ")) {
                printBoxed(addTask(tasks, parseEvent(input)));
            } else {
                printBoxed("OOPS!!! I'm not sure what that means yet.");
            }
        }
        scanner.close();
    }

    private static Deadline parseDeadline(String input) {
        String rest = input.length() > 8 ? input.substring(9) : "";
        String[] parts = rest.split(" /by ", 2);
        String description = parts[0].trim();
        String by = parts.length > 1 ? parts[1].trim() : "";
        return new Deadline(description, by);
    }

    private static Event parseEvent(String input) {
        String rest = input.length() > 5 ? input.substring(6) : "";
        String[] fromSplit = rest.split(" /from ", 2);
        String description = fromSplit[0].trim();
        String from = "";
        String to = "";
        if (fromSplit.length > 1) {
            String[] toSplit = fromSplit[1].split(" /to ", 2);
            from = toSplit[0].trim();
            to = toSplit.length > 1 ? toSplit[1].trim() : "";
        }
        return new Event(description, from, to);
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
    private static String setDone(List<Task> tasks, String indexText, boolean done) {
        int index;
        try {
            index = Integer.parseInt(indexText);
        } catch (NumberFormatException e) {
            return "OOPS!!! that needs a task number, e.g. \"" + (done ? "mark" : "unmark") + " 2\".";
        }
        if (index < 1 || index > tasks.size()) {
            return "OOPS!!! There is no task number " + indexText + ".";
        }
        Task task = tasks.get(index - 1);
        if (done) {
            task.markAsDone();
            return "Nice! I've marked this task as done:\n  " + task;
        } else {
            task.markAsNotDone();
            return "OK, I've marked this task as not done yet:\n  " + task;
        }
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
