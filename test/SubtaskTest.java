import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {
    @Test
    public void shouldSetEpicId() {
        Subtask subtask = new Subtask("Subtask A", "Description A", Status.NEW, 5, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        assertEquals(5, subtask.getEpicId(), "Должен быть задан epicId");
    }
}
