package ben;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for {@link TaskList}, focusing on 1-based indexing and range checks. */
class TaskListTest {
    @Test
    void addAndSize_trackTheNumberOfTasks() {
        TaskList list = new TaskList();
        assertTrue(list.isEmpty());
        list.add(new Todo("a"));
        list.add(new Todo("b"));
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
    }

    @Test
    void get_usesOneBasedIndexing() throws BenException {
        TaskList list = new TaskList();
        list.add(new Todo("first"));
        list.add(new Todo("second"));
        assertEquals("[T][ ] second", list.get(2).toString());
    }

    @Test
    void get_indexOutOfRange_throwsBenException() {
        TaskList list = new TaskList();
        list.add(new Todo("only"));
        assertThrows(BenException.class, () -> list.get(0));
        assertThrows(BenException.class, () -> list.get(2));
    }

    @Test
    void remove_returnsRemovedTaskAndShrinksList() throws BenException {
        TaskList list = new TaskList();
        list.add(new Todo("a"));
        list.add(new Todo("b"));
        Task removed = list.remove(1);
        assertEquals("[T][ ] a", removed.toString());
        assertEquals(1, list.size());
    }
}
