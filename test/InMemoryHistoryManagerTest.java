package test;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import service.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import service.HistoryManager;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void createHistoryManager() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void shouldAddTasks() {
        for (int i = 0; i < 5; i++) {
            Task task = new Task(i + 1, "task " + (i + 1), "description " + (i + 1), Status.NEW);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(5, history.size(), "История должна содержать 5 задач");
    }

    @Test
    public void shouldRemoveTasks() {
        Task task1 = new Task(1, "task 1", "description 1", Status.NEW);
        Task task2 = new Task(2, "task 2", "description 2", Status.NEW);
        Task task3 = new Task(3, "task 3", "description 3", Status.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи после удаления");
    }

    @Test
    public void shouldReturnEmptyListWhenHistoryIsEmpty() {
        List<Task> emptyHistory = historyManager.getHistory();
        assertTrue(emptyHistory.isEmpty(), "История должна быть пустой");
        historyManager.remove(1);
        assertTrue(historyManager.getHistory().isEmpty(), "История пуста после удаления несуществующей задачи");
    }
}
