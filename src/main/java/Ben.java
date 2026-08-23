import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * Level-2 (Add, List): on top of Level-1's echo loop, Ben now
 * remembers whatever the user types (other than "list"/"bye")
 * as a task, and can list all stored tasks back on request.
 * Nothing is saved to disk yet.
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

        List<String> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                printBoxed("Bye. Hope to see you again soon!");
                break;
            } else if (input.equals("list")) {
                printBoxed(formatList(tasks));
            } else {
                tasks.add(input);
                printBoxed("added: " + input);
            }
        }
        scanner.close();
    }

    /**
     * Builds the numbered listing of all stored tasks, one per line
     */
    private static String formatList(List<String> tasks) {
        if (tasks.isEmpty()) {
            return "No tasks yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(i + 1).append(". ").append(tasks.get(i));
        }
        return sb.toString();
    }

    /**
     * Prints the given message surrounded by horizontal divider lines
     */
    private static void printBoxed(String message) {
        System.out.println(LINE);
        for (String line : message.split("\n")) {
            System.out.println(" " + line);
        }
        System.out.println(LINE);
    }
}
