import java.util.ArrayList;
import java.util.List;

/**
 * The in-memory list of tasks, with the add / delete / lookup operations
 * the rest of the program needs.
 * <p>
 * A-MoreOOP: pulled out of the main class so that "what the task list can
 * do" lives in one place. Task numbers used by commands are 1-based (as
 * the user sees them); this class validates them and throws
 * {@link BenException} for anything out of range.
 */
class TaskList {
    private final List<Task> tasks;

    /** Creates an empty list. */
    TaskList() {
        this.tasks = new ArrayList<>();
    }

    /** Creates a list pre-populated with the given tasks (e.g. loaded from disk). */
    TaskList(List<Task> initialTasks) {
        this.tasks = new ArrayList<>(initialTasks);
    }

    void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns the task at the given 1-based position.
     *
     * @throws BenException if no task has that number
     */
    Task remove(int oneBasedIndex) throws BenException {
        return tasks.remove(checkIndex(oneBasedIndex) - 1);
    }

    /**
     * Returns the task at the given 1-based position.
     *
     * @throws BenException if no task has that number
     */
    Task get(int oneBasedIndex) throws BenException {
        return tasks.get(checkIndex(oneBasedIndex) - 1);
    }

    int size() {
        return tasks.size();
    }

    boolean isEmpty() {
        return tasks.isEmpty();
    }

    /** Returns the backing list, e.g. for saving or iterating. */
    List<Task> asList() {
        return tasks;
    }

    private int checkIndex(int oneBasedIndex) throws BenException {
        if (oneBasedIndex < 1 || oneBasedIndex > tasks.size()) {
            throw new BenException("There is no task number " + oneBasedIndex + ".");
        }
        return oneBasedIndex;
    }
}
