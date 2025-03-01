package test;

import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {
    @Test
    public void shouldSetEpicId() {
        Subtask subtask = new Subtask("Subtask A", "Description A", Status.NEW, 5);
        assertEquals(5, subtask.getEpicId(), "Должен быть задан epicId");
    }
}
