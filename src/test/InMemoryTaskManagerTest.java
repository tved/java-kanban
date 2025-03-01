package test;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void createTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void shouldAddAndGetTaskById() {
        Task task = new Task("task 1", "desc 1", Status.NEW);
        taskManager.addTask(task);
        Task addedTask = taskManager.getTaskById(1);
        assertNotNull(addedTask, "Задача должна быть получена по id.");
        assertEquals(1, addedTask.getId(), "ID задачи должно быть 1");
    }

    @Test
    public void shouldAddAndGetEpicById() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Epic addedEpic = taskManager.getEpicById(1);
        assertNotNull(addedEpic, "Эпик должен быть получен по id.");
        assertEquals(1, addedEpic.getId(), "ID эпика должно быть 1");
    }

    @Test
    public void shouldAddAndGetSubtaskById() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("subtask 1", "subtask desc", Status.NEW, 1);
        taskManager.addSubtask(subtask);
        Subtask addedSubtask = taskManager.getSubtaskById(2);
        assertNotNull(addedSubtask, "Подзадача должна быть получена по id");
        assertEquals(2, addedSubtask.getId(), "ID подзадачи должно быть 2");
        assertEquals(1, addedSubtask.getEpicId(), "ID эпика для подзадачи должно быть 1");
    }

    @Test
    public void shouldUpdateTask() {
        Task task = new Task("task 1", "desc 1", Status.NEW);
        taskManager.addTask(task);
        task.setStatus(Status.DONE);
        Task updatedTask = new Task(1, "task 1", "updated desc", Status.DONE);
        taskManager.updateTask(updatedTask);
        Task updated = taskManager.getTaskById(1);
        assertEquals(Status.DONE, updated.getStatus(), "Статус задачи должен обновиться");
        assertEquals("updated desc", updated.getDescription(), "Описание задачи должно обновиться");
    }

    @Test
    public void shouldUpdateEpic() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask 1", "subtask1 desc", Status.NEW, 1);
        Subtask subtask2 = new Subtask("subtask 2", "subtask1 desc", Status.NEW, 1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = new Epic(1, "epic 1", "updated epic desc");
        taskManager.updateEpic(updatedEpic);
        Epic updated = taskManager.getEpicById(1);
        assertEquals("updated epic desc", updated.getDescription(), "Описание эпика должно обновиться");
        List<Subtask> subtasks = updated.getSubtasks();
        assertEquals(2, subtasks.size(), "Эпик должен содержать 2 подзадачи");
    }

    @Test
    public void shouldUpdateSubtask() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("subtask 1", "desc 1", Status.NEW, 1);
        taskManager.addSubtask(subtask);
        Subtask updatedSubtask = new Subtask(2, "subtask 1", "updated subtask desc", Status.DONE, 1);
        taskManager.updateSubtask(updatedSubtask);
        Subtask updated = taskManager.getSubtaskById(2);
        assertEquals(Status.DONE, updated.getStatus(), "Статус подзадачи должен обновиться");
        assertEquals("updated subtask desc", updated.getDescription(), "Описание подзадачи должно обновиться");
        Epic newEpic = taskManager.getEpicById(1);
        List<Subtask> epicSubtasks = newEpic.getSubtasks();
        assertEquals(1, epicSubtasks.size(), "Эпик должен содержать ровно одну подзадачу");
        Subtask epicSubtask = epicSubtasks.get(0);
        assertEquals(2, epicSubtask.getId(), "ID подзадачи должен совпадать");
        assertEquals("updated subtask desc", epicSubtask.getDescription(), "Описание подзадачи должно обновиться");
        assertEquals(Status.DONE, epicSubtask.getStatus(), "Статус подзадачи должен обновиться");
    }

    @Test
    public void shouldDeleteTask() {
        Task task = new Task("task 1", "desc 1", Status.NEW);
        taskManager.addTask(task);
        taskManager.deleteTask(task);
        Task deletedTask = taskManager.getTaskById(1);
        assertNull(deletedTask, "Задача должна быть удалена");
    }

    @Test
    public void testDeleteEpic() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("subtask 1", "desc 1", Status.NEW, 1);
        taskManager.addSubtask(subtask);
        taskManager.deleteEpic(epic);
        Epic deletedEpic = taskManager.getEpicById(1);
        assertNull(deletedEpic, "Эпик должен быть удалён");
        Subtask subtaskInDeletedEpic = taskManager.getSubtaskById(2);
        assertNull(subtaskInDeletedEpic, "Подзадача должна быть удалена при удалении эпика");
    }

    @Test
    public void testDeleteSubtask() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask 1", "desc 1", Status.NEW, 1);
        Subtask subtask2 = new Subtask("subtask 2", "desc 2", Status.NEW, 1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.deleteSubtask(subtask1);
        assertNull(taskManager.getSubtaskById(2), "Удалённая подзадача должна отсутствовать");
        Epic epicWithDeletedSubtask = taskManager.getEpicById(1);
        List<Subtask> epicSubtasks = epicWithDeletedSubtask.getSubtasks();
        assertEquals(1, epicSubtasks.size(), "После удаления эпик должен содержать 1 подзадачу");
        assertEquals(3, epicSubtasks.get(0).getId(), "Оставшаяся подзадача должна иметь id 3");
    }

    @Test
    public void testClearTasks() {
        Task task1 = new Task("task 1", "desc 1", Status.NEW);
        Task task2 = new Task("task 2", "desc 2", Status.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.clearTasks();
        HashMap<Integer, Task> tasks = taskManager.getTasks();
        assertTrue(tasks.isEmpty(), "Список задач должен быть пуст после удаления всех задач");
    }

    @Test
    public void testClearEpics() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("subtask 1", "subtask desc", Status.NEW, 1);
        taskManager.addSubtask(subtask);
        taskManager.clearEpics();
        HashMap<Integer, Epic> epics = taskManager.getEpics();
        HashMap<Integer, Subtask> subtasks = taskManager.getSubtasks();
        assertTrue(epics.isEmpty(), "Список эпиков должен быть пуст после удаления всех эпиков");
        assertTrue(subtasks.isEmpty(), "Список подзадач должен быть пуст после удаления всех эпиков");
    }

    @Test
    public void testClearSubtasks() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask 1", "subtask 1 desc", Status.NEW, 1);
        Subtask subtask2 = new Subtask("subtask 2", "subtask 2 desc", Status.NEW, 1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.clearSubtasks();
        HashMap<Integer, Subtask> subtasks = taskManager.getSubtasks();
        assertTrue(subtasks.isEmpty(), "Список подзадач должен быть пуст после удаления всех подзадач");
        Epic updatedEpic = taskManager.getEpicById(1);
        assertTrue(updatedEpic.getSubtasks().isEmpty(), "У эпика не должно быть подзадач после очистки списка всех подзадач");
    }

    @Test
    public void testGetSubtasksInEpic() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask 1", "subtask 1 desc", Status.NEW, 1);
        Subtask subtask2 = new Subtask("subtask 2", "subtask 2 desc", Status.NEW, 1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        List<Subtask> epicSubtasks = taskManager.getSubtasksInEpic(epic);
        assertEquals(2, epicSubtasks.size(), "Эпик должен содержать 2 подзадачи.");
    }

    @Test
    public void shouldNotAllowSubtaskToBeItsOwnEpic() {
        Epic epic = new Epic("epic 1", "epic desc");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("subtask 1", "subtask 1 desc", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask);
        Subtask invalidSubtask = new Subtask(subtask.getId(), "Subtask self", "Описание подзадачи", Status.NEW, subtask.getId());
        taskManager.updateSubtask(invalidSubtask);

        Task epicForSubtask = taskManager.getEpicById(invalidSubtask.getEpicId());
        assertNull(epicForSubtask, "Подзадача не может быть своим же эпиком, поэтому эпика с таким id быть не должно");
    }

    @Test
    public void taskShouldRemainUnchangedAfterAdding() {
        String taskName = "new task";
        String taskDesc = "task desc";
        Status taskStatus = Status.NEW;
        Task task = new Task(taskName, taskDesc, taskStatus);
        taskManager.addTask(task);
        Task addedTask = taskManager.getTaskById(task.getId());

        assertEquals(taskName, addedTask.getName(), "Имя задачи должно остаться неизменным");
        assertEquals(taskDesc, addedTask.getDescription(), "Описание задачи должно остаться неизменным");
        assertEquals(taskStatus, addedTask.getStatus(), "Статус задачи должен остаться неизменным");
    }

    @Test
    public void historyShouldContainPreviousVersionOfTask() {
        Task task = new Task("task", "initial desc", Status.NEW);
        taskManager.addTask(task);

        taskManager.getTaskById(1);

        Task updatedTask = new Task(1, "updated task name", "updated desc", Status.DONE);
        taskManager.updateTask(updatedTask);
        taskManager.getTaskById(1);

        List<Task> history = taskManager.history.getHistory();
        assertFalse(history.isEmpty(), "История не должна быть пустой.");
        assertEquals(2, history.size(), "В истории должно быть 2 задачи");

        Task initialTaskInHistory = history.get(0);
        assertEquals("initial desc", initialTaskInHistory.getDescription(), "История должна хранить предыдущую версию описания");
        assertEquals(Status.NEW, initialTaskInHistory.getStatus(), "История должна хранить предыдущую версию статуса");
    }
}
