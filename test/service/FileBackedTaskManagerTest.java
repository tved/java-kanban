package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;
import errors.ManagerSaveException;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    File tempFile;
    @Override
    protected FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("task-manager-test", ".csv");
            return new FileBackedTaskManager(tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл", e);
        }
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        Task task2 = new Task("Task 2", "Desc 2", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 6, 13, 0));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(2, loaded.getTasks().size());
    }

    @Test
    void shouldSaveAndLoadEpicsAndSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic Desc");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Desc", Status.NEW, epic.getId(), Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        taskManager.addSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getEpics().size());
        assertEquals(1, loaded.getSubtasks().size());

        Subtask loadedSubtask = loaded.getSubtasks().values().iterator().next();
        assertEquals(epic.getId(), loadedSubtask.getEpicId());
    }

    @Test
    public void shouldNotThrowWhenLoadingValidFile() {
        assertDoesNotThrow(() -> {
            FileBackedTaskManager.loadFromFile(tempFile);
        }, "Загружает корректный файл");
    }

    @Test
    public void shouldThrowExceptionWhenReadingCorruptedFile() throws IOException {
        File corruptedFile = File.createTempFile("invalid", ".csv");

        try (FileWriter writer = new FileWriter(corruptedFile)) {
            writer.write("id,type,name,status,description,epic\n100,TASK,taskname,NEW,Описание,пять,2025-05-05T13:00\n");
        }

        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(corruptedFile);
        }, "Должно выбрасываться исключение при чтении некорректного файла");
    }

}
