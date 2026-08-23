import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * Level-3 (Mark as Done): tasks are now {@link Task} objects that can be
 * marked done via "mark &lt;index&gt;" (1-based), and back to not-done via
 * "unmark &lt;index&gt;". The list view shows each task's completion status.
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
            } else {
                tasks.add(new Task(input));
                printBoxed("added: " + input);
            }
        }
        scanner.close();
    }

    /**
     * Builds the numbered listing of all stored tasks, one per line, e.g.
     * "1.[X] read book".
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
     * A single task Ben is tracking: a description plus a done/not-done
     * status.
     */
    private static class Task {
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

        @Override
        public String toString() {
            return "[" + getStatusIcon() + "] " + description;
        }
    }
}
