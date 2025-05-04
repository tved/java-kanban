package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void shouldBeEqualWithSameId() {
        Epic epic1 = new Epic(1, "epic1", "epic1 desc");
        Epic epic2 = new Epic(1, "epic2", "epic2 desc");
        assertEquals(epic1, epic2, "Объекты Epic не равны");
    }

    @Test
    public void shouldNotBeEqualWithDifferentIds() {
        Epic epic1 = new Epic(1, "epic", "epic desc");
        Epic epic2 = new Epic(2, "epic", "epic desc");
        assertNotEquals(epic1, epic2, "Объекты Epic не должны быть равны друг другу");
    }

    @Test
    public void shouldHaveStatusNewByDefault() {
        Epic epic = new Epic("test epic", "epic desc");
        assertEquals(Status.NEW, epic.getStatus(), "Новый эпик без подзадач должен иметь статус NEW");
    }

    @Test
    public void shouldHaveStatusNewWithAllNewSubtasks() {
        Epic epic = new Epic("test epic", "epic desc");
        Subtask subtask1 = new Subtask("subtask 1", "subtask desc", Status.NEW, 1, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        Subtask subtask2 = new Subtask("subtask 2", "subtask desc", Status.NEW, 1, Duration.ofMinutes(240), LocalDateTime.of(2025, 5, 5, 14, 0));
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(Status.NEW, epic.getStatus(), "Эпик со всеми подзадачами в статусе NEW должен иметь статус NEW");
    }

    @Test
    public void shouldHaveStatusDoneWithAllDoneSubtasks() {
        Epic epic = new Epic("test epic", "epic desc");
        Subtask subtask1 = new Subtask("subtask 1", "subtask desc", Status.DONE, 1, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        Subtask subtask2 = new Subtask("subtask 2", "subtask desc", Status.DONE, 1, Duration.ofMinutes(240), LocalDateTime.of(2025, 5, 5, 14, 0));
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus(), "Эпик со всеми подзадачами в статусе DONE должен иметь статус DONE");
    }

    @Test
    public void shouldHaveStatusInProgressWithMixedSubtasks() {
        Epic epic = new Epic("test epic", "epic desc");
        Subtask subtask1 = new Subtask("subtask 1", "subtask desc", Status.NEW, 1, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        Subtask subtask2 = new Subtask("subtask 2", "subtask desc", Status.DONE, 1, Duration.ofMinutes(240), LocalDateTime.of(2025, 5, 5, 14, 0));
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Эпик с подзадачами в разных статусах должен иметь статус IN_PROGRESS");
    }

    @Test
    public void shouldUpdateStatusWhenSubtasksAreRemoved() {
        Epic epic = new Epic("test epic", "epic desc");
        Subtask subtask1 = new Subtask(2,"subtask 1", "subtask desc", Status.DONE, 1, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        Subtask subtask2 = new Subtask(3,"subtask 2", "subtask desc", Status.NEW, 1, Duration.ofMinutes(240), LocalDateTime.of(2025, 5, 5, 14, 0));
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Эпик с подзадачами в разных статусах должен иметь статус IN_PROGRESS");
        epic.removeSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus(), "Эпик со всеми подзадачами в статусе DONE должен иметь статус DONE");
        epic.removeSubtask(subtask1);
        assertEquals(Status.NEW, epic.getStatus(), "Эпик без подзадач должен иметь статус NEW");
    }
}