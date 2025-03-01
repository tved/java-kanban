package test;

import model.Task;
import model.Status;
import service.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import service.HistoryManager;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class InMemoryHistoryManagerTest {
    @Test
    public void shouldAddTasks() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        for (int i = 0; i < 5; i++) {
            Task task = new Task(i + 1, "task " + (i + 1), "description " + (i + 1), Status.NEW);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(5, history.size(), "История должна содержать 5 задач");
    }

    @Test
    public void shouldContain10TasksMax() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        int totalTasks = 15;
        for (int i = 0; i < totalTasks; i++) {
            Task task = new Task(i + 1, "task " + (i + 1), "description " + (i + 1), Status.NEW);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать максимум 10 задач");
    }
}
