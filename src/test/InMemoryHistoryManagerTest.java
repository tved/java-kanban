package test;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import service.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.InMemoryTaskManager;

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
    public void shouldContain10TasksMax() {
        int totalTasks = 15;
        for (int i = 0; i < totalTasks; i++) {
            Task task = new Task(i + 1, "task " + (i + 1), "description " + (i + 1), Status.NEW);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать максимум 10 задач");
    }

    @Test
    public void shouldRemoveTasks() {
        Task task1 = new Task(1, "task 1", "description 1", Status.NEW);
        Task task2 = new Task(1, "task 2", "description 2", Status.NEW);
        Task task3 = new Task(1, "task 3", "description 3", Status.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи после удаления");
    }
}
