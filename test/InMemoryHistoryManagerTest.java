import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import service.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import service.HistoryManager;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
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
            Task task = new Task(i + 1, "task " + (i + 1), "description " + (i + 1), Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5 + i, 13, 0));
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(5, history.size(), "История должна содержать 5 задач");
    }

    @Test
    public void shouldRemoveTasks() {
        Task task1 = new Task(1, "task 1", "description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        Task task2 = new Task(2, "task 2", "description 2", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 6, 13, 0));
        Task task3 = new Task(3, "task 3", "description 3", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 7, 13, 0));
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

    @Test
    public void shouldRemoveFromBeginningMiddleAndEnd() {
        Task task1 = new Task(1, "Task 1", "desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        Task task2 = new Task(2, "Task 2", "desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 6, 13, 0));
        Task task3 = new Task(3, "Task 3", "desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 7, 13, 0));
        Task task4 = new Task(4, "Task 4", "desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 8, 13, 0));

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task4);

        historyManager.remove(task1.getId());
        List<Task> removalFromBeginning = historyManager.getHistory();
        assertEquals(List.of(task2, task3, task4), removalFromBeginning, "Задача должна удаляться из начала истории");

        historyManager.remove(task3.getId());
        List<Task> removalFromMiddle = historyManager.getHistory();
        assertEquals(List.of(task2, task4), removalFromMiddle, "Задача должна удаляться из середины истории");

        historyManager.remove(task4.getId());
        List<Task> removalFromEnd = historyManager.getHistory();
        assertEquals(List.of(task2), removalFromEnd, "Задача должна удаляться из конца истории");
    }

    @Test
    public void shouldNotAddDuplicates() {
        Task task = new Task(1, "Task 1", "desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов");
    }


}
