package test;

import model.Status;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    public void shouldBeEqualWithSameId() {
        Task task1 = new Task(1, "task1", "task1 desc");
        Task task2 = new Task(1, "task2", "task2 desc");
        assertEquals(task1, task2, "Объекты Task не равны");
    }

    @Test
    public void shouldNotBeEqualWithDifferentIds() {
        Task task1 = new Task(1, "task", "task desc");
        Task task2 = new Task(2, "task", "task desc");
        assertNotEquals(task1, task2, "Объекты Task не должны быть равны друг другу");
    }

    @Test
    public void shouldCreateTaskWithAllFields() {
        Task task = new Task(1, "task", "task desc", Status.NEW);
        assertEquals(1, task.getId(), "Id объекта Task не установлен");
        assertEquals(Status.NEW, task.getStatus(), "Статус объекта Task не установлен");
    }

    @Test
    public void shouldCreateWithoutStatus() {
        Task task = new Task(1, "task", "task desc", Status.NEW);
        assertEquals(1, task.getId(), "Id объекта Task не установлен");
        assertEquals(Status.NEW, task.getStatus(), "Статус объекта Task не установлен");
    }

    @Test
    public void shouldSetTestId() {
        Task task = new Task("task", "task desc", Status.NEW);
        task.setId(1);
        assertEquals(1, task.getId(), "Id объекта Task не установлен");
    }

}
