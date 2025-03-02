package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    public HistoryManager history = Managers.getDefaultHistory();
    private int currentId = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();

    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    @Override
    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    @Override
    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    @Override
    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public void clearTasks() {
        for (Task task : tasks.values()) {
            history.remove(task);
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        for (Epic epic : epics.values()) {
            history.remove(epic);
        }
        epics.clear();
        for (Subtask subtask : subtasks.values()) {
            history.remove(subtask);
        }
        subtasks.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(subtask);
            }
            history.remove(subtask);
        }
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            history.add(tasks.get(id));
            return tasks.get(id);
        } else {
            System.out.println("Задачи с таким id не существует");
        }
        return null;
    }

    @Override
    public Epic getEpicById(int id) {
        if (epics.containsKey(id)) {
            history.add(epics.get(id));
            return epics.get(id);
        } else {
            System.out.println("Эпика с таким id не существует");
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            history.add(subtasks.get(id));
            return subtasks.get(id);
        } else {
            System.out.println("Подзадачи с таким id не существует");
        }
        return null;
    }

    @Override
    public void addTask(Task task) {
        tasks.put(currentId, task);
        task.setId(currentId);
        currentId++;
    }

    @Override
    public void addEpic(Epic epic) {
        epics.put(currentId, epic);
        epic.setId(currentId);
        currentId++;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        subtasks.put(currentId, subtask);
        subtask.setId(currentId);
        currentId++;
        Epic currentEpic = epics.get(subtask.getEpicId());
        if (currentEpic != null) {
            currentEpic.addSubtask(subtask);
        } else {
            System.out.println("Эпик для подзадачи указан неверно или не существует.");
        }
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        List<Subtask> epicSubtasks = epics.get(epic.getId()).getSubtasks();
        epic.setSubtasks(epicSubtasks);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic currentEpic = epics.get(subtask.getEpicId());
        if (currentEpic != null) {
            List<Subtask> subtasksInEpic = currentEpic.getSubtasks();
            for (int i = 0; i < subtasksInEpic.size(); i++) {
                if (subtasksInEpic.get(i).getId() == subtask.getId()) {
                    subtasksInEpic.set(i, subtask);
                    currentEpic.setSubtasks(subtasksInEpic);
                    break;
                }
            }
        } else {
            System.out.println("Эпик для подзадачи указан неверно или не существует.");
        }

    }

    @Override
    public void deleteTask(Task task) {
        tasks.remove(task.getId());
        history.remove(task);
    }

    @Override
    public void deleteEpic(Epic epic) {
        for (Subtask subtask : epic.getSubtasks()) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(epic.getId());
        history.remove(epic);
    }

    @Override
    public void deleteSubtask(Subtask subtask) {
        Epic currentEpic = epics.get(subtask.getEpicId());
        List<Subtask> subtasksInEpic = currentEpic.getSubtasks();
        subtasksInEpic.remove(subtask);
        currentEpic.setSubtasks(subtasksInEpic);

        subtasks.remove(subtask.getId());
        history.remove(subtask);
    }

    @Override
    public List<Subtask> getSubtasksInEpic(Epic epic) {
        return epics.get(epic.getId()).getSubtasks();
    }
}
