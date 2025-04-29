package service;

import model.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final Path filePath;

    public FileBackedTaskManager(String path) {
        this.filePath = Paths.get(path);
    }

    public void save() {
        try (Writer fileWriter = new FileWriter(filePath.toFile())) {
            fileWriter.write("id,type,name,status,description,epic\n");
            for (Task task : super.getTasks().values()) {
                fileWriter.write(toString(task));
            }

            for (Epic epic : super.getEpics().values()) {
                fileWriter.write(toString(epic));
            }

            for (Subtask subtask : super.getSubtasks().values()) {
                fileWriter.write(toString(subtask));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время записи файла.", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            fileReader.readLine(); // пропускает строку с названием колонок
            int maxId = 1;
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                Task task;
                try {
                    task = manager.fromString(line);
                } catch (Exception e) {
                    throw new ManagerSaveException("Ошибка при чтении строки: " + line, e);
                }
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }

                manager.currentId = maxId + 1;
            }
            // привяжем подзадачи к эпику, чтобы также посчитать duration, startTime и endTime в epic.setSubtasks()
            for (Epic epic : manager.getEpics().values()) {
                List<Subtask> subtasksForEpic = manager.getSubtasks().values().stream().filter(subtask -> subtask.getEpicId() == epic.getId()).toList();
                epic.setSubtasks(subtasksForEpic);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время чтения файла.", e);
        }

        return manager;
    }

    public String toString(Task task) {
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "null";
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "null";
        StringBuilder result = new StringBuilder(task.getId() + "," +
                task.getType() + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                durationStr + "," +
                startTimeStr);
        if (task instanceof Subtask) {
            result.append(",").append(((Subtask) task).getEpicId());
        }

        return result + "\n";
    }

    public Task fromString(String value) {
        String[] values = value.split(",");
        int id = Integer.parseInt(values[0]);
        String name = values[2];
        Status status = Status.valueOf(values[3]);
        String desc = values[4];
        Duration duration = values[5].equals("null") ? null : Duration.ofMinutes(Long.parseLong(values[5]));
        LocalDateTime startTime = values[6].equals("null") ? null : LocalDateTime.parse(values[6]);
        TaskType type = TaskType.valueOf(values[1]);
        if (type == TaskType.EPIC) {
            return new Epic(id, name, desc);
        } else if (type == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(values[7]);
            return new Subtask(id, name, desc, status, epicId, duration, startTime);
        } else {
            return new Task(id, name, desc, status, duration, startTime);
        }
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(Task task) {
        super.deleteTask(task);
        save();
    }

    @Override
    public void deleteEpic(Epic epic) {
        super.deleteEpic(epic);
        save();
    }

    @Override
    public void deleteSubtask(Subtask subtask) {
        super.deleteSubtask(subtask);
        save();
    }
}
