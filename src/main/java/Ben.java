import java.util.Scanner;

/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * Level-1 (Echo): greets the user, then repeatedly reads a line
 * of input and echoes it back, until the user types "bye".
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

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                printBoxed("Bye. Hope to see you again soon!");
                break;
            }
            // Level-1: just echo whatever the user typed.
            printBoxed(input);
        }
        scanner.close();
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
}
