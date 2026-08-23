/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * This is the Level-0 (skeletal) version: it just greets the
 * user and exits — no input is read yet.
 */
public class Ben {
    public static void main(String[] args) {
        String logo = " ____              \n"
                + "|  _ \\ ___ _ __    \n"
                + "| |_) / _ \\ '_ \\   \n"
                + "|  _ <  __/ | | |  \n"
                + "|_| \\_\\___|_| |_|  \n";
        System.out.println(logo);

        String greeting = "____________________________________________________________\n"
                + " Hello! I'm Ben\n"
                + " What can I do for you?\n"
                + "____________________________________________________________";
        System.out.println(greeting);

        String farewell = " Bye. Hope to see you again soon!\n"
                + "____________________________________________________________";
        System.out.println(farewell);
    }
}
