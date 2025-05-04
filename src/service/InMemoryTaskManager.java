package service;

import errors.NotFoundException;
import errors.TaskOverlapException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    public HistoryManager history = Managers.getDefaultHistory();
    protected int currentId = 1;
    protected final HashMap<Integer, Task> tasks = new HashMap<>();

    protected final HashMap<Integer, Epic> epics = new HashMap<>();

    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    Set<Task> prioritizedTasks = new TreeSet<>((Task t1, Task t2) -> {
        LocalDateTime time1 = t1.getStartTime();
        LocalDateTime time2 = t2.getStartTime();
        if (time1 == null && time2 == null) {
            return t1.getId() - t2.getId();
        }
        if (time1 == null) return 1;
        if (time2 == null) return -1;

        return time1.equals(time2) ? t1.getId() - t2.getId() : time1.compareTo(time2);
    });

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
        tasks.values().forEach(task -> {
            history.remove(task.getId());
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
        });
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        epics.values().forEach((epic -> {
            history.remove(epic.getId());
        }));
        epics.clear();
        subtasks.values().forEach(subtask -> {
            history.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        subtasks.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.values().forEach(subtask -> {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(subtask);
            }
            history.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задачи с id " + id + " не существует");
        }
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпика с id " + id + " не существует");
        }
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадачи с id " + id + " не существует");
        }
        history.add(subtask);
        return subtask;
    }

    @Override
    public void addTask(Task task) {
        if (isTaskOverlapping(task)) {
            throw new TaskOverlapException();
        }
        tasks.put(currentId, task);
        task.setId(currentId);
        currentId++;
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epics.put(currentId, epic);
        epic.setId(currentId);
        currentId++;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (isTaskOverlapping(subtask)) {
            throw new TaskOverlapException();
        }
        subtasks.put(currentId, subtask);
        subtask.setId(currentId);
        currentId++;
        Epic currentEpic = epics.get(subtask.getEpicId());
        if (currentEpic == null) {
            throw new NotFoundException("Эпик с id " + subtask.getEpicId() + " не найден.");
        }
        currentEpic.addSubtask(subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (isTaskOverlapping(task)) {
            throw new TaskOverlapException();
        }
        Task oldTask = tasks.get(task.getId());
        if (oldTask != null && oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
        if (isTaskOverlapping(subtask)) {
            throw new TaskOverlapException();
        }
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask == null) {
            throw new NotFoundException("Подзадача с id " + subtask.getId() + " не найдена.");
        }

        if (oldSubtask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubtask);
        }

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        subtasks.put(subtask.getId(), subtask);
        Epic currentEpic = epics.get(subtask.getEpicId());
        if (currentEpic == null) {
            throw new NotFoundException("Эпик с id " + subtask.getEpicId() + " не найден.");
        }

        List<Subtask> subtasksInEpic = currentEpic.getSubtasks();
        subtasksInEpic.stream().filter(s -> s.getId() == subtask.getId()).findFirst().ifPresent(foundSubtask -> {
            int index = subtasksInEpic.indexOf(foundSubtask);
            subtasksInEpic.set(index, subtask);
            currentEpic.setSubtasks(subtasksInEpic);
        });

    }

    @Override
    public void deleteTask(Task task) {
        tasks.remove(task.getId());
        history.remove(task.getId());
        prioritizedTasks.remove(task);
    }

    @Override
    public void deleteEpic(Epic epic) {
        epic.getSubtasks().forEach(subtask -> {
            subtasks.remove(subtask.getId());
            history.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        epics.remove(epic.getId());
        history.remove(epic.getId());
    }

    @Override
    public void deleteSubtask(Subtask subtask) {
        Epic currentEpic = epics.get(subtask.getEpicId());
        currentEpic.removeSubtask(subtask);
        subtasks.remove(subtask.getId());
        history.remove(subtask.getId());
        prioritizedTasks.remove(subtask);
    }

    @Override
    public List<Subtask> getSubtasksInEpic(Epic epic) {
        Epic foundEpic = epics.get(epic.getId());
        if (foundEpic == null) {
            throw new NotFoundException("Эпик с id " + epic.getId() + " не найден.");
        }
        return foundEpic.getSubtasks();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean isOverlapping(Task a, Task b) {
        if (a.getStartTime() != null && a.getEndTime() != null && b.getStartTime() != null && b.getEndTime() != null) {
            return a.getStartTime().isBefore(b.getEndTime()) && b.getStartTime().isBefore(a.getEndTime());
        }
        return false;
    }

    @Override
    public boolean isTaskOverlapping(Task newTask) {
        return prioritizedTasks.stream().filter(task -> task.getId() != newTask.getId()).anyMatch(task -> isOverlapping(task, newTask));
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}
