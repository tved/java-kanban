package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

         InMemoryTaskManager taskManager = new InMemoryTaskManager();
        //FileBackedTaskManager taskManager = new FileBackedTaskManager("tracker.csv");
        Task task1 = new Task("task 1", "do sth useful", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 1, 12, 0));
        Task task2 = new Task("task 2", "do sth different", Status.NEW, Duration.ofMinutes(180), LocalDateTime.now());
        Epic epic1 = new Epic("Epic 1", "do sth");
        Epic epic2 = new Epic("Epic 2", "do sth else");
        Subtask subtask1For1 = new Subtask("subtask1For1", "desc for task", Status.NEW, 3, Duration.ofMinutes(240), LocalDateTime.of(2025, 5, 5, 13, 0));
        Subtask subtask2For1 = new Subtask("subtask2For1", "desc for task", Status.NEW, 3, Duration.ofMinutes(240), LocalDateTime.of(2025, 5, 5, 17, 0));
        Subtask subtask3For1 = new Subtask("subtask3For1", "desc for task", Status.NEW, 3, Duration.ofMinutes(240), LocalDateTime.of(2025, 5, 6, 9, 0));
        Subtask subtask1For2 = new Subtask("subtask1For2", "desc for task", Status.NEW, 4, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 11, 0));
        Subtask subtask2For2 = new Subtask("subtask2For2", "desc for task", Status.NEW, 4, Duration.ofMinutes(180), LocalDateTime.of(2025, 5, 5, 13, 0));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addSubtask(subtask1For1);
        taskManager.addSubtask(subtask2For1);
        taskManager.addSubtask(subtask3For1);
        taskManager.addSubtask(subtask1For2);
        taskManager.addSubtask(subtask2For2);
        taskManager.getTaskById(1);
        taskManager.getEpicById(3);
        taskManager.getTaskById(1);
        taskManager.getSubtaskById(6);
        taskManager.getSubtaskById(6);
        taskManager.getSubtaskById(7);
        taskManager.getSubtaskById(8);
        taskManager.getSubtaskById(8);
        taskManager.getSubtaskById(9);
        taskManager.getSubtaskById(6);
        taskManager.getSubtaskById(6);
        taskManager.getSubtaskById(7);
        taskManager.getEpicById(3);
        System.out.println(taskManager.history.getHistory());

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(new File("tracker.csv"));
        System.out.println("Restored tasks:");
        for (Task task : restored.getTasks().values()) {
            System.out.println(task);
        }
        for (Epic epic : restored.getEpics().values()) {
            System.out.println(epic);
        }
        for (Subtask subtask : restored.getSubtasks().values()) {
            System.out.println(subtask);
        }

    }
}
