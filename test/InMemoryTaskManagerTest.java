import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    public void shouldNotAllowSubtaskToBeItsOwnEpic() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("subtask 1", "subtask 1 desc", Status.NEW, epic.getId(), Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        taskManager.addSubtask(subtask);
        Subtask invalidSubtask = new Subtask(subtask.getId(), "Subtask self", "Описание подзадачи", Status.NEW, subtask.getId(), Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        taskManager.updateSubtask(invalidSubtask);

        Task epicForSubtask = taskManager.getEpicById(invalidSubtask.getEpicId());
        assertNull(epicForSubtask, "Подзадача не может быть своим же эпиком, поэтому эпика с таким id быть не должно");
    }

    @Test
    public void historyShouldContainPreviousVersionOfTask() {
        Task task = new Task("task", "initial desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        taskManager.addTask(task);

        taskManager.getTaskById(1);

        Task updatedTask = new Task(1, "updated task name", "updated desc", Status.DONE, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        taskManager.updateTask(updatedTask);
        taskManager.getTaskById(1);

        List<Task> history = taskManager.history.getHistory();
        assertFalse(history.isEmpty(), "История не должна быть пустой.");
        assertEquals(1, history.size(), "В истории должна быть 1 задача");

        Task taskInHistory = history.get(0);
        assertEquals("updated desc", taskInHistory.getDescription(), "История должна хранить обновленную версию описания");
        assertEquals(Status.DONE, taskInHistory.getStatus(), "История должна хранить обновленный статус задачи");
    }

    @Test
    public void shouldNotADdOverlappingTasks() {
        Task task1 = new Task("Task 1", "desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        taskManager.addTask(task1);
        Task task2 = new Task("Task 2", "desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 5, 13, 30));
        taskManager.addTask(task2);

        assertEquals(1, taskManager.getTasks().size(), "Задача с пересечением по времени не должна быть добавлена");
    }

}
