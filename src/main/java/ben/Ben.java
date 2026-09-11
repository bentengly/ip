package ben;

import java.util.List;

/**
 * Entry point for Ben, a simple command-line chatbot.
 * <p>
 * A-MoreOOP: the program is now split across small, single-responsibility
 * classes. {@code Ben} just wires them together and runs the main loop:
 * <ul>
 *   <li>{@link Ui} &ndash; reads commands and prints replies</li>
 *   <li>{@link Storage} &ndash; loads/saves the task list (Level-7)</li>
 *   <li>{@link TaskList} &ndash; holds the tasks and the add/delete/lookup logic</li>
 *   <li>{@link Parser} &ndash; makes sense of a line of input</li>
 * </ul>
 * Task types stay as the {@link Task} class hierarchy (Level-4), and
 * command keywords as the {@link CommandWord} enum (A-Enums).
 */
public class Ben {
    /**
     * Relative path (from the project root) of the data file. Kept
     * relative and OS-independent so it behaves the same on any machine
     * the chatbot is copied to.
     */
    private static final String DATA_FILE = "data/ben.txt";

    private final Ui ui;
    private final Storage storage;
    private TaskList tasks;

    /**
     * Builds a chatbot that persists its tasks to {@code filePath}. A
     * failure to load leaves the chatbot running with an empty list
     * rather than crashing.
     *
     * @param filePath relative path of the data file to load from and save to
     */
    public Ben(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            tasks = new TaskList(storage.load());
        } catch (BenException e) {
            ui.showError(e.getMessage());
            tasks = new TaskList();
        }
    }

    /**
     * Builds a chatbot that uses the default data file. Convenience
     * constructor for the GUI (Level-10), which has no reason to choose a
     * different path.
     */
    public Ben() {
        this(DATA_FILE);
    }

    /**
     * Produces Ben's reply to a single line of input, for the GUI.
     * <p>
     * Level-10: this is the GUI's counterpart to {@link #run()}. Instead
     * of reading from and printing to the console, it takes one line and
     * returns the text to display, so the JavaFX layer never has to know
     * how a command is handled. Errors come back as their message text
     * rather than as thrown exceptions.
     *
     * @param input one line of user input (the same strings {@code run} accepts)
     * @return the message to show the user
     */
    public String getResponse(String input) {
        if (input.equals("bye")) {
            return "Bye. Hope to see you again soon!";
        }
        try {
            String reply = handleCommand(input);
            storage.save(tasks);
            return reply;
        } catch (BenException e) {
            return e.getMessage();
        }
    }

    /** Runs the read-eval-print loop until the user types "bye". */
    public void run() {
        ui.showWelcome();
        while (true) {
            String input = ui.readCommand();
            if (input.equals("bye")) {
                ui.showBye();
                break;
            }
            try {
                String reply = handleCommand(input);
                storage.save(tasks);
                ui.show(reply);
            } catch (BenException e) {
                ui.showError(e.getMessage());
            }
        }
        ui.close();
    }

    /**
     * Dispatches a single (non-"bye") line of input to the right handler
     * and returns the message to display.
     */
    private String handleCommand(String input) throws BenException {
        CommandWord command = Parser.commandWord(input);
        String args = Parser.args(input);

        switch (command) {
            case LIST:
                return formatList();
            case MARK:
                return setDone(args, true);
            case UNMARK:
                return setDone(args, false);
            case DELETE:
                return deleteTask(args);
            case TODO:
                if (args.isEmpty()) {
                    throw new BenException("The description of a todo cannot be empty.");
                }
                return addTask(new Todo(args));
            case DEADLINE:
                return addTask(Parser.parseDeadline(args));
            case EVENT:
                return addTask(Parser.parseEvent(args));
            case FIND:
                return findTasks(args);
            case BYE:
            case UNKNOWN:
            default:
                throw new BenException("I'm sorry, but I don't know what that means :-(");
        }
    }

    /**
     * Adds the given task to the list and returns the confirmation
     * message: "Got it. I've added this task: ... Now you have N tasks in
     * the list."
     */
    private String addTask(Task task) {
        tasks.add(task);
        return "Got it. I've added this task:\n  " + task + "\n" + taskCountLine();
    }

    /**
     * Builds the numbered listing of all stored tasks, one per line, e.g.
     * "Here are the tasks in your list:\n1.[T][X] read book".
     */
    private String formatList() {
        return formatNumberedList("Here are the tasks in your list:", tasks.asList(), "(no tasks yet)");
    }

    /**
     * Builds the listing of tasks whose description contains
     * {@code keyword}, in the same numbered format as {@code list}.
     */
    private String findTasks(String keyword) throws BenException {
        if (keyword.isEmpty()) {
            throw new BenException("Tell me what to look for, e.g. \"find book\".");
        }
        List<Task> matches = tasks.find(keyword);
        return formatNumberedList("Here are the matching tasks in your list:", matches, "(no matching tasks)");
    }

    /**
     * Builds a numbered listing of {@code items}, one per line under
     * {@code header}, or {@code header} followed by {@code emptyMessage} if
     * there are none. Shared by {@link #formatList()} and
     * {@link #findTasks(String)}, which only differ in which tasks they list.
     */
    private String formatNumberedList(String header, List<Task> items, String emptyMessage) {
        if (items.isEmpty()) {
            return header + "\n" + emptyMessage;
        }
        StringBuilder sb = new StringBuilder(header);
        for (int i = 0; i < items.size(); i++) {
            sb.append("\n").append(i + 1).append(".").append(items.get(i));
        }
        return sb.toString();
    }

    /**
     * Marks (or unmarks) the task named by {@code indexText} and returns
     * the confirmation message to show the user.
     */
    private String setDone(String indexText, boolean isDone) throws BenException {
        String commandName = isDone ? "mark" : "unmark";
        Task task = tasks.get(Parser.parseIndex(indexText, commandName));
        if (isDone) {
            task.markAsDone();
            return "Nice! I've marked this task as done:\n  " + task;
        } else {
            task.markAsNotDone();
            return "OK, I've marked this task as not done yet:\n  " + task;
        }
    }

    /**
     * Removes the task named by {@code indexText} and returns the
     * confirmation message: "Noted. I've removed this task: ... Now you
     * have N tasks in the list."
     */
    private String deleteTask(String indexText) throws BenException {
        Task removed = tasks.remove(Parser.parseIndex(indexText, "delete"));
        return "Noted. I've removed this task:\n  " + removed + "\n" + taskCountLine();
    }

    /**
     * Returns "Now you have N task(s) in the list.", pluralised to match the
     * current size of {@link #tasks}. Shared by {@link #addTask(Task)} and
     * {@link #deleteTask(String)}, the two commands that report the new count.
     */
    private String taskCountLine() {
        int size = tasks.size();
        return "Now you have " + size + " task" + (size == 1 ? "" : "s") + " in the list.";
    }

    /**
     * Launches the chatbot with the default data file.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        new Ben(DATA_FILE).run();
    }
}
